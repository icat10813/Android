package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.ApiService
import com.example.domain.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class InventoryRepositoryImpl(
    private val db: AppDatabase,
    private val apiService: ApiService
) : InventoryRepository {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val transactionItemsAdapter = moshi.adapter<List<TransactionItem>>(
        Types.newParameterizedType(List::class.java, TransactionItem::class.java)
    )
    private val movementItemsAdapter = moshi.adapter<List<StockMovementItem>>(
        Types.newParameterizedType(List::class.java, StockMovementItem::class.java)
    )

    override fun getAllProducts(): Flow<List<Product>> {
        return db.productDao().getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) {
        db.productDao().getProductById(id)?.toDomain()
    }

    override suspend fun getProductByCode(code: String): Product? = withContext(Dispatchers.IO) {
        db.productDao().getProductByCode(code)?.toDomain()
    }

    override suspend fun createProduct(product: Product) {
        withContext(Dispatchers.IO) {
            val entity = product.toEntity()
            db.productDao().insertProduct(entity)
            try {
                apiService.createProduct(product)
            } catch (_: Exception) {
                // Handled offline in Room
            }
        }
    }

    override suspend fun updateProduct(product: Product) {
        withContext(Dispatchers.IO) {
            val entity = product.toEntity()
            db.productDao().updateProduct(entity)
            try {
                apiService.updateProduct(product.id, product)
            } catch (_: Exception) {
                // Handled offline in Room
            }
        }
    }

    override suspend fun deleteProduct(id: String) {
        withContext(Dispatchers.IO) {
            db.productDao().deleteProductById(id)
            try {
                apiService.deleteProduct(id)
            } catch (_: Exception) {
                // Handled offline
            }
        }
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return db.categoryDao().getAllCategories().map { entities ->
            entities.map { Category(id = it.id, name = it.name, description = it.description) }
        }
    }

    override suspend fun createCategory(category: Category) {
        withContext(Dispatchers.IO) {
            db.categoryDao().insertCategory(CategoryEntity(category.id, category.name, category.description))
        }
    }

    override suspend fun deleteCategory(id: String) {
        withContext(Dispatchers.IO) {
            db.categoryDao().deleteCategoryById(id)
        }
    }

    override fun getAllUnits(): Flow<List<ProductUnit>> {
        return db.unitDao().getAllUnits().map { entities ->
            entities.map { ProductUnit(id = it.id, name = it.name, symbol = it.symbol) }
        }
    }

    override fun getAllSuppliers(): Flow<List<Supplier>> {
        return db.supplierDao().getAllSuppliers().map { entities ->
            entities.map { Supplier(it.id, it.code, it.name, it.phone, it.email, it.address, it.notes) }
        }
    }

    override suspend fun createSupplier(supplier: Supplier) {
        withContext(Dispatchers.IO) {
            db.supplierDao().insertSupplier(
                SupplierEntity(supplier.id, supplier.code, supplier.name, supplier.phone, supplier.email, supplier.address, supplier.notes)
            )
        }
    }

    override suspend fun deleteSupplier(id: String) {
        withContext(Dispatchers.IO) {
            db.supplierDao().deleteSupplierById(id)
        }
    }

    override fun getAllCustomers(): Flow<List<Customer>> {
        return db.customerDao().getAllCustomers().map { entities ->
            entities.map { Customer(it.id, it.code, it.name, it.phone, it.email, it.address) }
        }
    }

    override suspend fun createCustomer(customer: Customer) {
        withContext(Dispatchers.IO) {
            db.customerDao().insertCustomer(
                CustomerEntity(customer.id, customer.code, customer.name, customer.phone, customer.email, customer.address)
            )
        }
    }

    override suspend fun deleteCustomer(id: String) {
        withContext(Dispatchers.IO) {
            db.customerDao().deleteCustomerById(id)
        }
    }

    override fun getAllWarehouses(): Flow<List<Warehouse>> {
        return db.warehouseDao().getAllWarehouses().map { entities ->
            entities.map { Warehouse(it.id, it.code, it.name, it.address, it.pic, it.status) }
        }
    }

    override suspend fun createWarehouse(warehouse: Warehouse) {
        withContext(Dispatchers.IO) {
            db.warehouseDao().insertWarehouse(
                WarehouseEntity(warehouse.id, warehouse.code, warehouse.name, warehouse.address, warehouse.pic, warehouse.status)
            )
        }
    }

    override suspend fun deleteWarehouse(id: String) {
        withContext(Dispatchers.IO) {
            db.warehouseDao().deleteWarehouseById(id)
        }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return db.transactionDao().getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? = withContext(Dispatchers.IO) {
        db.transactionDao().getTransactionById(id)?.toDomain()
    }

    override suspend fun processPosTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            // 1. Insert transaction into local db
            val entity = transaction.toEntity()
            db.transactionDao().insertTransaction(entity)

            // 2. Reduce stock for each item
            transaction.items.forEach { item ->
                db.productDao().updateStock(item.productId, -item.qty)
            }

            // 3. Try online sync
            try {
                val res = apiService.createTransaction(transaction)
                if (res.isSuccessful && res.body()?.success == true) {
                    db.transactionDao().markSynced(transaction.id)
                }
            } catch (_: Exception) {
                // Keep status as PENDING for offline sync later
            }
        }
    }

    override suspend fun syncPendingTransactions() {
        withContext(Dispatchers.IO) {
            val pending = db.transactionDao().getPendingTransactions()
            pending.forEach { entity ->
                val domain = entity.toDomain()
                try {
                    val res = apiService.createTransaction(domain)
                    if (res.isSuccessful && res.body()?.success == true) {
                        db.transactionDao().markSynced(domain.id)
                    }
                } catch (_: Exception) {
                    // Will retry next time online
                }
            }
        }
    }

    override fun getAllStockMovements(): Flow<List<StockMovement>> {
        return db.stockMovementDao().getAllMovements().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun processStockIn(movement: StockMovement) {
        withContext(Dispatchers.IO) {
            val entity = movement.toEntity()
            db.stockMovementDao().insertMovement(entity)
            movement.items.forEach { item ->
                db.productDao().updateStock(item.productId, item.qty)
            }
            try {
                apiService.stockIn(movement)
            } catch (_: Exception) {}
        }
    }

    override suspend fun processStockOut(movement: StockMovement) {
        withContext(Dispatchers.IO) {
            val entity = movement.toEntity()
            db.stockMovementDao().insertMovement(entity)
            movement.items.forEach { item ->
                db.productDao().updateStock(item.productId, -item.qty)
            }
            try {
                apiService.stockOut(movement)
            } catch (_: Exception) {}
        }
    }

    override fun getAllStockAdjustments(): Flow<List<StockAdjustment>> {
        return db.stockAdjustmentDao().getAllAdjustments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun processStockAdjustment(adjustment: StockAdjustment) {
        withContext(Dispatchers.IO) {
            val entity = adjustment.toEntity()
            db.stockAdjustmentDao().insertAdjustment(entity)

            val product = db.productDao().getProductById(adjustment.productId)
            if (product != null) {
                val updated = product.copy(stock = adjustment.actualStock, updatedAt = System.currentTimeMillis())
                db.productDao().updateProduct(updated)
            }
            try {
                apiService.adjustStock(adjustment)
            } catch (_: Exception) {}
        }
    }

    override suspend fun getDashboardSummary(): DashboardSummary = withContext(Dispatchers.IO) {
        val products = db.productDao().getAllProducts().first().map { it.toDomain() }
        val transactions = db.transactionDao().getAllTransactions().first().map { it.toDomain() }
        val movements = db.stockMovementDao().getAllMovements().first().map { it.toDomain() }

        val totalProducts = products.size
        val totalStock = products.sumOf { it.stock }
        val lowStockCount = products.count { it.isLowStock }
        val outOfStockCount = products.count { it.isOutOfStock }

        val todayStart = getStartOfDayTimestamp()
        val todayTransactions = transactions.filter { it.date >= todayStart }
        val totalSalesToday = todayTransactions.sumOf { it.grandTotal }
        val totalTransactionsToday = todayTransactions.size

        val todayMovements = movements.filter { it.date >= todayStart }
        val stockInToday = todayMovements.filter { it.type == MovementType.IN }.sumOf { m -> m.items.sumOf { it.qty } }
        val stockOutToday = todayMovements.filter { it.type == MovementType.OUT }.sumOf { m -> m.items.sumOf { it.qty } } +
                todayTransactions.sumOf { t -> t.items.sumOf { it.qty } }

        DashboardSummary(
            totalProducts = totalProducts,
            totalStock = totalStock,
            lowStockCount = lowStockCount,
            outOfStockCount = outOfStockCount,
            stockInToday = stockInToday,
            stockOutToday = stockOutToday,
            totalSalesToday = totalSalesToday,
            totalTransactionsToday = totalTransactionsToday
        )
    }

    override suspend fun getSalesChartData(): List<SalesChartData> = withContext(Dispatchers.IO) {
        listOf(
            SalesChartData("Sen", 1200000.0, 45, 30),
            SalesChartData("Sel", 1850000.0, 20, 42),
            SalesChartData("Rab", 2400000.0, 60, 50),
            SalesChartData("Kam", 1950000.0, 15, 38),
            SalesChartData("Jum", 3100000.0, 80, 65),
            SalesChartData("Sab", 4200000.0, 50, 92),
            SalesChartData("Min", 3800000.0, 30, 85)
        )
    }

    override suspend fun initializeSeedDataIfNeeded() {
        withContext(Dispatchers.IO) {
            val count = db.productDao().getAllProducts().first().size
            if (count == 0) {
                db.categoryDao().insertCategories(MockData.initialCategories.map { CategoryEntity(it.id, it.name, it.description) })
                db.unitDao().insertUnits(MockData.initialUnits.map { UnitEntity(it.id, it.name, it.symbol) })
                db.supplierDao().insertSuppliers(MockData.initialSuppliers.map { SupplierEntity(it.id, it.code, it.name, it.phone, it.email, it.address, it.notes) })
                db.customerDao().insertCustomers(MockData.initialCustomers.map { CustomerEntity(it.id, it.code, it.name, it.phone, it.email, it.address) })
                db.warehouseDao().insertWarehouses(MockData.initialWarehouses.map { WarehouseEntity(it.id, it.code, it.name, it.address, it.pic, it.status) })
                db.productDao().insertProducts(MockData.initialProducts.map { it.toEntity() })
                db.transactionDao().insertTransactions(MockData.initialTransactions.map { it.toEntity() })
            }
        }
    }

    private fun ProductEntity.toDomain() = Product(
        id = id,
        name = name,
        code = code,
        sku = sku,
        barcode = barcode,
        qrCode = qrCode,
        categoryId = categoryId,
        categoryName = categoryName,
        unitId = unitId,
        unitName = unitName,
        purchasePrice = purchasePrice,
        sellingPrice = sellingPrice,
        stock = stock,
        minimumStock = minimumStock,
        warehouseId = warehouseId,
        warehouseName = warehouseName,
        image = image,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Product.toEntity() = ProductEntity(
        id = id,
        name = name,
        code = code,
        sku = sku,
        barcode = barcode,
        qrCode = qrCode,
        categoryId = categoryId,
        categoryName = categoryName,
        unitId = unitId,
        unitName = unitName,
        purchasePrice = purchasePrice,
        sellingPrice = sellingPrice,
        stock = stock,
        minimumStock = minimumStock,
        warehouseId = warehouseId,
        warehouseName = warehouseName,
        image = image,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TransactionEntity.toDomain(): Transaction {
        val itemsList = try {
            transactionItemsAdapter.fromJson(itemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val pm = try { PaymentMethod.valueOf(paymentMethod) } catch (e: Exception) { PaymentMethod.CASH }
        val sync = try { SyncStatus.valueOf(syncStatus) } catch (e: Exception) { SyncStatus.SYNCED }
        return Transaction(
            id = id,
            date = date,
            cashierName = cashierName,
            cashierId = cashierId,
            customerName = customerName,
            items = itemsList,
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            additionalFee = additionalFee,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            changeAmount = changeAmount,
            paymentMethod = pm,
            syncStatus = sync,
            notes = notes
        )
    }

    private fun Transaction.toEntity(): TransactionEntity {
        val json = transactionItemsAdapter.toJson(items)
        return TransactionEntity(
            id = id,
            date = date,
            cashierName = cashierName,
            cashierId = cashierId,
            customerName = customerName,
            itemsJson = json,
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            additionalFee = additionalFee,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            changeAmount = changeAmount,
            paymentMethod = paymentMethod.name,
            syncStatus = syncStatus.name,
            notes = notes
        )
    }

    private fun StockMovementEntity.toDomain(): StockMovement {
        val itemsList = try { movementItemsAdapter.fromJson(itemsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val mType = try { MovementType.valueOf(type) } catch (e: Exception) { MovementType.IN }
        return StockMovement(
            id = id,
            transactionNo = transactionNo,
            type = mType,
            date = date,
            warehouseId = warehouseId,
            warehouseName = warehouseName,
            entityName = entityName,
            items = itemsList,
            totalAmount = totalAmount,
            notes = notes
        )
    }

    private fun StockMovement.toEntity(): StockMovementEntity {
        val json = movementItemsAdapter.toJson(items)
        return StockMovementEntity(
            id = id,
            transactionNo = transactionNo,
            type = type.name,
            date = date,
            warehouseId = warehouseId,
            warehouseName = warehouseName,
            entityName = entityName,
            itemsJson = json,
            totalAmount = totalAmount,
            notes = notes
        )
    }

    private fun StockAdjustmentEntity.toDomain(): StockAdjustment {
        val r = try { AdjustmentReason.valueOf(reason) } catch (e: Exception) { AdjustmentReason.STOCK_OPNAME }
        return StockAdjustment(
            id = id,
            date = date,
            productId = productId,
            productName = productName,
            productCode = productCode,
            warehouseId = warehouseId,
            warehouseName = warehouseName,
            systemStock = systemStock,
            actualStock = actualStock,
            difference = difference,
            reason = r,
            notes = notes,
            createdBy = createdBy
        )
    }

    private fun StockAdjustment.toEntity(): StockAdjustmentEntity {
        return StockAdjustmentEntity(
            id = id,
            date = date,
            productId = productId,
            productName = productName,
            productCode = productCode,
            warehouseId = warehouseId,
            warehouseName = warehouseName,
            systemStock = systemStock,
            actualStock = actualStock,
            difference = difference,
            reason = reason.name,
            notes = notes,
            createdBy = createdBy
        )
    }

    private fun getStartOfDayTimestamp(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
