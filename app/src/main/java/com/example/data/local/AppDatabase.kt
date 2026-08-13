package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.StockAdjustmentDao
import com.example.data.local.dao.StockMovementDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UnitDao
import com.example.data.local.dao.WarehouseDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockAdjustmentEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UnitEntity
import com.example.data.local.entity.WarehouseEntity

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        UnitEntity::class,
        SupplierEntity::class,
        CustomerEntity::class,
        WarehouseEntity::class,
        TransactionEntity::class,
        StockMovementEntity::class,
        StockAdjustmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun unitDao(): UnitDao
    abstract fun supplierDao(): SupplierDao
    abstract fun customerDao(): CustomerDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun transactionDao(): TransactionDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_pos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
