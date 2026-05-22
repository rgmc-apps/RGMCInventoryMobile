package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.BrandCoordinatorEntity

@Dao
interface BrandCoordinatorDao {
    @Query("SELECT * FROM brand_coordinators ORDER BY name ASC")
    suspend fun getAllCoordinators(): List<BrandCoordinatorEntity>

    @Query("SELECT * FROM brand_coordinators WHERE brandId = :brandId ORDER BY name ASC")
    suspend fun getCoordinatorsByBrand(brandId: Int): List<BrandCoordinatorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coordinators: List<BrandCoordinatorEntity>)

    @Query("DELETE FROM brand_coordinators")
    suspend fun deleteAll()
}
