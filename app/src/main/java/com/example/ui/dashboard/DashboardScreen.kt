package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MetricCard
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.StockBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.utils.CurrencyFormatter
import com.example.utils.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    scannedCode: String? = null,
    onClearScannedCode: () -> Unit = {},
    onNavigateToPos: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToStockIn: () -> Unit,
    onNavigateToStockInWithCode: (String) -> Unit = {},
    onNavigateToStockOut: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(scannedCode) {
        if (!scannedCode.isNullOrEmpty()) {
            onNavigateToStockInWithCode(scannedCode)
            onClearScannedCode()
        }
    }

    Scaffold { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Header Banner
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ringkasan Toko & Gudang",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pantau stok, penjualan, dan pergerakan barang hari ini.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Quick Actions Section
                item {
                    Text(
                        text = "Aksi Cepat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            QuickActionButton(
                                title = "POS Kasir",
                                icon = Icons.Default.PointOfSale,
                                color = MaterialTheme.colorScheme.primary,
                                onClick = onNavigateToPos,
                                tag = "quick_pos_button"
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "Scan QR",
                                icon = Icons.Default.QrCodeScanner,
                                color = Color(0xFF8B5CF6),
                                onClick = onNavigateToQrScanner,
                                tag = "quick_qr_button"
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "+ Barang",
                                icon = Icons.Default.AddBox,
                                color = SuccessGreen,
                                onClick = onNavigateToAddProduct,
                                tag = "quick_add_product_button"
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "Barang Masuk",
                                icon = Icons.Default.Input,
                                color = Color(0xFF0284C7),
                                onClick = onNavigateToStockIn,
                                tag = "quick_stock_in_button"
                            )
                        }
                        item {
                            QuickActionButton(
                                title = "Barang Keluar",
                                icon = Icons.Default.Output,
                                color = WarningAmber,
                                onClick = onNavigateToStockOut,
                                tag = "quick_stock_out_button"
                            )
                        }
                    }
                }

                // Key Metric Cards Grid (2 columns)
                item {
                    Text(
                        text = "Statistik Stok & Penjualan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = "Total Penjualan",
                                value = CurrencyFormatter.formatRupiah(uiState.summary.totalSalesToday),
                                icon = Icons.Default.Payments,
                                iconColor = SuccessGreen,
                                subText = "${uiState.summary.totalTransactionsToday} Transaksi Hari Ini",
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Total Produk",
                                value = "${uiState.summary.totalProducts} Items",
                                icon = Icons.Default.Inventory2,
                                iconColor = MaterialTheme.colorScheme.primary,
                                subText = "${uiState.summary.totalStock} Total Stok Unit",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = "Stok Menipis",
                                value = "${uiState.summary.lowStockCount} Produk",
                                icon = Icons.Default.Warning,
                                iconColor = WarningAmber,
                                subText = "Perlu restock",
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Barang Habis",
                                value = "${uiState.summary.outOfStockCount} Produk",
                                icon = Icons.Default.ErrorOutline,
                                iconColor = DangerRed,
                                subText = "Stok Kosong",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = "Barang Masuk",
                                value = "${uiState.summary.stockInToday} Unit",
                                icon = Icons.Default.ArrowDownward,
                                iconColor = Color(0xFF0284C7),
                                subText = "Penerimaan hari ini",
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Barang Keluar",
                                value = "${uiState.summary.stockOutToday} Unit",
                                icon = Icons.Default.ArrowUpward,
                                iconColor = WarningAmber,
                                subText = "Pengeluaran hari ini",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sales Chart
                item {
                    SimpleBarChart(data = uiState.chartData)
                }

                // Low Stock Alert Section ("Produk Perlu Restock")
                if (uiState.lowStockProducts.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = WarningAmber
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Produk Perlu Restock",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                uiState.lowStockProducts.take(4).forEach { product ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateToProductDetail(product.id) }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = product.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "SKU: ${product.sku} | Gudang: ${product.warehouseName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        StockBadge(stock = product.stock, minStock = product.minimumStock)
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }

                // Recent Transactions List
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Transaksi Terbaru",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (uiState.recentTransactions.isEmpty()) {
                                Text(
                                    text = "Belum ada transaksi hari ini.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            } else {
                                uiState.recentTransactions.forEach { trx ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = trx.id,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${DateFormatter.formatDate(trx.date)} • ${trx.items.size} Items",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        Text(
                                            text = CurrencyFormatter.formatRupiah(trx.grandTotal),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 11.sp
            )
        }
    }
}
