package com.rgmc.inventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brands")
data class BrandEntity(
    @PrimaryKey val brandId: Int = 0,
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

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val categoryId: Int = 0,
    val lineNumber: Int = 0,
    val categoryCode: String = "",
    val name: String = "",
    val brandId: Int = 0,
    val itemGroupId: Int = 0,
    val remark: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "item_groups")
data class ItemGroupEntity(
    @PrimaryKey val itemGroupId: Int = 0,
    val itemGroupCode: String = "",
    val name: String = "",
    val remark: String = "",
    val isRW: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "customer_stores")
data class CustomerStoreEntity(
    @PrimaryKey val storeId: Int = 0,
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

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerBrand: String = "",
    val customerId: Int = 0,
    val brandId: Int = 0,
    val customerName: String = ""
)

@Entity(tableName = "brand_coordinators")
data class BrandCoordinatorEntity(
    @PrimaryKey val brandCoor: String = "",
    val brandId: Int = 0,
    val coorCode: String = "",
    val name: String = "",
    val remark: String = "",
    val isActive: Boolean = true,
    val createBy: String = "",
    val createDate: String = ""
)

@Entity(tableName = "cut_off_dates")
data class CutOffDateEntity(
    @PrimaryKey val cutOff: String = ""
)

@Entity(tableName = "store_inventory_locations")
data class StoreInventoryLocationEntity(
    @PrimaryKey val locationId: Int = 0,
    val locationName: String = "",
    val createBy: String = "",
    val createDate: String = ""
)

@Entity(tableName = "store_inventory_cutoffs")
data class StoreInventoryCutOffEntity(
    @PrimaryKey val storeCutOff: String = "",
    val storeId: Int = 0,
    val cutOffDate: String = "",
    val isActive: Boolean = true,
    val createBy: String = "",
    val createDate: String = "",
    val updateBy: String = "",
    val updateDate: String = ""
)

@Entity(tableName = "store_inventory_nav")
data class StoreInventoryNAVEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
) {
    val variance: Int get() = actualQty - qty
    val totalVariance: Int get() = totalQty - qty
}

@Entity(tableName = "store_inventories")
data class StoreInventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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

@Entity(tableName = "barcodes")
data class BarcodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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

@Entity(tableName = "store_inventory_personnel")
data class StoreInventoryPersonnelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inventoryDate: String = "",
    val cutOffDate: String = "",
    val storeId: Int = 0,
    val storePersonnel: String = "",
    val signature: ByteArray? = null,
    val createBy: String = "",
    val createDate: String = ""
)

@Entity(tableName = "product_images")
data class ProductImageEntity(
    @PrimaryKey val stockNumber: String = "",
    val productDesignId: Int = 0,
    val brandId: Int = 0,
    val lineNumber: Int = 0,
    val categoryId: Int = 0,
    val imageData: ByteArray? = null,
    val description: String = "",
    val price: Double = 0.0
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val text: String = "",
    val date: String = ""
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val id: Int = 1,
    val encoder: String = "",
    val brandId: Int = 0,
    val storeId: Int = 0,
    val locationId: Int = 0,
    val categoryId: Int = 0,
    val rack: Int = 0,
    val inventoryDate: String = "",
    val invDate: String = "",
    val cutOffDate: String = ""
)

@Entity(tableName = "scanning_sessions")
data class ScanningSessionEntity(
    @PrimaryKey(autoGenerate = true) val sessionId: Int = 0,
    val brandId: Int = 0,
    val brandName: String = "",
    val customerId: Int = 0,
    val customerName: String = "",
    val storeId: Int = 0,
    val storeName: String = "",
    val cutOffDate: String = "",
    val locationId: Int = 0,
    val locationName: String = "",
    val rack: Int = 1,
    val encoder: String = "",
    val coordinatorName: String = "",
    val createdAt: String = "",
    val lastModifiedAt: String = "",
    val status: String = "open",
    val totalScanned: Int = 0
)
