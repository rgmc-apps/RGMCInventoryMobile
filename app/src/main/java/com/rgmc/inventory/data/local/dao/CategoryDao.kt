package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE brandId = :brandId ORDER BY name ASC")
    suspend fun getCategoriesByBrand(brandId: Int): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE brandId = :brandId AND itemGroupId = :itemGroupId ORDER BY name ASC")
    suspend fun getCategoriesByBrandAndItemGroup(brandId: Int, itemGroupId: Int): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
