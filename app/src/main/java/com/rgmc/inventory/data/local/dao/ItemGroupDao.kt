package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.ItemGroupEntity

@Dao
interface ItemGroupDao {
    @Query("SELECT * FROM item_groups ORDER BY name ASC")
    suspend fun getAllItemGroups(): List<ItemGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(itemGroups: List<ItemGroupEntity>)

    @Query("DELETE FROM item_groups")
    suspend fun deleteAll()
}
