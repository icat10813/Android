package com.example.data.repository

import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    // Products
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
    suspend fun getProductByCode(code: String): Product?
    suspend fun createProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(id: String)

    // Categories
    fun getAllCategories(): Flow<List<Category>>
    suspend fun createCategory(category: Category)
    suspend fun deleteCategory(id: String)

    // Units
    fun getAllUnits(): Flow<List<ProductUnit>>

    // Suppliers
    fun getAllSuppliers(): Flow<List<Supplier>>
    suspend fun createSupplier(supplier: Supplier)
    suspend fun deleteSupplier(id: String)

    // Customers
    fun getAllCustomers(): Flow<List<Customer>>
    suspend fun createCustomer(customer: Customer)
    suspend fun deleteCustomer(id: String)

    // Warehouses
    fun getAllWarehouses(): Flow<List<Warehouse>>
    suspend fun createWarehouse(warehouse: Warehouse)
    suspend fun deleteWarehouse(id: String)

    // Transactions
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun processPosTransaction(transaction: Transaction)
    suspend fun syncPendingTransactions()

    // Stock Movements (In & Out)
    fun getAllStockMovements(): Flow<List<StockMovement>>
    suspend fun processStockIn(movement: StockMovement)
    suspend fun processStockOut(movement: StockMovement)

    // Stock Adjustments
    fun getAllStockAdjustments(): Flow<List<StockAdjustment>>
    suspend fun processStockAdjustment(adjustment: StockAdjustment)

    // Dashboard & Reports
    suspend fun getDashboardSummary(): DashboardSummary
    suspend fun getSalesChartData(): List<SalesChartData>

    // Seed Data Initialization
    suspend fun initializeSeedDataIfNeeded()
}
