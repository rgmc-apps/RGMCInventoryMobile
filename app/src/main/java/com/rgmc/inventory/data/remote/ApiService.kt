package com.rgmc.inventory.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("api/brand")
    suspend fun getBrands(): Response<List<BrandDto>>

    @GET("api/brand/coordinator")
    suspend fun getBrandCoordinators(): Response<List<BrandCoordinatorDto>>

    @GET("api/customerstore")
    suspend fun getCustomerStores(): Response<List<CustomerStoreDto>>

    @GET("api/store/customer")
    suspend fun getCustomers(): Response<List<CustomerDto>>

    @GET("api/storeinventorylocation")
    suspend fun getLocations(): Response<List<StoreInventoryLocationDto>>

    @GET("api/itemgroup")
    suspend fun getItemGroups(): Response<List<ItemGroupDto>>

    @GET("api/category")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("api/product/{categoryId}")
    suspend fun getProductsByCategory(@Path("categoryId") categoryId: Int): Response<List<ProductImageDto>>

    @GET("api/product/detail/{stockNumber}")
    suspend fun getProductDetail(@Path("stockNumber") stockNumber: String): Response<List<ProductImageDto>>

    @GET("api/storeinventory/navlist/{storeId}")
    suspend fun getNavList(@Path("storeId") storeId: Int): Response<List<StoreInventoryNAVDto>>

    @POST("api/storeinventory/barcodelist")
    suspend fun getBarcodeList(@Body request: BarcodeListRequestDto): Response<List<BarcodeDto>>

    @POST("api/storeinventory/invlist")
    suspend fun getInventoryList(@Body request: StoreInventoryRequestDto): Response<List<StoreInventoryDto>>

    @POST("api/storeinventory/save/personnel")
    suspend fun savePersonnel(@Body personnel: List<StoreInventoryPersonnelDto>): Response<Unit>

    @POST("api/storeinventory/save/inventory")
    suspend fun saveInventory(@Body inventories: List<StoreInventoryDto>): Response<Unit>

    @POST("api/storeinventory/save/barcode")
    suspend fun saveBarcodes(@Body barcodes: List<BarcodeDto>): Response<Unit>

    @GET("api/storeinventory/appversion")
    suspend fun getAppVersion(): Response<SystemSettingDto>

    @GET("api/storeinventorycutoff")
    suspend fun getAllCutOffs(): Response<List<StoreInventoryCutOffDto>>

    @GET("api/storeinventorycutoff/{storeId}")
    suspend fun getCutOffsByStore(@Path("storeId") storeId: Int): Response<List<StoreInventoryCutOffDto>>

    @POST("api/storeinventorycutoff/create")
    suspend fun createCutOff(@Body request: StoreInventoryCutOffRequestDto): Response<Unit>

    @POST("api/storeinventorycutoff/close")
    suspend fun closeCutOff(@Body request: StoreInventoryCutOffRequestDto): Response<Unit>

    @GET("api/barcode")
    suspend fun getAllBarcodes(): Response<List<BarcodeDto>>

    @POST("api/barcode")
    suspend fun postBarcodes(@Body barcodes: List<BarcodeDto>): Response<Unit>
}
