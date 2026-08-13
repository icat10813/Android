package com.example.ui.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Product
import com.example.ui.products.ProductViewModel
import com.example.utils.CurrencyFormatter
import com.example.utils.QrCodeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrGeneratorScreen(
    productId: String,
    viewModel: ProductViewModel,
    onBackClick: () -> Unit
) {
    LaunchedEffect(productId) {
        viewModel.getProductDetail(productId)
    }

    val product by viewModel.selectedProduct.collectAsState()
    var labelCount by remember { mutableStateOf(4) } // 1, 4, 8, 12 per page

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cetak Label QR Barang", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Print action simulation */ },
                        modifier = Modifier.testTag("print_action_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Cetak")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Layout preset selector
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pilih Jumlah Label Per Halaman:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(1, 4, 8, 12).forEach { count ->
                                FilterChip(
                                    selected = labelCount == count,
                                    onClick = { labelCount = count },
                                    label = { Text("$count Label") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Pratinjau Cetak Label:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Grid of QR Code Labels
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val columns = if (labelCount <= 1) 1 else 2
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items((1..labelCount).toList()) { index ->
                            QrLabelItem(product = p)
                        }
                    }
                }

                Button(
                    onClick = { /* Print logic */ },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_print_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CETAK HARGA & QR LABEL ($labelCount PCS)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QrLabelItem(product: Product) {
    val bitmap = remember(product.qrCode) { QrCodeUtils.generateQrBitmap(product.qrCode, 180, 180) }

    Box(
        modifier = Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "INVENTORY STORE",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.formatRupiah(product.sellingPrice),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "SKU: ${product.sku}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = Color.DarkGray
            )
        }
    }
}
