package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.ProductImageEntity

@Dao
interface ProductImageDao {
    @Query("SELECT * FROM product_images WHERE categoryId = :categoryId ORDER BY stockNumber ASC")
    suspend fun getProductsByCategory(categoryId: Int): List<ProductImageEntity>

    @Query("SELECT * FROM product_images WHERE brandId = :brandId ORDER BY stockNumber ASC")
    suspend fun getProductsByBrand(brandId: Int): List<ProductImageEntity>

    @Query("SELECT * FROM product_images WHERE stockNumber = :stockNumber LIMIT 1")
    suspend fun getProductByStockNumber(stockNumber: String): ProductImageEntity?

    @Query("SELECT * FROM product_images WHERE stockNumber LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<ProductImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductImageEntity>)

    @Query("DELETE FROM product_images WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: Int)
}
