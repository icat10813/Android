package com.example.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.PaymentMethod
import com.example.domain.model.Product
import com.example.domain.model.TransactionItem
import com.example.ui.components.StockBadge
import com.example.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    scannedCode: String? = null,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToReceipt: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showPaymentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(scannedCode) {
        if (!scannedCode.isNullOrEmpty()) {
            viewModel.addProductByCodeOrId(scannedCode)
        }
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted && uiState.completedTransaction != null) {
            onNavigateToReceipt(uiState.completedTransaction!!.id)
        }
    }

    Scaffold(
        bottomBar = {
            if (uiState.cartItems.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${uiState.totalItemsCount} Barang di Keranjang",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = CurrencyFormatter.formatRupiah(uiState.grandTotal),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.setPaidAmount(uiState.grandTotal)
                                showPaymentSheet = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("pos_checkout_button")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BAYAR SEKARANG", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Scan Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.searchProducts(it) },
                    placeholder = { Text("Cari barang atau scan barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_search_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onNavigateToQrScanner,
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .testTag("pos_scan_qr_button")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.White)
                }
            }

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.filteredProducts, key = { it.id }) { product ->
                    PosProductGridCard(
                        product = product,
                        onClick = { viewModel.addToCart(product) }
                    )
                }
            }
        }

        // Payment Modal Bottom Sheet
        if (showPaymentSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPaymentSheet = false },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                PaymentSheetContent(
                    uiState = uiState,
                    onDiscountChange = { viewModel.setDiscount(it) },
                    onPaymentMethodSelect = { viewModel.setPaymentMethod(it) },
                    onPaidAmountChange = { viewModel.setPaidAmount(it) },
                    onCartItemQtyChange = { id, delta -> viewModel.updateCartQty(id, delta) },
                    onConfirmPayment = {
                        viewModel.checkout("Rudi Kasir POS")
                        showPaymentSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun PosProductGridCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("pos_product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockBadge(stock = product.stock, minStock = product.minimumStock)
                Text(
                    text = product.code,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.formatRupiah(product.sellingPrice),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PaymentSheetContent(
    uiState: PosUiState,
    onDiscountChange: (Double) -> Unit,
    onPaymentMethodSelect: (String) -> Unit,
    onPaidAmountChange: (Double) -> Unit,
    onCartItemQtyChange: (String, Int) -> Unit,
    onConfirmPayment: () -> Unit
) {
    var discountInput by remember { mutableStateOf(uiState.discount.toInt().toString()) }
    var paidInput by remember { mutableStateOf(uiState.paidAmount.toInt().toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Rincian Pembayaran Kasir",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Cart items preview list
        LazyColumn(
            modifier = Modifier.heightIn(max = 180.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.cartItems) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.productName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("${item.qty} x ${CurrencyFormatter.formatRupiah(item.price)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onCartItemQtyChange(item.productId, -1) }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "-")
                        }
                        Text("${item.qty}", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onCartItemQtyChange(item.productId, 1) }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "+")
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
        }

        // Discount input
        OutlinedTextField(
            value = discountInput,
            onValueChange = {
                discountInput = it
                onDiscountChange(it.toDoubleOrNull() ?: 0.0)
            },
            label = { Text("Diskon Transaksi (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Payment Method Options
        Text("Metode Pembayaran", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("CASH", "QRIS", "DEBIT", "CREDIT").forEach { method ->
                FilterChip(
                    selected = uiState.paymentMethod.name == method,
                    onClick = { onPaymentMethodSelect(method) },
                    label = { Text(if (method == "CASH") "TUNAI" else method) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (uiState.paymentMethod == PaymentMethod.CASH) {
            OutlinedTextField(
                value = paidInput,
                onValueChange = {
                    paidInput = it
                    onPaidAmountChange(it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Uang Diterima (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cash_paid_input")
            )

            // Fast Cash suggestions
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    AssistChip(
                        onClick = {
                            paidInput = uiState.grandTotal.toInt().toString()
                            onPaidAmountChange(uiState.grandTotal)
                        },
                        label = { Text("Uang Pas") }
                    )
                }
                listOf(20000.0, 50000.0, 100000.0, 200000.0).forEach { amount ->
                    if (amount >= uiState.grandTotal) {
                        item {
                            AssistChip(
                                onClick = {
                                    paidInput = amount.toInt().toString()
                                    onPaidAmountChange(amount)
                                },
                                label = { Text(CurrencyFormatter.formatRupiah(amount)) }
                            )
                        }
                    }
                }
            }

            if (uiState.paidAmount >= uiState.grandTotal) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kembalian:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        CurrencyFormatter.formatRupiah(uiState.changeAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Button(
            onClick = onConfirmPayment,
            enabled = uiState.paymentMethod != PaymentMethod.CASH || uiState.paidAmount >= uiState.grandTotal,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_checkout_button")
        ) {
            Text("PROSES PEMBAYARAN & CETAK STRUK", fontWeight = FontWeight.Bold)
        }
    }
}
