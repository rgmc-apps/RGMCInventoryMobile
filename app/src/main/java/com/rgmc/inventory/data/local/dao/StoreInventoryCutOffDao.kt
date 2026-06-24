package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.StoreInventoryCutOffEntity

@Dao
interface StoreInventoryCutOffDao {
    @Query("SELECT * FROM store_inventory_cutoffs ORDER BY cutOffDate DESC")
    suspend fun getAllCutOffs(): List<StoreInventoryCutOffEntity>

    @Query("SELECT * FROM store_inventory_cutoffs WHERE storeId = :storeId ORDER BY cutOffDate DESC")
    suspend fun getCutOffsByStore(storeId: Int): List<StoreInventoryCutOffEntity>

    @Query("SELECT * FROM store_inventory_cutoffs WHERE isActive = 1 ORDER BY cutOffDate DESC")
    suspend fun getActiveCutOffs(): List<StoreInventoryCutOffEntity>

    @Query("SELECT DISTINCT storeId FROM store_inventory_cutoffs WHERE isActive = 1")
    suspend fun getActiveStoreIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cutOffs: List<StoreInventoryCutOffEntity>)

    @Query("DELETE FROM store_inventory_cutoffs")
    suspend fun deleteAll()
}
