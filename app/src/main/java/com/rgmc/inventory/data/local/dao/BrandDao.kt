package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.BrandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDao {
    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun getAllBrands(): Flow<List<BrandEntity>>

    @Query("SELECT * FROM brands ORDER BY name ASC")
    suspend fun getAllBrandsList(): List<BrandEntity>

    @Query("SELECT * FROM brands WHERE brandId = :brandId")
    suspend fun getBrandById(brandId: Int): BrandEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(brands: List<BrandEntity>)

    @Query("DELETE FROM brands")
    suspend fun deleteAll()
}
