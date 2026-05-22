package com.rgmc.inventory.data.repository

import com.rgmc.inventory.data.local.AppDatabase
import com.rgmc.inventory.data.local.entity.ProductImageEntity
import com.rgmc.inventory.data.remote.ApiService
import android.util.Base64

class ProductRepository(private val db: AppDatabase, private val api: ApiService) {
    suspend fun fetchProductsByCategory(categoryId: Int): Result<List<ProductImageEntity>> = runCatching {
        val resp = api.getProductsByCategory(categoryId)
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                val imgBytes = if (!it.imageData.isNullOrEmpty()) Base64.decode(it.imageData, Base64.DEFAULT) else null
                ProductImageEntity(it.stockNumber, it.productDesignId, it.brandId, it.lineNumber, it.categoryId, imgBytes, it.description, it.price)
            } ?: emptyList()
            db.productImageDao().deleteByCategory(categoryId)
            db.productImageDao().insertAll(entities)
            entities
        } else throw Exception("API error: ${resp.code()}")
    }

    suspend fun getProductsByCategory(categoryId: Int) = db.productImageDao().getProductsByCategory(categoryId)
    suspend fun getProductByStockNumber(stockNumber: String) = db.productImageDao().getProductByStockNumber(stockNumber)
    suspend fun searchProducts(query: String) = db.productImageDao().searchProducts(query)
}
