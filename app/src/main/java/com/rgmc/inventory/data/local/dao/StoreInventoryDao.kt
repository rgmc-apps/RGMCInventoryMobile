package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.StoreInventoryEntity

@Dao
interface StoreInventoryDao {
    @Query("SELECT * FROM store_inventories WHERE storeId = :storeId AND cutOffDate = :cutOffDate ORDER BY createDate DESC")
    suspend fun getInventoryByStoreCutOff(storeId: Int, cutOffDate: String): List<StoreInventoryEntity>

    @Query("SELECT * FROM store_inventories WHERE barcode = :barcode AND storeId = :storeId AND cutOffDate = :cutOffDate")
    suspend fun getInventoryByBarcode(barcode: String, storeId: Int, cutOffDate: String): List<StoreInventoryEntity>

    @Query("SELECT COUNT(*) FROM store_inventories WHERE barcode = :barcode AND storeId = :storeId AND cutOffDate = :cutOffDate AND locationId = :locationId AND rack = :rack")
    suspend fun getScannedQty(barcode: String, storeId: Int, cutOffDate: String, locationId: Int, rack: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventory: StoreInventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inventories: List<StoreInventoryEntity>)

    @Query("DELETE FROM store_inventories WHERE storeId = :storeId AND cutOffDate = :cutOffDate")
    suspend fun deleteByStoreCutOff(storeId: Int, cutOffDate: String)

    @Query("DELETE FROM store_inventories")
    suspend fun deleteAll()
}
