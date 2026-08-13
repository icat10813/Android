package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.remote.RetrofitClient
import com.example.data.repository.InventoryRepositoryImpl
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.AuthViewModelFactory
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.dashboard.DashboardViewModelFactory
import com.example.ui.navigation.AppNavigationGraph
import com.example.ui.pos.PosViewModel
import com.example.ui.pos.PosViewModelFactory
import com.example.ui.products.ProductViewModel
import com.example.ui.products.ProductViewModelFactory
import com.example.ui.theme.InventoryPosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val apiService = RetrofitClient.apiService
        val repository = InventoryRepositoryImpl(database, apiService)
        val userPreferences = UserPreferences(applicationContext)

        setContent {
            InventoryPosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LaunchedEffect(Unit) {
                        repository.initializeSeedDataIfNeeded()
                    }

                    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userPreferences))
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(repository))
                    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(repository))
                    val posViewModel: PosViewModel = viewModel(factory = PosViewModelFactory(repository))

                    AppNavigationGraph(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        productViewModel = productViewModel,
                        posViewModel = posViewModel,
                        repository = repository
                    )
                }
            }
        }
    }
}
