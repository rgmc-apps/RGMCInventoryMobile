package com.rgmc.inventory.data.local.dao

import androidx.room.*
import com.rgmc.inventory.data.local.entity.CustomerEntity

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY customerName ASC")
    suspend fun getAllCustomers(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE brandId = :brandId ORDER BY customerName ASC")
    suspend fun getCustomersByBrand(brandId: Int): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Query("DELETE FROM customers")
    suspend fun deleteAll()
}
