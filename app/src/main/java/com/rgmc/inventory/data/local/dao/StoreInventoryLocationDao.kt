package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.StoreInventoryLocationEntity

@Dao
interface StoreInventoryLocationDao {
    @Query("SELECT * FROM store_inventory_locations ORDER BY locationName ASC")
    suspend fun getAllLocations(): List<StoreInventoryLocationEntity>

    @Query("SELECT * FROM store_inventory_locations WHERE locationId = :locationId")
    suspend fun getLocationById(locationId: Int): StoreInventoryLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<StoreInventoryLocationEntity>)

    @Query("DELETE FROM store_inventory_locations")
    suspend fun deleteAll()
}
