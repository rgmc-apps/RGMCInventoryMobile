package com.rgmc.inventory.data.repository

import android.util.Base64
import com.rgmc.inventory.data.local.AppDatabase
import com.rgmc.inventory.data.local.entity.*
import com.rgmc.inventory.data.remote.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class StoreInventoryRepository(private val db: AppDatabase, private val api: ApiService) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    fun getNavListFlow(storeId: Int): Flow<List<StoreInventoryNAVEntity>> = db.storeInventoryNAVDao().getNavByStore(storeId)

    suspend fun fetchAndCacheNavList(storeId: Int): Result<Unit> = runCatching {
        val resp = api.getNavList(storeId)
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                StoreInventoryNAVEntity(0, it.barcode, it.storeId, it.brandId, it.description, it.price, it.qty, it.actualQty, it.totalQty, it.createBy, it.createDate)
            } ?: emptyList()
            db.storeInventoryNAVDao().deleteByStore(storeId)
            db.storeInventoryNAVDao().insertAll(entities)
        } else throw Exception("API error: ${resp.code()}")
    }

    suspend fun fetchAndCacheBarcodeList(storeId: Int, cutOffDate: String): Result<Unit> = runCatching {
        val resp = api.getBarcodeList(StoreInventoryRequestDto(cutOffDate, storeId))
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                BarcodeEntity(0, it.cutOffDate, it.storeId, it.brandId, it.rack, it.type, it.text, it.locationId, it.createBy, it.createDate)
            } ?: emptyList()
            db.barcodeDao().deleteByStoreCutOff(storeId, cutOffDate)
            db.barcodeDao().insertAll(entities)
        } else throw Exception("API error: ${resp.code()}")
    }

    suspend fun fetchAndCacheInventoryList(storeId: Int, cutOffDate: String): Result<Unit> = runCatching {
        val resp = api.getInventoryList(StoreInventoryRequestDto(cutOffDate, storeId))
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                StoreInventoryEntity(0, it.inventoryDate, it.cutOffDate, it.storeId, it.barcode, it.locationId, it.rack, it.deviceId, it.brandId, it.qty, it.createBy, it.createDate)
            } ?: emptyList()
            db.storeInventoryDao().deleteByStoreCutOff(storeId, cutOffDate)
            db.storeInventoryDao().insertAll(entities)
        } else throw Exception("API error: ${resp.code()}")
    }

    suspend fun saveBarcodeScan(
        barcode: String, storeId: Int, brandId: Int, cutOffDate: String,
        locationId: Int, rack: Int, qty: Int, encoder: String, deviceId: String
    ) {
        val now = dateFormat.format(Date())
        repeat(qty) {
            db.barcodeDao().insert(BarcodeEntity(0, cutOffDate, storeId, brandId, rack, "EAN_13", barcode, locationId, encoder, now))
            db.storeInventoryDao().insert(StoreInventoryEntity(0, now, cutOffDate, storeId, barcode, locationId, rack, deviceId, brandId, 1, encoder, now))
        }
        db.storeInventoryNAVDao().incrementActualQty(barcode, storeId, qty)
    }

    suspend fun getNavByBarcode(barcode: String, storeId: Int) = db.storeInventoryNAVDao().getNavByBarcode(barcode, storeId)
    suspend fun searchNav(query: String, storeId: Int) = db.storeInventoryNAVDao().searchNav(query, storeId)

    suspend fun exportInventory(storeId: Int, cutOffDate: String, deviceId: String): Result<Unit> = runCatching {
        val inventories = db.storeInventoryDao().getInventoryByStoreCutOff(storeId, cutOffDate)
        val dtos = inventories.map {
            StoreInventoryDto(it.inventoryDate, it.cutOffDate, it.storeId, it.barcode, it.locationId, it.rack, it.deviceId, it.brandId, it.qty, it.createBy, it.createDate)
        }
        val resp = api.saveInventory(dtos)
        if (!resp.isSuccessful) throw Exception("API error: ${resp.code()}")
    }

    suspend fun exportBarcodes(storeId: Int, cutOffDate: String): Result<Unit> = runCatching {
        val barcodes = db.barcodeDao().getBarcodesByStoreCutOff(storeId, cutOffDate)
        val dtos = barcodes.map {
            BarcodeDto(it.id, it.cutOffDate, it.storeId, it.brandId, it.rack, it.type, it.text, it.locationId, it.createBy, it.createDate)
        }
        val resp = api.saveBarcodes(dtos)
        if (!resp.isSuccessful) throw Exception("API error: ${resp.code()}")
    }

    suspend fun exportPersonnel(storeId: Int, cutOffDate: String): Result<Unit> = runCatching {
        val personnel = db.storeInventoryPersonnelDao().getPersonnelByStoreCutOff(storeId, cutOffDate)
        val dtos = personnel.map {
            val sigBase64 = if (it.signature != null) Base64.encodeToString(it.signature, Base64.NO_WRAP) else ""
            StoreInventoryPersonnelDto(it.inventoryDate, it.cutOffDate, it.storeId, it.storePersonnel, sigBase64, it.createBy, it.createDate)
        }
        val resp = api.savePersonnel(dtos)
        if (!resp.isSuccessful) throw Exception("API error: ${resp.code()}")
    }

    suspend fun getPersonnelByStoreCutOff(storeId: Int, cutOffDate: String) = db.storeInventoryPersonnelDao().getPersonnelByStoreCutOff(storeId, cutOffDate)
    suspend fun insertPersonnel(p: StoreInventoryPersonnelEntity) = db.storeInventoryPersonnelDao().insert(p)
    suspend fun deletePersonnel(p: StoreInventoryPersonnelEntity) = db.storeInventoryPersonnelDao().delete(p)
}
