package com.rgmc.inventory.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rgmc.inventory.data.local.dao.*
import com.rgmc.inventory.data.local.entity.*

@Database(
    entities = [
        BrandEntity::class,
        CategoryEntity::class,
        ItemGroupEntity::class,
        CustomerStoreEntity::class,
        CustomerEntity::class,
        BrandCoordinatorEntity::class,
        CutOffDateEntity::class,
        StoreInventoryLocationEntity::class,
        StoreInventoryCutOffEntity::class,
        StoreInventoryNAVEntity::class,
        StoreInventoryEntity::class,
        BarcodeEntity::class,
        StoreInventoryPersonnelEntity::class,
        ProductImageEntity::class,
        NoteEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun brandDao(): BrandDao
    abstract fun categoryDao(): CategoryDao
    abstract fun itemGroupDao(): ItemGroupDao
    abstract fun customerStoreDao(): CustomerStoreDao
    abstract fun customerDao(): CustomerDao
    abstract fun brandCoordinatorDao(): BrandCoordinatorDao
    abstract fun cutOffDateDao(): CutOffDateDao
    abstract fun storeInventoryLocationDao(): StoreInventoryLocationDao
    abstract fun storeInventoryCutOffDao(): StoreInventoryCutOffDao
    abstract fun storeInventoryNAVDao(): StoreInventoryNAVDao
    abstract fun storeInventoryDao(): StoreInventoryDao
    abstract fun barcodeDao(): BarcodeDao
    abstract fun storeInventoryPersonnelDao(): StoreInventoryPersonnelDao
    abstract fun productImageDao(): ProductImageDao
    abstract fun noteDao(): NoteDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "rgmc_inventory.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
