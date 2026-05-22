package com.rgmc.inventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.rgmc.inventory.RGMCApp
import com.rgmc.inventory.data.local.entity.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductSetupState(
    val brands: List<BrandEntity> = emptyList(),
    val itemGroups: List<ItemGroupEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedBrand: BrandEntity? = null,
    val selectedItemGroup: ItemGroupEntity? = null,
    val selectedCategory: CategoryEntity? = null,
    val isLoading: Boolean = false
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as RGMCApp
    private val brandRepo = app.brandRepository
    private val productRepo = app.productRepository

    private val _state = MutableStateFlow(ProductSetupState())
    val state: StateFlow<ProductSetupState> = _state.asStateFlow()

    private val _products = MutableStateFlow<List<ProductImageEntity>>(emptyList())
    val products: StateFlow<List<ProductImageEntity>> = _products.asStateFlow()

    fun loadBrands() {
        viewModelScope.launch {
            val brands = brandRepo.getBrandsLocal()
            val groups = brandRepo.getItemGroupsLocal()
            _state.update { it.copy(brands = brands, itemGroups = groups, categories = emptyList(), selectedBrand = null, selectedItemGroup = null, selectedCategory = null) }
        }
    }

    fun onBrandSelected(brand: BrandEntity) {
        viewModelScope.launch {
            val cats = brandRepo.getCategoriesByBrand(brand.brandId)
            _state.update { it.copy(selectedBrand = brand, categories = cats, selectedCategory = null) }
        }
    }

    fun onItemGroupSelected(ig: ItemGroupEntity) {
        viewModelScope.launch {
            val brandId = _state.value.selectedBrand?.brandId ?: return@launch
            val cats = brandRepo.getCategoriesByBrandAndItemGroup(brandId, ig.itemGroupId)
            _state.update { it.copy(selectedItemGroup = ig, categories = cats) }
        }
    }

    fun onCategorySelected(c: CategoryEntity) { _state.update { it.copy(selectedCategory = c) } }

    fun loadProducts(categoryId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = productRepo.fetchProductsByCategory(categoryId)
            _products.value = result.getOrDefault(productRepo.getProductsByCategory(categoryId))
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _products.value = productRepo.searchProducts(query)
        }
    }
}
