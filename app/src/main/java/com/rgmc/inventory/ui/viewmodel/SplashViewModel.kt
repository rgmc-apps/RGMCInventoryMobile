package com.rgmc.inventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rgmc.inventory.RGMCApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SplashState(
    val progress: Int = 0,
    val status: String = "Connecting...",
    val isComplete: Boolean = false
)

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as RGMCApp
    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state

    fun loadData() {
        viewModelScope.launch {
            try {
                step(10, "Loading brands...")
                app.brandRepository.fetchAndCacheBrands()

                step(25, "Loading customers...")
                app.storeRepository.fetchAndCacheCustomers()

                step(40, "Loading stores...")
                app.storeRepository.fetchAndCacheStores()

                step(55, "Loading coordinators...")
                app.brandRepository.fetchAndCacheCoordinators()

                step(65, "Loading cut-off dates...")
                app.storeRepository.fetchAndCacheCutOffs()

                step(75, "Loading locations...")
                app.storeRepository.fetchAndCacheLocations()

                step(88, "Loading categories...")
                app.brandRepository.fetchAndCacheItemGroups()
                app.brandRepository.fetchAndCacheCategories()

                step(100, "Ready")
                delay(300)
                _state.value = _state.value.copy(isComplete = true)
            } catch (e: Exception) {
                _state.value = SplashState(100, "Offline — continuing with cached data")
                delay(1200)
                _state.value = _state.value.copy(isComplete = true)
            }
        }
    }

    private fun step(progress: Int, status: String) {
        _state.value = SplashState(progress, status)
    }
}
