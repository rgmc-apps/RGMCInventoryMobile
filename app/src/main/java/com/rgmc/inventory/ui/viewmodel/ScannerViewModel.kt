package com.rgmc.inventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.rgmc.inventory.RGMCApp
import com.rgmc.inventory.data.local.entity.*
import com.rgmc.inventory.data.remote.ApiException
import com.rgmc.inventory.util.ErrorReport
import com.rgmc.inventory.util.ErrorReporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    val format: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val navQty: Int = 0,
    val scannedQty: Int = 0,
    val variance: Int = 0,
    val isAccepted: Boolean = true,
    val inNavList: Boolean = true,
    val rejectionReason: String = ""
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

    private val _errorReport = MutableSharedFlow<ErrorReport>()
    val errorReport: SharedFlow<ErrorReport> = _errorReport.asSharedFlow()

    private var currentSessionId: Int = 0

    val openSessions: StateFlow<List<ScanningSessionEntity>> =
        app.database.scanningSessionDao().getOpenSessionsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadInitialData() {
        viewModelScope.launch {
            _setupState.update { ScannerSetupState(isLoading = true) }
            val results = listOf(
                brandRepo.fetchAndCacheBrands().also { it.emitErrorReport("Load brands") },
                storeRepo.fetchAndCacheStores().also { it.emitErrorReport("Load stores") },
                storeRepo.fetchAndCacheCustomers().also { it.emitErrorReport("Load customers") },
                brandRepo.fetchAndCacheCoordinators().also { it.emitErrorReport("Load coordinators") },
                storeRepo.fetchAndCacheCutOffs().also { it.emitErrorReport("Load cut-off dates") },
                storeRepo.fetchAndCacheLocations().also { it.emitErrorReport("Load locations") },
                brandRepo.fetchAndCacheItemGroups().also { it.emitErrorReport("Load item groups") },
                brandRepo.fetchAndCacheCategories().also { it.emitErrorReport("Load categories") }
            )
            val firstError = results.firstOrNull { it.isFailure }?.exceptionOrNull()
            val brands = brandRepo.getBrandsLocal()
            val locations = storeRepo.getAllLocations()
            _setupState.update { it.copy(brands = brands, locations = locations, isLoading = false, error = firstError?.message) }
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
            invRepo.fetchAndCacheNavList(storeId).emitErrorReport("Load NAV list for store $storeId")
            invRepo.getNavListFlow(storeId).collect { list -> _navList.value = list }
        }
    }

    fun importData(storeId: Int, cutOffDate: String) {
        viewModelScope.launch {
            _setupState.update { it.copy(isLoading = true) }
            val r1 = invRepo.fetchAndCacheBarcodeList(storeId, cutOffDate)
                .also { it.emitErrorReport("Import barcode list — store $storeId, cutOff $cutOffDate") }
            val r2 = invRepo.fetchAndCacheInventoryList(storeId, cutOffDate)
                .also { it.emitErrorReport("Import inventory list — store $storeId, cutOff $cutOffDate") }
            _setupState.update { it.copy(isLoading = false) }
            if (r1.isFailure || r2.isFailure) {
                val errMsg = (r1.exceptionOrNull() ?: r2.exceptionOrNull())?.message ?: "Unknown error"
                _message.emit("Import failed: $errMsg")
            } else {
                _message.emit("Import successful")
            }
        }
    }

    fun processBarcodeScan(barcode: String, format: String, qty: Int, deviceId: String) {
        viewModelScope.launch {
            val state = _setupState.value
            val storeId = state.selectedStore?.storeId ?: return@launch
            val brandId = state.selectedBrand?.brandId ?: 0
            val cutOff = state.selectedCutOff?.cutOffDate ?: return@launch
            val locId = state.selectedLocation?.locationId ?: 0
            val type = format.replace("-", "_").replace(" ", "_").uppercase()
            invRepo.saveBarcodeScan(barcode, type, storeId, brandId, cutOff, locId, state.rack, qty, state.encoder, deviceId)
            val nav = invRepo.getNavByBarcode(barcode, storeId)
            _lastScan.value = ScanResultState(
                barcode = barcode,
                format = format,
                description = nav?.description ?: "",
                price = nav?.price ?: 0.0,
                navQty = nav?.qty ?: 0,
                scannedQty = nav?.actualQty ?: 0,
                variance = (nav?.actualQty ?: 0) - (nav?.qty ?: 0),
                isAccepted = true,
                inNavList = nav != null
            )
            updateSessionActivity()
        }
    }

    fun reportScanRejected(barcode: String, format: String, reason: String) {
        _lastScan.value = ScanResultState(
            barcode = barcode,
            format = format,
            isAccepted = false,
            rejectionReason = reason
        )
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
            val setting = SettingEntity(
                1, state.encoder, state.selectedBrand?.brandId ?: 0,
                state.selectedStore?.storeId ?: 0, state.selectedLocation?.locationId ?: 0,
                0, state.rack, "", "", state.selectedCutOff?.cutOffDate ?: ""
            )
            app.database.settingDao().insertOrUpdate(setting)
        }
    }

    suspend fun loadSetting(): SettingEntity? = app.database.settingDao().getSetting()

    fun createSession() {
        viewModelScope.launch {
            val state = _setupState.value
            val now = nowTimestamp()
            val session = ScanningSessionEntity(
                brandId = state.selectedBrand?.brandId ?: 0,
                brandName = state.selectedBrand?.name ?: "",
                customerId = state.selectedCustomer?.customerId ?: 0,
                customerName = state.selectedCustomer?.customerName ?: "",
                storeId = state.selectedStore?.storeId ?: 0,
                storeName = state.selectedStore?.name ?: "",
                cutOffDate = state.selectedCutOff?.cutOffDate ?: "",
                locationId = state.selectedLocation?.locationId ?: 0,
                locationName = state.selectedLocation?.locationName ?: "",
                rack = state.rack,
                encoder = state.encoder,
                coordinatorName = state.selectedCoordinator?.name ?: "",
                createdAt = now,
                lastModifiedAt = now
            )
            currentSessionId = app.database.scanningSessionDao().insert(session).toInt()
        }
    }

    fun updateSessionActivity() {
        if (currentSessionId == 0) return
        viewModelScope.launch {
            val count = _navList.value.sumOf { it.actualQty }
            app.database.scanningSessionDao().updateScanCount(currentSessionId, count, nowTimestamp())
        }
    }

    fun resumeSession(session: ScanningSessionEntity) {
        currentSessionId = session.sessionId
        val brand = BrandEntity(brandId = session.brandId, name = session.brandName)
        val store = CustomerStoreEntity(
            storeId = session.storeId, name = session.storeName,
            brandId = session.brandId, customerName = session.customerName
        )
        val cutOff = StoreInventoryCutOffEntity(
            storeCutOff = "${session.storeId}_${session.cutOffDate}",
            storeId = session.storeId, cutOffDate = session.cutOffDate
        )
        val location = StoreInventoryLocationEntity(
            locationId = session.locationId, locationName = session.locationName
        )
        _setupState.update {
            ScannerSetupState(
                selectedBrand = brand,
                selectedStore = store,
                selectedCutOff = cutOff,
                selectedLocation = location,
                rack = session.rack,
                encoder = session.encoder
            )
        }
        loadNavList(session.storeId)
    }

    fun deleteSession(session: ScanningSessionEntity) {
        viewModelScope.launch {
            app.database.scanningSessionDao().delete(session)
        }
    }

    private fun nowTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    private suspend fun Result<*>.emitErrorReport(context: String) {
        val e = exceptionOrNull() ?: return
        val report = if (e is ApiException) {
            ErrorReport(e.endpoint, e.statusCode, e.errorBody, e.requestBody, nowTimestamp(), context)
        } else {
            ErrorReport("Unknown", -1, e.message ?: "Unknown error", "", nowTimestamp(), context)
        }
        _errorReport.emit(report)
    }
}
