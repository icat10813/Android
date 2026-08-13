package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val sku: String,
    val barcode: String,
    val qrCode: String,
    val categoryId: String,
    val categoryName: String,
    val unitId: String,
    val unitName: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stock: Int,
    val minimumStock: Int,
    val warehouseId: String,
    val warehouseName: String,
    val image: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPendingSync: Boolean = false
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String
)

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val notes: String
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String
)

@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val address: String,
    val pic: String,
    val status: String
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val cashierName: String,
    val cashierId: String,
    val customerName: String,
    val itemsJson: String, // Stored as serialized JSON string
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val additionalFee: Double,
    val grandTotal: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val paymentMethod: String,
    val syncStatus: String, // SYNCED, PENDING
    val notes: String
)

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val transactionNo: String,
    val type: String, // IN, OUT
    val date: Long,
    val warehouseId: String,
    val warehouseName: String,
    val entityName: String,
    val itemsJson: String,
    val totalAmount: Double,
    val notes: String
)

@Entity(tableName = "stock_adjustments")
data class StockAdjustmentEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val productId: String,
    val productName: String,
    val productCode: String,
    val warehouseId: String,
    val warehouseName: String,
    val systemStock: Int,
    val actualStock: Int,
    val difference: Int,
    val reason: String,
    val notes: String,
    val createdBy: String
)
