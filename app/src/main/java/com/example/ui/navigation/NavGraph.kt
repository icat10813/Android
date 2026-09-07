package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.InventoryRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.components.AppTopBar
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.master.MasterDataScreen
import com.example.ui.pos.PosScreen
import com.example.ui.pos.PosViewModel
import com.example.ui.pos.ReceiptScreen
import com.example.ui.products.AddEditProductScreen
import com.example.ui.products.ProductDetailScreen
import com.example.ui.products.ProductListScreen
import com.example.ui.products.ProductViewModel
import com.example.ui.qr.QrGeneratorScreen
import com.example.ui.qr.QrScannerScreen
import com.example.ui.reports.ReportScreen
import com.example.ui.stock.StockAdjustmentScreen
import com.example.ui.stock.StockMovementScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.BarChart)
    object ProductList : Screen("products", "Barang", Icons.Default.Inventory2)
    object Pos : Screen("pos", "Kasir POS", Icons.Default.PointOfSale)
    object QrScanner : Screen("qr_scanner", "Scan QR", Icons.Default.QrCodeScanner)
    object ProductDetail : Screen("product_detail/{productId}", "Detail Barang") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object AddEditProduct : Screen("add_edit_product/{productId}", "Form Barang") {
        fun createRoute(productId: String) = "add_edit_product/$productId"
    }
    object QrGenerator : Screen("qr_generator/{productId}", "Label QR") {
        fun createRoute(productId: String) = "qr_generator/$productId"
    }
    object Receipt : Screen("receipt/{transactionId}", "Struk Transaksi") {
        fun createRoute(transactionId: String) = "receipt/$transactionId"
    }
    object StockIn : Screen("stock_in", "Barang Masuk", Icons.Default.Input)
    object StockOut : Screen("stock_out", "Barang Keluar", Icons.Default.Output)
    object StockAdjustment : Screen("stock_adjustment/{productId}", "Penyesuaian") {
        fun createRoute(productId: String) = "stock_adjustment/$productId"
    }
    object MasterCategory : Screen("master_category", "Kategori", Icons.Default.Category)
    object MasterSupplier : Screen("master_supplier", "Supplier", Icons.Default.LocalShipping)
    object MasterWarehouse : Screen("master_warehouse", "Gudang", Icons.Default.Warehouse)
    object Reports : Screen("reports", "Laporan", Icons.Default.Analytics)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    productViewModel: ProductViewModel,
    posViewModel: PosViewModel,
    repository: InventoryRepository
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val bottomNavScreens = listOf(
        Screen.Dashboard,
        Screen.ProductList,
        Screen.Pos,
        Screen.Reports
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.ProductList.route,
        Screen.Pos.route,
        Screen.Reports.route
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBottomBar,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Inventory POS Gudang", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Versi 2.4 Terpadu", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Dashboard Utama") },
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Dashboard.route)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                    label = { Text("Kelola Stok Barang") },
                    selected = currentRoute == Screen.ProductList.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.ProductList.route)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Input, contentDescription = null) },
                    label = { Text("Barang Masuk (Penerimaan)") },
                    selected = currentRoute == Screen.StockIn.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.StockIn.route)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Output, contentDescription = null) },
                    label = { Text("Barang Keluar (Pengeluaran)") },
                    selected = currentRoute == Screen.StockOut.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.StockOut.route)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Category, contentDescription = null) },
                    label = { Text("Master Kategori") },
                    selected = currentRoute == Screen.MasterCategory.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.MasterCategory.route)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                    label = { Text("Master Supplier") },
                    selected = currentRoute == Screen.MasterSupplier.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.MasterSupplier.route)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Warehouse, contentDescription = null) },
                    label = { Text("Master Gudang") },
                    selected = currentRoute == Screen.MasterWarehouse.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.MasterWarehouse.route)
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    label = { Text("Keluar (Logout)", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showBottomBar) {
                    val title = when (currentRoute) {
                        Screen.Dashboard.route -> "Inventory POS"
                        Screen.ProductList.route -> "Daftar Barang"
                        Screen.Pos.route -> "Kasir Penjualan POS"
                        Screen.Reports.route -> "Laporan & Analitik"
                        else -> "Inventory POS"
                    }
                    AppTopBar(
                        title = title,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        bottomNavScreens.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon ?: Icons.Default.Circle,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title, fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (showBottomBar && currentRoute != Screen.Pos.route) {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.QrScanner.route) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("global_qr_scan_fab")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Dashboard.route) { backStackEntry ->
                    val scannedCode = backStackEntry.savedStateHandle.get<String>("scanned_code")
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        scannedCode = scannedCode,
                        onClearScannedCode = { backStackEntry.savedStateHandle.remove<String>("scanned_code") },
                        onNavigateToPos = { navController.navigate(Screen.Pos.route) },
                        onNavigateToQrScanner = { navController.navigate(Screen.QrScanner.route) },
                        onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute("new")) },
                        onNavigateToStockIn = { navController.navigate(Screen.StockIn.route) },
                        onNavigateToStockInWithCode = { code -> 
                            navController.currentBackStackEntry?.savedStateHandle?.set("scanned_code", code)
                            navController.navigate(Screen.StockIn.route)
                        },
                        onNavigateToStockOut = { navController.navigate(Screen.StockOut.route) },
                        onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
                    )
                }

                composable(Screen.ProductList.route) {
                    ProductListScreen(
                        viewModel = productViewModel,
                        onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute("new")) },
                        onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
                    )
                }

                composable(Screen.Pos.route) { backStackEntry ->
                    val scannedCode = backStackEntry.savedStateHandle.get<String>("scanned_code")
                    PosScreen(
                        viewModel = posViewModel,
                        scannedCode = scannedCode,
                        onNavigateToQrScanner = { navController.navigate(Screen.QrScanner.route) },
                        onNavigateToReceipt = { trxId -> navController.navigate(Screen.Receipt.createRoute(trxId)) }
                    )
                }

                composable(Screen.QrScanner.route) {
                    QrScannerScreen(
                        onBackClick = { navController.navigateUp() },
                        onCodeScanned = { code ->
                            navController.previousBackStackEntry?.savedStateHandle?.set("scanned_code", code)
                            navController.navigateUp()
                        }
                    )
                }

                composable(Screen.ProductDetail.route) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId") ?: ""
                    ProductDetailScreen(
                        productId = productId,
                        viewModel = productViewModel,
                        onBackClick = { navController.navigateUp() },
                        onEditClick = { id -> navController.navigate(Screen.AddEditProduct.createRoute(id)) },
                        onNavigateToQrLabel = { id -> navController.navigate(Screen.QrGenerator.createRoute(id)) },
                        onNavigateToPosWithProduct = { id ->
                            posViewModel.addProductByCodeOrId(id)
                            navController.navigate(Screen.Pos.route)
                        },
                        onNavigateToStockAdjustment = { id -> navController.navigate(Screen.StockAdjustment.createRoute(id)) }
                    )
                }

                composable(Screen.AddEditProduct.route) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId")
                    AddEditProductScreen(
                        productId = productId,
                        viewModel = productViewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Screen.QrGenerator.route) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId") ?: ""
                    QrGeneratorScreen(
                        productId = productId,
                        viewModel = productViewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Screen.Receipt.route) { backStackEntry ->
                    val trxId = backStackEntry.arguments?.getString("transactionId") ?: ""
                    ReceiptScreen(
                        transactionId = trxId,
                        repository = repository,
                        onBackToDashboard = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        },
                        onNewTransaction = {
                            posViewModel.resetCheckout()
                            navController.navigate(Screen.Pos.route) {
                                popUpTo(Screen.Pos.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.StockIn.route) { backStackEntry ->
                    val scannedCode = backStackEntry.savedStateHandle.get<String>("scanned_code")
                    StockMovementScreen(
                        type = "IN",
                        repository = repository,
                        productViewModel = productViewModel,
                        scannedCode = scannedCode,
                        onNavigateToQrScanner = { navController.navigate(Screen.QrScanner.route) },
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Screen.StockOut.route) { backStackEntry ->
                    val scannedCode = backStackEntry.savedStateHandle.get<String>("scanned_code")
                    StockMovementScreen(
                        type = "OUT",
                        repository = repository,
                        productViewModel = productViewModel,
                        scannedCode = scannedCode,
                        onNavigateToQrScanner = { navController.navigate(Screen.QrScanner.route) },
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Screen.StockAdjustment.route) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId") ?: ""
                    StockAdjustmentScreen(
                        productId = productId,
                        repository = repository,
                        productViewModel = productViewModel,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Screen.MasterCategory.route) {
                    MasterDataScreen(
                        masterType = "CATEGORY",
                        repository = repository,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Screen.MasterSupplier.route) {
                    MasterDataScreen(
                        masterType = "SUPPLIER",
                        repository = repository,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Screen.MasterWarehouse.route) {
                    MasterDataScreen(
                        masterType = "WAREHOUSE",
                        repository = repository,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Screen.Reports.route) {
                    ReportScreen(
                        repository = repository,
                        onBackClick = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}
