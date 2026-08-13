package com.example.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.InventoryRepository
import com.example.domain.model.Category
import com.example.domain.model.Product
import com.example.domain.model.ProductUnit
import com.example.domain.model.Warehouse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProductListUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val warehouses: List<Warehouse> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val selectedWarehouseId: String? = null,
    val stockFilter: String = "ALL", // ALL, NORMAL, LOW, OUT
    val sortBy: String = "NAME_ASC" // NAME_ASC, PRICE_ASC, PRICE_DESC, STOCK_ASC, STOCK_DESC
)

class ProductViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateInScope(emptyList())

    val units: StateFlow<List<ProductUnit>> = repository.getAllUnits()
        .stateInScope(emptyList())

    val warehouses: StateFlow<List<Warehouse>> = repository.getAllWarehouses()
        .stateInScope(emptyList())

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            combine(
                repository.getAllProducts(),
                repository.getAllCategories(),
                repository.getAllWarehouses(),
                _uiState
            ) { products, categories, warehouses, currentState ->
                var filtered = products

                // Search Filter
                if (currentState.searchQuery.isNotBlank()) {
                    val q = currentState.searchQuery.trim().lowercase()
                    filtered = filtered.filter {
                        it.name.lowercase().contains(q) ||
                                it.code.lowercase().contains(q) ||
                                it.sku.lowercase().contains(q) ||
                                it.barcode.lowercase().contains(q)
                    }
                }

                // Category Filter
                if (currentState.selectedCategoryId != null) {
                    filtered = filtered.filter { it.categoryId == currentState.selectedCategoryId }
                }

                // Warehouse Filter
                if (currentState.selectedWarehouseId != null) {
                    filtered = filtered.filter { it.warehouseId == currentState.selectedWarehouseId }
                }

                // Stock Filter
                filtered = when (currentState.stockFilter) {
                    "LOW" -> filtered.filter { it.isLowStock }
                    "OUT" -> filtered.filter { it.isOutOfStock }
                    "NORMAL" -> filtered.filter { !it.isLowStock && !it.isOutOfStock }
                    else -> filtered
                }

                // Sort
                filtered = when (currentState.sortBy) {
                    "PRICE_ASC" -> filtered.sortedBy { it.sellingPrice }
                    "PRICE_DESC" -> filtered.sortedByDescending { it.sellingPrice }
                    "STOCK_ASC" -> filtered.sortedBy { it.stock }
                    "STOCK_DESC" -> filtered.sortedByDescending { it.stock }
                    else -> filtered.sortedBy { it.name }
                }

                currentState.copy(
                    isLoading = false,
                    products = filtered,
                    categories = categories,
                    warehouses = warehouses
                )
            }.collect { updated ->
                _uiState.value = updated
            }
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun filterByCategory(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun filterByWarehouse(warehouseId: String?) {
        _uiState.value = _uiState.value.copy(selectedWarehouseId = warehouseId)
    }

    fun filterByStock(stockFilter: String) {
        _uiState.value = _uiState.value.copy(stockFilter = stockFilter)
    }

    fun setSortBy(sort: String) {
        _uiState.value = _uiState.value.copy(sortBy = sort)
    }

    fun getProductDetail(id: String) {
        viewModelScope.launch {
            _selectedProduct.value = repository.getProductById(id)
        }
    }

    fun saveProduct(product: Product, isEdit: Boolean) {
        viewModelScope.launch {
            if (isEdit) {
                repository.updateProduct(product)
            } else {
                repository.createProduct(product)
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInScope(initialValue: T): StateFlow<T> {
        val flow = MutableStateFlow(initialValue)
        viewModelScope.launch {
            this@stateInScope.collect { flow.value = it }
        }
        return flow.asStateFlow()
    }
}

class ProductViewModelFactory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
