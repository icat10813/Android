package com.example.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.Product
import com.example.domain.model.ProductUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: String?,
    viewModel: ProductViewModel,
    onBackClick: () -> Unit
) {
    val isEdit = productId != null && productId != "new"

    val categories by viewModel.categories.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()
    val units: List<ProductUnit> by viewModel.units.collectAsState()

    var code by remember { mutableStateOf("PRD-${(1000..9999).random()}") }
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("SKU-${(10000..99999).random()}") }
    var barcode by remember { mutableStateOf("899${(100000000..999999999).random()}") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedWarehouseId by remember { mutableStateOf("") }
    var selectedUnitId by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("0") }
    var sellingPrice by remember { mutableStateOf("0") }
    var initialStock by remember { mutableStateOf("10") }
    var minimumStock by remember { mutableStateOf("5") }
    var description by remember { mutableStateOf("") }

    var expandedCat by remember { mutableStateOf(false) }
    var expandedWh by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (isEdit && productId != null) {
            viewModel.getProductDetail(productId)
        }
    }

    val selectedProduct by viewModel.selectedProduct.collectAsState()

    LaunchedEffect(selectedProduct) {
        if (isEdit && selectedProduct != null) {
            val p = selectedProduct!!
            code = p.code
            name = p.name
            sku = p.sku
            barcode = p.barcode
            selectedCategoryId = p.categoryId
            selectedWarehouseId = p.warehouseId
            selectedUnitId = p.unitId
            purchasePrice = p.purchasePrice.toInt().toString()
            sellingPrice = p.sellingPrice.toInt().toString()
            initialStock = p.stock.toString()
            minimumStock = p.minimumStock.toString()
            description = p.description
        }
    }

    LaunchedEffect(categories, warehouses, units) {
        if (selectedCategoryId.isEmpty() && categories.isNotEmpty()) selectedCategoryId = categories.first().id
        if (selectedWarehouseId.isEmpty() && warehouses.isNotEmpty()) selectedWarehouseId = warehouses.first().id
        if (selectedUnitId.isEmpty() && units.isNotEmpty()) selectedUnitId = units.first().id
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Barang" else "Tambah Barang Baru", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Produk / Barang") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_name_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Kode Barang") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_code_input")
                )
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_sku_input")
                )
            }

            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Barcode") },
                trailingIcon = {
                    IconButton(onClick = { barcode = "899${(100000000..999999999).random()}" }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Generate")
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_barcode_input")
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCat,
                onExpandedChange = { expandedCat = !expandedCat }
            ) {
                val catName = categories.find { it.id == selectedCategoryId }?.name ?: "Pilih Kategori"
                OutlinedTextField(
                    value = catName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCat,
                    onDismissRequest = { expandedCat = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategoryId = cat.id
                                expandedCat = false
                            }
                        )
                    }
                }
            }

            // Warehouse Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedWh,
                onExpandedChange = { expandedWh = !expandedWh }
            ) {
                val whName = warehouses.find { it.id == selectedWarehouseId }?.name ?: "Pilih Gudang"
                OutlinedTextField(
                    value = whName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gudang Penyimpanan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWh) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedWh,
                    onDismissRequest = { expandedWh = false }
                ) {
                    warehouses.forEach { wh ->
                        DropdownMenuItem(
                            text = { Text(wh.name) },
                            onClick = {
                                selectedWarehouseId = wh.id
                                expandedWh = false
                            }
                        )
                    }
                }
            }

            // Price Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("Harga Beli (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("purchase_price_input")
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Harga Jual (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("selling_price_input")
                )
            }

            // Stock & Min Stock
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = initialStock,
                    onValueChange = { initialStock = it },
                    label = { Text("Stok Awal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_stock_input")
                )
                OutlinedTextField(
                    value = minimumStock,
                    onValueChange = { minimumStock = it },
                    label = { Text("Stok Minimum") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("min_stock_input")
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi Catatan") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val catObj = categories.find { it.id == selectedCategoryId }
                    val whObj = warehouses.find { it.id == selectedWarehouseId }
                    val unitObj = units.find { it.id == selectedUnitId }

                    val productToSave = Product(
                        id = if (isEdit && productId != null) productId else "prd_${System.currentTimeMillis()}",
                        code = code,
                        name = name,
                        sku = sku,
                        barcode = barcode,
                        categoryId = selectedCategoryId,
                        categoryName = catObj?.name ?: "Umum",
                        warehouseId = selectedWarehouseId,
                        warehouseName = whObj?.name ?: "Gudang Utama",
                        unitId = selectedUnitId,
                        unitName = unitObj?.symbol ?: "pcs",
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                        stock = initialStock.toIntOrNull() ?: 0,
                        minimumStock = minimumStock.toIntOrNull() ?: 5,
                        description = description,
                        qrCode = "PRODUCT:$code"
                    )

                    viewModel.saveProduct(productToSave, isEdit)
                    onBackClick()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_product_button")
            ) {
                Text("SIMPAN PRODUK", fontWeight = FontWeight.Bold)
            }
        }
    }
}
