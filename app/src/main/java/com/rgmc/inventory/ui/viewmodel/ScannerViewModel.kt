package com.rgmc.inventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.rgmc.inventory.RGMCApp
import com.rgmc.inventory.data.local.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ScannerSetupState(
    val brands: List<BrandEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val stores: List<CustomerStoreEntity> = emptyList(),
    val coordinators: List<BrandCoordinatorEntity> = emptyList(),
    val cutOffs: List<StoreInventoryCutOffEntity> = emptyList(),
    val locations: List<StoreInventoryLocationEntity> = emptyList(),
    val selectedBrand: BrandEntity? = null,
    val selectedCustomer: CustomerEntity? = null,
    val selectedStore: CustomerStoreEntity? = null,
    val selectedCoordinator: BrandCoordinatorEntity? = null,
    val selectedCutOff: StoreInventoryCutOffEntity? = null,
    val selectedLocation: StoreInventoryLocationEntity? = null,
    val rack: Int = 1,
    val encoder: String = "",
    val isLoading: Boolean = false,
    val isCutOffLoading: Boolean = false,
    val error: String? = null
)

data class ScanResultState(
    val barcode: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val navQty: Int = 0,
    val scannedQty: Int = 0,
    val variance: Int = 0
)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as RGMCApp
    private val brandRepo = app.brandRepository
    private val storeRepo = app.storeRepository
    private val invRepo = app.inventoryRepository

    private val _setupState = MutableStateFlow(ScannerSetupState())
    val setupState: StateFlow<ScannerSetupState> = _setupState.asStateFlow()

    private val _navList = MutableStateFlow<List<StoreInventoryNAVEntity>>(emptyList())
    val navList: StateFlow<List<StoreInventoryNAVEntity>> = _navList.asStateFlow()

    private val _lastScan = MutableStateFlow<ScanResultState?>(null)
    val lastScan: StateFlow<ScanResultState?> = _lastScan.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            _setupState.update { ScannerSetupState(isLoading = true) }
            try {
                brandRepo.fetchAndCacheBrands()
                storeRepo.fetchAndCacheStores()
                storeRepo.fetchAndCacheCustomers()
                brandRepo.fetchAndCacheCoordinators()
                storeRepo.fetchAndCacheCutOffs()
                storeRepo.fetchAndCacheLocations()
                brandRepo.fetchAndCacheItemGroups()
                brandRepo.fetchAndCacheCategories()
                val brands = brandRepo.getBrandsLocal()
                val locations = storeRepo.getAllLocations()
                _setupState.update { it.copy(brands = brands, locations = locations, isLoading = false) }
            } catch (e: Exception) {
                _setupState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onBrandSelected(brand: BrandEntity) {
        viewModelScope.launch {
            val customers = storeRepo.getCustomersByBrand(brand.brandId)
            val coordinators = brandRepo.getCoordinatorsByBrand(brand.brandId)
            _setupState.update { it.copy(selectedBrand = brand, customers = customers, coordinators = coordinators, selectedCustomer = null, selectedStore = null, stores = emptyList()) }
        }
    }

    fun onCustomerSelected(customer: CustomerEntity) {
        viewModelScope.launch {
            val brand = _setupState.value.selectedBrand ?: return@launch
            val stores = storeRepo.getStoresByBrandAndCustomer(brand.brandId, customer.customerId)
            _setupState.update { it.copy(selectedCustomer = customer, stores = stores, selectedStore = null) }
        }
    }

    fun onStoreSelected(store: CustomerStoreEntity) {
        viewModelScope.launch {
            _setupState.update { it.copy(selectedStore = store, cutOffs = emptyList(), selectedCutOff = null, isCutOffLoading = true) }
            val cutOffs = storeRepo.getCutOffsByStore(store.storeId)
            _setupState.update { it.copy(cutOffs = cutOffs, isCutOffLoading = false) }
        }
    }

    fun onCoordinatorSelected(c: BrandCoordinatorEntity) { _setupState.update { it.copy(selectedCoordinator = c) } }
    fun onCutOffSelected(c: StoreInventoryCutOffEntity) { _setupState.update { it.copy(selectedCutOff = c) } }
    fun onLocationSelected(l: StoreInventoryLocationEntity) { _setupState.update { it.copy(selectedLocation = l) } }
    fun onRackChanged(rack: Int) { _setupState.update { it.copy(rack = rack) } }
    fun onEncoderChanged(encoder: String) { _setupState.update { it.copy(encoder = encoder) } }

    fun loadNavList(storeId: Int) {
        viewModelScope.launch {
            _setupState.update { it.copy(isLoading = true) }
            invRepo.fetchAndCacheNavList(storeId)
            invRepo.getNavListFlow(storeId).collect { list -> _navList.value = list }
        }
    }

    fun importData(storeId: Int, cutOffDate: String) {
        viewModelScope.launch {
            _setupState.update { it.copy(isLoading = true) }
            val r1 = invRepo.fetchAndCacheBarcodeList(storeId, cutOffDate)
            val r2 = invRepo.fetchAndCacheInventoryList(storeId, cutOffDate)
            _setupState.update { it.copy(isLoading = false) }
            if (r1.isFailure || r2.isFailure) _message.emit("Import failed: ${r1.exceptionOrNull()?.message}")
            else _message.emit("Import successful")
        }
    }

    fun processBarcodeScan(barcode: String, qty: Int, deviceId: String) {
        viewModelScope.launch {
            val state = _setupState.value
            val storeId = state.selectedStore?.storeId ?: return@launch
            val brandId = state.selectedBrand?.brandId ?: 0
            val cutOff = state.selectedCutOff?.cutOffDate ?: return@launch
            val locId = state.selectedLocation?.locationId ?: 0
            invRepo.saveBarcodeScan(barcode, storeId, brandId, cutOff, locId, state.rack, qty, state.encoder, deviceId)
            val nav = invRepo.getNavByBarcode(barcode, storeId)
            _lastScan.value = ScanResultState(
                barcode = barcode,
                description = nav?.description ?: "",
                price = nav?.price ?: 0.0,
                navQty = nav?.qty ?: 0,
                scannedQty = (nav?.actualQty ?: 0),
                variance = (nav?.actualQty ?: 0) - (nav?.qty ?: 0)
            )
        }
    }

    fun searchInventory(query: String) {
        viewModelScope.launch {
            val storeId = _setupState.value.selectedStore?.storeId ?: return@launch
            val results = invRepo.searchNav(query, storeId)
            _navList.value = results
        }
    }

    fun saveSetting() {
        viewModelScope.launch {
            val state = _setupState.value
            val setting = com.rgmc.inventory.data.local.entity.SettingEntity(
                1, state.encoder, state.selectedBrand?.brandId ?: 0,
                state.selectedStore?.storeId ?: 0, state.selectedLocation?.locationId ?: 0,
                0, state.rack, "", "", state.selectedCutOff?.cutOffDate ?: ""
            )
            app.database.settingDao().insertOrUpdate(setting)
        }
    }

    suspend fun loadSetting(): SettingEntity? = app.database.settingDao().getSetting()
}
