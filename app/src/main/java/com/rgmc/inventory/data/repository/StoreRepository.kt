package com.rgmc.inventory.data.repository

import com.rgmc.inventory.data.local.AppDatabase
import com.rgmc.inventory.data.local.entity.*
import com.rgmc.inventory.data.remote.*

class StoreRepository(private val db: AppDatabase, private val api: ApiService) {
    suspend fun fetchAndCacheStores(): Result<Unit> = runCatching {
        val resp = api.getCustomerStores()
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                CustomerStoreEntity(it.storeId, it.storeTypeId, it.customerId, it.brandId, it.customerName, it.storeCode, it.name, it.storeTypeName, it.storeName, it.brandName)
            } ?: emptyList()
            db.customerStoreDao().deleteAll()
            db.customerStoreDao().insertAll(entities)
        } else throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "", "GET api/customerstore"
        )
    }

    suspend fun fetchAndCacheCustomers(): Result<Unit> = runCatching {
        val resp = api.getCustomers()
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                CustomerEntity("${it.customerId}-${it.brandId}", it.customerId, it.brandId, it.customerName)
            } ?: emptyList()
            db.customerDao().deleteAll()
            db.customerDao().insertAll(entities)
        } else throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "", "GET api/store/customer"
        )
    }

    suspend fun fetchAndCacheLocations(): Result<Unit> = runCatching {
        val resp = api.getLocations()
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                StoreInventoryLocationEntity(it.locationId, it.locationName, it.createBy, it.createDate)
            } ?: emptyList()
            db.storeInventoryLocationDao().deleteAll()
            db.storeInventoryLocationDao().insertAll(entities)
        } else throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "", "GET api/storeinventorylocation"
        )
    }

    suspend fun fetchAndCacheCutOffs(): Result<Unit> = runCatching {
        val resp = api.getAllCutOffs()
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                StoreInventoryCutOffEntity("${it.storeId}_${it.cutOffDate}", it.storeId, it.cutOffDate, it.isActive, it.createBy, it.createDate, it.updateBy, it.updateDate)
            } ?: emptyList()
            db.storeInventoryCutOffDao().deleteAll()
            db.storeInventoryCutOffDao().insertAll(entities)
        } else throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "", "GET api/storeinventorycutoff"
        )
    }

    suspend fun getAllStores() = db.customerStoreDao().getAllStores()
    suspend fun getStoresByBrand(brandId: Int) = db.customerStoreDao().getStoresByBrand(brandId)
    suspend fun getStoresByBrandAndCustomer(brandId: Int, customerId: Int) = db.customerStoreDao().getStoresByBrandAndCustomer(brandId, customerId)
    suspend fun getStoreById(storeId: Int) = db.customerStoreDao().getStoreById(storeId)
    suspend fun getCustomersByBrand(brandId: Int) = db.customerDao().getCustomersByBrand(brandId)
    suspend fun getAllLocations() = db.storeInventoryLocationDao().getAllLocations()
    suspend fun getAllCutOffs() = db.storeInventoryCutOffDao().getAllCutOffs()
    suspend fun getCutOffsByStore(storeId: Int) = db.storeInventoryCutOffDao().getCutOffsByStore(storeId)
    suspend fun getActiveCutOffs() = db.storeInventoryCutOffDao().getActiveCutOffs()
    suspend fun getStoreIdsWithActiveCutoffs() = db.storeInventoryCutOffDao().getActiveStoreIds()
    suspend fun createCutOff(storeId: Int, cutOffDate: String, createBy: String): Result<Unit> = runCatching {
        val resp = api.createCutOff(StoreInventoryCutOffRequestDto(storeId, cutOffDate, createBy))
        if (!resp.isSuccessful) throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "",
            "POST api/storeinventorycutoff/create",
            "storeId=$storeId, cutOffDate=$cutOffDate"
        )
    }
    suspend fun closeCutOff(storeId: Int, cutOffDate: String, createBy: String): Result<Unit> = runCatching {
        val resp = api.closeCutOff(StoreInventoryCutOffRequestDto(storeId, cutOffDate, createBy))
        if (!resp.isSuccessful) throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "",
            "POST api/storeinventorycutoff/close",
            "storeId=$storeId, cutOffDate=$cutOffDate"
        )
    }
}
