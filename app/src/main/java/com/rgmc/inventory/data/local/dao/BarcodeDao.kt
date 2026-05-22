package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.BarcodeEntity

@Dao
interface BarcodeDao {
    @Query("SELECT * FROM barcodes WHERE storeId = :storeId AND cutOffDate = :cutOffDate ORDER BY createDate DESC")
    suspend fun getBarcodesByStoreCutOff(storeId: Int, cutOffDate: String): List<BarcodeEntity>

    @Query("SELECT * FROM barcodes WHERE text = :text AND storeId = :storeId AND cutOffDate = :cutOffDate LIMIT 1")
    suspend fun getBarcodeByText(text: String, storeId: Int, cutOffDate: String): BarcodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(barcode: BarcodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(barcodes: List<BarcodeEntity>)

    @Query("DELETE FROM barcodes WHERE storeId = :storeId AND cutOffDate = :cutOffDate")
    suspend fun deleteByStoreCutOff(storeId: Int, cutOffDate: String)

    @Query("DELETE FROM barcodes")
    suspend fun deleteAll()
}
