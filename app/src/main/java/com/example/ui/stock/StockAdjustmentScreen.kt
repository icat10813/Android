package com.example.ui.stock

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.repository.InventoryRepository
import com.example.domain.model.AdjustmentReason
import com.example.domain.model.StockAdjustment
import com.example.ui.products.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentScreen(
    productId: String,
    repository: InventoryRepository,
    productViewModel: ProductViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(productId) {
        productViewModel.getProductDetail(productId)
    }

    val product by productViewModel.selectedProduct.collectAsState()

    var physicalQtyInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("Stock Opname Rutin") }
    var selectedReason by remember { mutableStateOf(AdjustmentReason.STOCK_OPNAME) }

    LaunchedEffect(product) {
        if (product != null) {
            physicalQtyInput = product!!.stock.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Penyesuaian Stok (Stock Opname)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val p = product!!
            val physicalQty = physicalQtyInput.toIntOrNull() ?: p.stock
            val difference = physicalQty - p.stock

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(p.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("SKU: ${p.sku} | Kode: ${p.code}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Stok Sistem", style = MaterialTheme.typography.labelSmall)
                                Text("${p.stock} ${p.unitName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Selisih Adjust", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = if (difference > 0) "+$difference" else "$difference",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (difference == 0) MaterialTheme.colorScheme.onSurface else if (difference > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = physicalQtyInput,
                    onValueChange = { physicalQtyInput = it },
                    label = { Text("Hasil Perhitungan Fisik (Stok Nyata)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("physical_stock_input")
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Alasan Penyesuaian / Catatan Opname") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Button(
                    onClick = {
                        val adj = StockAdjustment(
                            id = "adj_${System.currentTimeMillis()}",
                            date = System.currentTimeMillis(),
                            productId = p.id,
                            productName = p.name,
                            productCode = p.code,
                            warehouseId = p.warehouseId,
                            warehouseName = p.warehouseName,
                            systemStock = p.stock,
                            actualStock = physicalQty,
                            difference = difference,
                            reason = selectedReason,
                            notes = notesInput,
                            createdBy = "Auditor Gudang"
                        )

                        scope.launch {
                            repository.processStockAdjustment(adj)
                            Toast.makeText(context, "Penyesuaian stok berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_adjustment_button")
                ) {
                    Text("SIMPAN PENYESUAIAN STOK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
