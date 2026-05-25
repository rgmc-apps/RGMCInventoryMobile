package com.rgmc.inventory.data.repository

import com.rgmc.inventory.data.local.AppDatabase
import com.rgmc.inventory.data.local.entity.*
import com.rgmc.inventory.data.remote.*
import kotlinx.coroutines.flow.Flow

class BrandRepository(private val db: AppDatabase, private val api: ApiService) {
    fun getBrands(): Flow<List<BrandEntity>> = db.brandDao().getAllBrands()

    suspend fun fetchAndCacheBrands(): Result<Unit> = runCatching {
        val resp = api.getBrands()
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                BrandEntity(it.brandId, it.brandCode, it.companyId, it.initials, it.name, it.percentage, it.remark, it.isActive, it.createBy, it.createDate)
            } ?: emptyList()
            db.brandDao().deleteAll()
            db.brandDao().insertAll(entities)
        } else throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "", "GET api/brand"
        )
    }

    suspend fun fetchAndCacheCoordinators(): Result<Unit> = runCatching {
        val resp = api.getBrandCoordinators()
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                BrandCoordinatorEntity("${it.brandId}-${it.coorCode}", it.brandId, it.coorCode, it.name, it.remark, it.isActive, it.createBy, it.createDate)
            } ?: emptyList()
            db.brandCoordinatorDao().deleteAll()
            db.brandCoordinatorDao().insertAll(entities)
        } else throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "", "GET api/brand/coordinator"
        )
    }

    suspend fun fetchAndCacheItemGroups(): Result<Unit> = runCatching {
        val resp = api.getItemGroups()
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                ItemGroupEntity(it.itemGroupId, it.itemGroupCode, it.name, it.remark, it.isRW, it.isActive)
            } ?: emptyList()
            db.itemGroupDao().deleteAll()
            db.itemGroupDao().insertAll(entities)
        } else throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "", "GET api/itemgroup"
        )
    }

    suspend fun fetchAndCacheCategories(): Result<Unit> = runCatching {
        val resp = api.getCategories()
        if (resp.isSuccessful) {
            val entities = resp.body()?.map {
                CategoryEntity(it.categoryId, it.lineNumber, it.categoryCode, it.name, it.brandId, it.itemGroupId, it.remark, it.isActive)
            } ?: emptyList()
            db.categoryDao().deleteAll()
            db.categoryDao().insertAll(entities)
        } else throw ApiException(
            "API error: ${resp.code()}", resp.code(),
            resp.errorBody()?.string() ?: "", "GET api/category"
        )
    }

    suspend fun getBrandsLocal() = db.brandDao().getAllBrandsList()
    suspend fun getItemGroupsLocal() = db.itemGroupDao().getAllItemGroups()
    suspend fun getCategoriesLocal() = db.categoryDao().getAllCategories()
    suspend fun getCategoriesByBrand(brandId: Int) = db.categoryDao().getCategoriesByBrand(brandId)
    suspend fun getCategoriesByBrandAndItemGroup(brandId: Int, itemGroupId: Int) = db.categoryDao().getCategoriesByBrandAndItemGroup(brandId, itemGroupId)
    suspend fun getCoordinatorsByBrand(brandId: Int) = db.brandCoordinatorDao().getCoordinatorsByBrand(brandId)
}
