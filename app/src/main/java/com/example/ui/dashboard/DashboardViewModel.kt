package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.InventoryRepository
import com.example.domain.model.DashboardSummary
import com.example.domain.model.Product
import com.example.domain.model.SalesChartData
import com.example.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary = DashboardSummary(),
    val chartData: List<SalesChartData> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val isRefreshing: Boolean = false
)

class DashboardViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.initializeSeedDataIfNeeded()

            combine(
                repository.getAllProducts(),
                repository.getAllTransactions()
            ) { products, transactions ->
                val summary = repository.getDashboardSummary()
                val chart = repository.getSalesChartData()
                val lowStock = products.filter { it.isLowStock || it.isOutOfStock }
                val recentTrx = transactions.take(5)

                DashboardUiState(
                    isLoading = false,
                    summary = summary,
                    chartData = chart,
                    lowStockProducts = lowStock,
                    recentTransactions = recentTrx,
                    isRefreshing = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadDashboardData()
        }
    }
}

class DashboardViewModelFactory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
