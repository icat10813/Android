package com.example.ui.stock

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.repository.InventoryRepository
import com.example.domain.model.MovementType
import com.example.domain.model.StockMovement
import com.example.domain.model.StockMovementItem
import com.example.ui.components.EmptyState
import com.example.ui.products.ProductViewModel
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.utils.DateFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockMovementScreen(
    type: String, // "IN" or "OUT"
    repository: InventoryRepository,
    productViewModel: ProductViewModel,
    scannedCode: String? = null,
    onNavigateToQrScanner: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val title = if (type == "IN") "Barang Masuk (Penerimaan)" else "Barang Keluar (Pengeluaran)"
    val isTypeIn = type == "IN"

    val productsState by productViewModel.uiState.collectAsState()
    val suppliers by repository.getAllSuppliers().collectAsState(initial = emptyList())

    var selectedProductId by remember { mutableStateOf("") }
    var selectedSupplierId by remember { mutableStateOf("") }
    var qtyInput by remember { mutableStateOf("10") }
    var notesInput by remember { mutableStateOf("") }

    var expandedProd by remember { mutableStateOf(false) }
    var expandedSup by remember { mutableStateOf(false) }

    var movementsList by remember { mutableStateOf<List<StockMovement>>(emptyList()) }

    fun refreshLogs() {
        scope.launch {
            val allMovements = repository.getAllStockMovements().first()
            val targetType = if (isTypeIn) MovementType.IN else MovementType.OUT
            movementsList = allMovements.filter { it.type == targetType }
        }
    }

    LaunchedEffect(Unit) {
        refreshLogs()
    }

    LaunchedEffect(productsState.products) {
        if (selectedProductId.isEmpty() && productsState.products.isNotEmpty()) {
            selectedProductId = productsState.products.first().id
        }
    }

    LaunchedEffect(scannedCode, productsState.products) {
        if (!scannedCode.isNullOrEmpty() && productsState.products.isNotEmpty()) {
            val matchingProduct = productsState.products.find { 
                it.code == scannedCode || it.sku == scannedCode || it.barcode == scannedCode 
            }
            if (matchingProduct != null) {
                selectedProductId = matchingProduct.id
                Toast.makeText(context, "Produk dipilih: ${matchingProduct.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Barcode tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Entry Form
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Input Transaksi ${if (isTypeIn) "Masuk" else "Keluar"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Product Dropdown & Scan QR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedProd,
                            onExpandedChange = { expandedProd = !expandedProd },
                            modifier = Modifier.weight(1f)
                        ) {
                            val prodName = productsState.products.find { it.id == selectedProductId }?.name ?: "Pilih Barang"
                            OutlinedTextField(
                                value = prodName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Barang / Produk") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProd) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("select_product_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = expandedProd,
                                onDismissRequest = { expandedProd = false }
                            ) {
                                productsState.products.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("${p.name} (Stok: ${p.stock})") },
                                        onClick = {
                                            selectedProductId = p.id
                                            expandedProd = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = onNavigateToQrScanner,
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", tint = Color.White)
                        }
                    }

                    if (isTypeIn && suppliers.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedSup,
                            onExpandedChange = { expandedSup = !expandedSup }
                        ) {
                            val supName = suppliers.find { it.id == selectedSupplierId }?.name ?: "Pilih Supplier"
                            OutlinedTextField(
                                value = supName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Supplier / Pemasok") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSup) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedSup,
                                onDismissRequest = { expandedSup = false }
                            ) {
                                suppliers.forEach { sup ->
                                    DropdownMenuItem(
                                        text = { Text(sup.name) },
                                        onClick = {
                                            selectedSupplierId = sup.id
                                            expandedSup = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = qtyInput,
                            onValueChange = { qtyInput = it },
                            label = { Text("Jumlah Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stock_qty_input")
                        )
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Keterangan") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val qty = qtyInput.toIntOrNull() ?: 0
                            if (qty <= 0 || selectedProductId.isEmpty()) return@Button

                            val targetProduct = productsState.products.find { it.id == selectedProductId } ?: return@Button
                            val supplierObj = suppliers.find { it.id == selectedSupplierId }

                            val trxNo = DateFormatter.generateStockTransactionNo(if (isTypeIn) "IN" else "OUT")
                            val movementItem = StockMovementItem(
                                productId = targetProduct.id,
                                productName = targetProduct.name,
                                productCode = targetProduct.code,
                                qty = qty,
                                price = targetProduct.purchasePrice,
                                subtotal = targetProduct.purchasePrice * qty
                            )

                            val movement = StockMovement(
                                id = "mvt_${System.currentTimeMillis()}",
                                transactionNo = trxNo,
                                type = if (isTypeIn) MovementType.IN else MovementType.OUT,
                                date = System.currentTimeMillis(),
                                warehouseId = targetProduct.warehouseId,
                                warehouseName = targetProduct.warehouseName,
                                entityName = supplierObj?.name ?: "Gudang",
                                items = listOf(movementItem),
                                totalAmount = movementItem.subtotal,
                                notes = notesInput
                            )

                            scope.launch {
                                if (isTypeIn) {
                                    repository.processStockIn(movement)
                                } else {
                                    repository.processStockOut(movement)
                                }
                                Toast.makeText(context, "Stok $type berhasil diproses!", Toast.LENGTH_SHORT).show()
                                qtyInput = "10"
                                notesInput = ""
                                refreshLogs()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isTypeIn) SuccessGreen else DangerRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_stock_button")
                    ) {
                        Text("SIMPAN TRANSAKSI $type", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Riwayat Transaksi Barang $type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (movementsList.isEmpty()) {
                EmptyState("Belum ada riwayat stok $type")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(movementsList) { item ->
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
                                    val itemNames = item.items.joinToString { it.productName }
                                    val totalQty = item.items.sumOf { it.qty }
                                    Text(itemNames, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${item.transactionNo} • ${DateFormatter.formatDate(item.date)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Surface(
                                    color = (if (isTypeIn) SuccessGreen else DangerRed).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    val totalQty = item.items.sumOf { it.qty }
                                    Text(
                                        text = "${if (isTypeIn) "+" else "-"}$totalQty",
                                        color = if (isTypeIn) SuccessGreen else DangerRed,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
