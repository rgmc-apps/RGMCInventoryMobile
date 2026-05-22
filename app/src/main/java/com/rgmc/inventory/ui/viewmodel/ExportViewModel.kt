package com.rgmc.inventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.rgmc.inventory.RGMCApp
import com.rgmc.inventory.data.local.entity.StoreInventoryCutOffEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExportState(
    val cutOffs: List<StoreInventoryCutOffEntity> = emptyList(),
    val selectedCutOff: StoreInventoryCutOffEntity? = null,
    val storeId: Int = 0,
    val encoder: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)

class ExportViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as RGMCApp
    private val invRepo = app.inventoryRepository
    private val storeRepo = app.storeRepository

    private val _state = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> = _state.asStateFlow()

    fun loadForStore(storeId: Int, encoder: String) {
        viewModelScope.launch {
            val cutOffs = storeRepo.getCutOffsByStore(storeId)
            _state.update { it.copy(cutOffs = cutOffs, storeId = storeId, encoder = encoder) }
        }
    }

    fun onCutOffSelected(c: StoreInventoryCutOffEntity) { _state.update { it.copy(selectedCutOff = c) } }

    fun exportAll(deviceId: String) {
        viewModelScope.launch {
            val s = _state.value
            val storeId = s.storeId
            val cutOff = s.selectedCutOff?.cutOffDate ?: return@launch
            _state.update { it.copy(isLoading = true, message = null) }
            val r1 = invRepo.exportInventory(storeId, cutOff, deviceId)
            val r2 = invRepo.exportBarcodes(storeId, cutOff)
            val r3 = invRepo.exportPersonnel(storeId, cutOff)
            if (r1.isSuccess && r2.isSuccess && r3.isSuccess) {
                storeRepo.closeCutOff(storeId, cutOff, s.encoder)
                _state.update { it.copy(isLoading = false, message = "Export successful. Cut-off closed.") }
            } else {
                val err = listOf(r1, r2, r3).firstOrNull { it.isFailure }?.exceptionOrNull()?.message
                _state.update { it.copy(isLoading = false, message = "Export failed: $err") }
            }
        }
    }
}
