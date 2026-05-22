package com.rgmc.inventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.rgmc.inventory.RGMCApp
import com.rgmc.inventory.data.local.entity.StoreInventoryPersonnelEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SigningViewModel(application: Application) : AndroidViewModel(application) {
    private val invRepo = (application as RGMCApp).inventoryRepository

    private val _personnel = MutableStateFlow<List<StoreInventoryPersonnelEntity>>(emptyList())
    val personnel: StateFlow<List<StoreInventoryPersonnelEntity>> = _personnel.asStateFlow()

    private var storeId: Int = 0
    private var cutOffDate: String = ""
    private var encoder: String = ""

    fun init(storeId: Int, cutOffDate: String, encoder: String) {
        this.storeId = storeId
        this.cutOffDate = cutOffDate
        this.encoder = encoder
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _personnel.value = invRepo.getPersonnelByStoreCutOff(storeId, cutOffDate)
        }
    }

    fun addPersonnel(name: String) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            invRepo.insertPersonnel(StoreInventoryPersonnelEntity(0, now, cutOffDate, storeId, name, null, encoder, now))
            load()
        }
    }

    fun saveSignature(personnelId: Int, signatureBytes: ByteArray) {
        viewModelScope.launch {
            val list = _personnel.value
            val p = list.find { it.id == personnelId } ?: return@launch
            invRepo.insertPersonnel(p.copy(signature = signatureBytes))
            load()
        }
    }

    fun deletePersonnel(p: StoreInventoryPersonnelEntity) {
        viewModelScope.launch {
            invRepo.deletePersonnel(p)
            load()
        }
    }
}
