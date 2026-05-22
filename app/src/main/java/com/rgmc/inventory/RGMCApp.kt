package com.rgmc.inventory

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.rgmc.inventory.data.local.AppDatabase
import com.rgmc.inventory.data.remote.NetworkModule
import com.rgmc.inventory.data.repository.*

class RGMCApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    val database by lazy { AppDatabase.getInstance(this) }
    val apiService by lazy { NetworkModule.apiService }
    val brandRepository by lazy { BrandRepository(database, apiService) }
    val productRepository by lazy { ProductRepository(database, apiService) }
    val storeRepository by lazy { StoreRepository(database, apiService) }
    val inventoryRepository by lazy { StoreInventoryRepository(database, apiService) }
    val noteRepository by lazy { NoteRepository(database) }
}
