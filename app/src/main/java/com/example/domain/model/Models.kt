package com.example.domain.model

enum class UserRole {
    ADMIN,
    KASIR,
    STAFF_GUDANG
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val avatarUrl: String? = null,
    val token: String? = null
)

data class Product(
    val id: String,
    val name: String,
    val code: String,
    val sku: String,
    val barcode: String,
    val qrCode: String,
    val categoryId: String,
    val categoryName: String = "",
    val unitId: String,
    val unitName: String = "",
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stock: Int,
    val minimumStock: Int,
    val warehouseId: String,
    val warehouseName: String = "",
    val image: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean get() = stock in 1..minimumStock
    val isOutOfStock: Boolean get() = stock <= 0
}

data class Category(
    val id: String,
    val name: String,
    val description: String = "",
    val productCount: Int = 0
)

data class ProductUnit(
    val id: String,
    val name: String,
    val symbol: String
)

data class Supplier(
    val id: String,
    val code: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val notes: String = ""
)

data class Customer(
    val id: String,
    val code: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String
)

data class Warehouse(
    val id: String,
    val code: String,
    val name: String,
    val address: String,
    val pic: String,
    val status: String = "ACTIVE"
)

enum class MovementType {
    IN,
    OUT
}

data class StockMovement(
    val id: String,
    val transactionNo: String,
    val type: MovementType,
    val date: Long,
    val warehouseId: String,
    val warehouseName: String = "",
    val entityName: String = "", // Supplier or Customer name
    val items: List<StockMovementItem>,
    val totalAmount: Double,
    val notes: String = ""
)

data class StockMovementItem(
    val productId: String,
    val productName: String,
    val productCode: String,
    val qty: Int,
    val price: Double,
    val subtotal: Double
)

enum class PaymentMethod {
    CASH,
    TRANSFER,
    QRIS,
    E_WALLET,
    DEBIT,
    CREDIT
}

data class TransactionItem(
    val productId: String,
    val productName: String,
    val productCode: String,
    val price: Double,
    val qty: Int,
    val subtotal: Double = price * qty
)

enum class SyncStatus {
    SYNCED,
    PENDING
}

data class Transaction(
    val id: String, // POS-YYYYMMDD-XXXX
    val date: Long,
    val cashierName: String,
    val cashierId: String = "",
    val customerName: String = "Pelanggan Umum",
    val items: List<TransactionItem>,
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val additionalFee: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val paymentMethod: PaymentMethod,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val notes: String = ""
)

enum class AdjustmentReason {
    BARANG_RUSAK,
    BARANG_HILANG,
    STOCK_OPNAME,
    KESALAHAN_INPUT,
    LAINNYA
}

data class StockAdjustment(
    val id: String,
    val date: Long,
    val productId: String,
    val productName: String,
    val productCode: String,
    val warehouseId: String,
    val warehouseName: String,
    val systemStock: Int,
    val actualStock: Int,
    val difference: Int,
    val reason: AdjustmentReason,
    val notes: String = "",
    val createdBy: String = ""
)

data class DashboardSummary(
    val totalProducts: Int = 0,
    val totalStock: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val stockInToday: Int = 0,
    val stockOutToday: Int = 0,
    val totalSalesToday: Double = 0.0,
    val totalTransactionsToday: Int = 0
)

data class SalesChartData(
    val label: String,
    val sales: Double,
    val stockIn: Int,
    val stockOut: Int
)
