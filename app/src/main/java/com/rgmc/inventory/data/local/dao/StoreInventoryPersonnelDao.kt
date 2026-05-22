package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.StoreInventoryPersonnelEntity

@Dao
interface StoreInventoryPersonnelDao {
    @Query("SELECT * FROM store_inventory_personnel WHERE storeId = :storeId AND cutOffDate = :cutOffDate")
    suspend fun getPersonnelByStoreCutOff(storeId: Int, cutOffDate: String): List<StoreInventoryPersonnelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personnel: StoreInventoryPersonnelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(personnel: List<StoreInventoryPersonnelEntity>)

    @Delete
    suspend fun delete(personnel: StoreInventoryPersonnelEntity)

    @Query("DELETE FROM store_inventory_personnel WHERE storeId = :storeId AND cutOffDate = :cutOffDate")
    suspend fun deleteByStoreCutOff(storeId: Int, cutOffDate: String)
}
