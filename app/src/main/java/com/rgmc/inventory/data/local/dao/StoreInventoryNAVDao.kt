package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.StoreInventoryNAVEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreInventoryNAVDao {
    @Query("SELECT * FROM store_inventory_nav WHERE storeId = :storeId ORDER BY barcode ASC")
    fun getNavByStore(storeId: Int): Flow<List<StoreInventoryNAVEntity>>

    @Query("SELECT * FROM store_inventory_nav WHERE storeId = :storeId ORDER BY barcode ASC")
    suspend fun getNavByStoreList(storeId: Int): List<StoreInventoryNAVEntity>

    @Query("SELECT * FROM store_inventory_nav WHERE barcode = :barcode AND storeId = :storeId LIMIT 1")
    suspend fun getNavByBarcode(barcode: String, storeId: Int): StoreInventoryNAVEntity?

    @Query("SELECT * FROM store_inventory_nav WHERE (barcode LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND storeId = :storeId")
    suspend fun searchNav(query: String, storeId: Int): List<StoreInventoryNAVEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(navList: List<StoreInventoryNAVEntity>)

    @Query("UPDATE store_inventory_nav SET actualQty = actualQty + :qty WHERE barcode = :barcode AND storeId = :storeId")
    suspend fun incrementActualQty(barcode: String, storeId: Int, qty: Int)

    @Query("DELETE FROM store_inventory_nav WHERE storeId = :storeId")
    suspend fun deleteByStore(storeId: Int)

    @Query("DELETE FROM store_inventory_nav")
    suspend fun deleteAll()
}
