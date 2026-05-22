package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.SettingEntity

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSetting(): SettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(setting: SettingEntity)

    @Query("DELETE FROM settings")
    suspend fun deleteAll()
}
