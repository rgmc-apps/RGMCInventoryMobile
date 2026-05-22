package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.CutOffDateEntity

@Dao
interface CutOffDateDao {
    @Query("SELECT * FROM cut_off_dates ORDER BY cutOff DESC")
    suspend fun getAllCutOffDates(): List<CutOffDateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dates: List<CutOffDateEntity>)

    @Query("DELETE FROM cut_off_dates")
    suspend fun deleteAll()
}
