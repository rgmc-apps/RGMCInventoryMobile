package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.CustomerStoreEntity

@Dao
interface CustomerStoreDao {
    @Query("SELECT * FROM customer_stores ORDER BY name ASC")
    suspend fun getAllStores(): List<CustomerStoreEntity>

    @Query("SELECT * FROM customer_stores WHERE brandId = :brandId ORDER BY name ASC")
    suspend fun getStoresByBrand(brandId: Int): List<CustomerStoreEntity>

    @Query("SELECT * FROM customer_stores WHERE brandId = :brandId AND customerId = :customerId ORDER BY name ASC")
    suspend fun getStoresByBrandAndCustomer(brandId: Int, customerId: Int): List<CustomerStoreEntity>

    @Query("SELECT * FROM customer_stores WHERE storeId = :storeId")
    suspend fun getStoreById(storeId: Int): CustomerStoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stores: List<CustomerStoreEntity>)

    @Query("DELETE FROM customer_stores")
    suspend fun deleteAll()
}
