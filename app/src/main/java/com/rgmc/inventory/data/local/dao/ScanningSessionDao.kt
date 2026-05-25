package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.ScanningSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanningSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ScanningSessionEntity): Long

    @Update
    suspend fun update(session: ScanningSessionEntity)

    @Query("SELECT * FROM scanning_sessions WHERE status = 'open' ORDER BY lastModifiedAt DESC")
    fun getOpenSessionsFlow(): Flow<List<ScanningSessionEntity>>

    @Query("SELECT * FROM scanning_sessions WHERE sessionId = :id LIMIT 1")
    suspend fun getSessionById(id: Int): ScanningSessionEntity?

    @Query("UPDATE scanning_sessions SET totalScanned = :count, lastModifiedAt = :time WHERE sessionId = :id")
    suspend fun updateScanCount(id: Int, count: Int, time: String)

    @Delete
    suspend fun delete(session: ScanningSessionEntity)
}
