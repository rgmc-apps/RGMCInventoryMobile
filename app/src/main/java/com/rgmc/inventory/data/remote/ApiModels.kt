package com.rgmc.inventory.data.remote

data class BrandDto(
    val brandId: Int = 0,
    val brandCode: String = "",
    val companyId: Int = 0,
    val initials: String = "",
    val name: String = "",
    val percentage: Double = 0.0,
    val remark: String = "",
    val isActive: Boolean = true,
    val createBy: String = "",
    val createDate: String = ""
)

data class CategoryDto(
    val lineNumber: Int = 0,
    val categoryId: Int = 0,
    val categoryCode: String = "",
    val name: String = "",
    val brandId: Int = 0,
    val itemGroupId: Int = 0,
    val remark: String = "",
    val isActive: Boolean = true
)

data class ItemGroupDto(
    val itemGroupId: Int = 0,
    val itemGroupCode: String = "",
    val name: String = "",
    val remark: String = "",
    val isRW: Boolean = false,
    val isActive: Boolean = true
)

data class CustomerStoreDto(
    val storeId: Int = 0,
    val storeTypeId: Int = 0,
    val customerId: Int = 0,
    val brandId: Int = 0,
    val customerName: String = "",
    val storeCode: String = "",
    val name: String = "",
    val storeTypeName: String = "",
    val storeName: String = "",
    val brandName: String = ""
)

data class CustomerDto(
    val customerId: Int = 0,
    val brandId: Int = 0,
    val customerName: String = ""
)

data class BrandCoordinatorDto(
    val brandId: Int = 0,
    val coorCode: String = "",
    val name: String = "",
    val remark: String = "",
    val isActive: Boolean = true,
    val createBy: String = "",
    val createDate: String = ""
)

data class StoreInventoryLocationDto(
    val locationId: Int = 0,
    val locationName: String = "",
    val createBy: String = "",
    val createDate: String = ""
)

data class StoreInventoryCutOffDto(
    val storeCutOff: String = "",
    val storeId: Int = 0,
    val cutOffDate: String = "",
    val isActive: Boolean = true,
    val createBy: String = "",
    val createDate: String = "",
    val updateBy: String = "",
    val updateDate: String = ""
)

data class StoreInventoryNAVDto(
    val barcode: String = "",
    val storeId: Int = 0,
    val brandId: Int = 0,
    val description: String = "",
    val price: Double = 0.0,
    val qty: Int = 0,
    val actualQty: Int = 0,
    val totalQty: Int = 0,
    val createBy: String = "",
    val createDate: String = ""
)

data class BarcodeDto(
    val id: Int = 0,
    val cutOffDate: String = "",
    val storeId: Int = 0,
    val brandId: Int = 0,
    val rack: Int = 0,
    val type: String = "EAN_13",
    val text: String = "",
    val locationId: Int = 0,
    val createBy: String = "",
    val createDate: String = ""
)

data class StoreInventoryDto(
    val inventoryDate: String = "",
    val cutOffDate: String = "",
    val storeId: Int = 0,
    val barcode: String = "",
    val locationId: Int = 0,
    val rack: Int = 0,
    val deviceId: String = "",
    val brandId: Int = 0,
    val qty: Int = 1,
    val createBy: String = "",
    val createDate: String = ""
)

data class StoreInventoryPersonnelDto(
    val inventoryDate: String = "",
    val cutOffDate: String = "",
    val storeId: Int = 0,
    val storePersonnel: String = "",
    val signature: String = "",
    val createBy: String = "",
    val createDate: String = ""
)

data class ProductImageDto(
    val stockNumber: String = "",
    val productDesignId: Int = 0,
    val brandId: Int = 0,
    val lineNumber: Int = 0,
    val categoryId: Int = 0,
    val imageData: String? = null,
    val description: String = "",
    val price: Double = 0.0
)

data class SystemSettingDto(
    val setCode: String = "",
    val setName: String = "",
    val setValue: String = ""
)

data class StoreInventoryRequestDto(
    val cutOffDate: String,
    val storeId: Int
)

data class StoreInventoryCutOffRequestDto(
    val storeId: Int,
    val cutOffDate: String,
    val createBy: String
)

data class BarcodeListRequestDto(
    val storeId: Int,
    val cutOffDate: String,
    val type: String = "EAN_13",
    val text: String = "",
    val createBy: String = ""
)

class ApiException(
    message: String,
    val statusCode: Int,
    val errorBody: String,
    val endpoint: String,
    val requestBody: String = ""
) : Exception(message)
