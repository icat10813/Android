package com.example.ui.reports

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.InventoryRepository
import com.example.domain.model.Product
import com.example.ui.components.MetricCard
import com.example.ui.components.SimpleBarChart
import com.example.utils.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    repository: InventoryRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var productsList by remember { mutableStateOf<List<Product>>(emptyList()) }
    var chartData by remember { mutableStateOf(emptyList<com.example.domain.model.SalesChartData>()) }
    var totalSalesVal by remember { mutableStateOf(0.0) }
    var totalCostVal by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        scope.launch {
            repository.getAllProducts().collect { products ->
                productsList = products
                chartData = repository.getSalesChartData()
                totalSalesVal = products.sumOf { it.sellingPrice * it.stock }
                totalCostVal = products.sumOf { it.purchasePrice * it.stock }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan & Analitik Gudang", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Laporan berhasil diexport ke Excel / PDF!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("export_report_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Nilai Aset Jual",
                        value = CurrencyFormatter.formatRupiah(totalSalesVal),
                        icon = Icons.Default.TrendingUp,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Nilai Modal Beli",
                        value = CurrencyFormatter.formatRupiah(totalCostVal),
                        icon = Icons.Default.TrendingUp,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SimpleBarChart(data = chartData)
            }

            item {
                Text(
                    text = "Daftar Aset Nilai Stok per Barang",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(productsList) { p ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(p.name, fontWeight = FontWeight.Bold)
                            Text("Stok: ${p.stock} ${p.unitName}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            CurrencyFormatter.formatRupiah(p.sellingPrice * p.stock),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
