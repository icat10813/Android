package com.example.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.InventoryRepository
import com.example.domain.model.Customer
import com.example.domain.model.PaymentMethod
import com.example.domain.model.Product
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionItem
import com.example.utils.DateFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PosUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val cartItems: List<TransactionItem> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val searchQuery: String = "",
    val discount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paidAmount: Double = 0.0,
    val isCompleted: Boolean = false,
    val completedTransaction: Transaction? = null
) {
    val subtotal: Double
        get() = cartItems.sumOf { it.subtotal }

    val grandTotal: Double
        get() = (subtotal - discount).coerceAtLeast(0.0)

    val changeAmount: Double
        get() = (paidAmount - grandTotal).coerceAtLeast(0.0)

    val totalItemsCount: Int
        get() = cartItems.sumOf { it.qty }
}

class PosViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                repository.getAllCustomers().collect { customers ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        filteredProducts = filterProducts(products, _uiState.value.searchQuery),
                        customers = customers,
                        selectedCustomer = customers.firstOrNull()
                    )
                }
            }
        }
    }

    private fun filterProducts(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products
        val q = query.trim().lowercase()
        return products.filter {
            it.name.lowercase().contains(q) ||
                    it.code.lowercase().contains(q) ||
                    it.sku.lowercase().contains(q) ||
                    it.barcode.lowercase().contains(q)
        }
    }

    fun searchProducts(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredProducts = filterProducts(_uiState.value.products, query)
        )
    }

    fun addToCart(product: Product) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.productId == product.id }

        if (existingIndex >= 0) {
            val existingItem = currentCart[existingIndex]
            if (existingItem.qty < product.stock) {
                val newQty = existingItem.qty + 1
                currentCart[existingIndex] = existingItem.copy(
                    qty = newQty,
                    subtotal = existingItem.price * newQty
                )
            }
        } else {
            if (product.stock > 0) {
                currentCart.add(
                    TransactionItem(
                        productId = product.id,
                        productName = product.name,
                        productCode = product.code,
                        price = product.sellingPrice,
                        qty = 1,
                        subtotal = product.sellingPrice
                    )
                )
            }
        }
        _uiState.value = _uiState.value.copy(cartItems = currentCart)
    }

    fun addProductByCodeOrId(codeOrId: String) {
        val product = _uiState.value.products.find {
            it.id == codeOrId || it.code == codeOrId || it.sku == codeOrId || it.barcode == codeOrId
        }
        if (product != null) {
            addToCart(product)
        }
    }

    fun updateCartQty(productId: String, delta: Int) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val index = currentCart.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = currentCart[index]
            val newQty = item.qty + delta
            if (newQty <= 0) {
                currentCart.removeAt(index)
            } else {
                val product = _uiState.value.products.find { it.id == productId }
                val maxStock = product?.stock ?: 9999
                if (newQty <= maxStock) {
                    currentCart[index] = item.copy(
                        qty = newQty,
                        subtotal = item.price * newQty
                    )
                }
            }
        }
        _uiState.value = _uiState.value.copy(cartItems = currentCart)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(
            cartItems = emptyList(),
            discount = 0.0,
            paidAmount = 0.0
        )
    }

    fun selectCustomer(customer: Customer) {
        _uiState.value = _uiState.value.copy(selectedCustomer = customer)
    }

    fun setDiscount(amount: Double) {
        _uiState.value = _uiState.value.copy(discount = amount)
    }

    fun setPaymentMethod(methodStr: String) {
        val pm = try { PaymentMethod.valueOf(methodStr) } catch (_: Exception) { PaymentMethod.CASH }
        _uiState.value = _uiState.value.copy(paymentMethod = pm)
    }

    fun setPaidAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(paidAmount = amount)
    }

    fun checkout(cashierName: String) {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return

        val trxId = DateFormatter.generatePosTransactionNo()
        val transaction = Transaction(
            id = trxId,
            date = System.currentTimeMillis(),
            cashierName = cashierName,
            customerName = state.selectedCustomer?.name ?: "Pelanggan Umum",
            items = state.cartItems,
            subtotal = state.subtotal,
            discount = state.discount,
            grandTotal = state.grandTotal,
            paidAmount = if (state.paymentMethod == PaymentMethod.CASH) state.paidAmount else state.grandTotal,
            changeAmount = if (state.paymentMethod == PaymentMethod.CASH) state.changeAmount else 0.0,
            paymentMethod = state.paymentMethod
        )

        viewModelScope.launch {
            repository.processPosTransaction(transaction)
            _uiState.value = state.copy(
                isCompleted = true,
                completedTransaction = transaction
            )
        }
    }

    fun resetCheckout() {
        _uiState.value = _uiState.value.copy(
            cartItems = emptyList(),
            discount = 0.0,
            paidAmount = 0.0,
            isCompleted = false,
            completedTransaction = null
        )
    }
}

class PosViewModelFactory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
