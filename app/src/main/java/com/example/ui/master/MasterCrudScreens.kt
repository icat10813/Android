package com.example.ui.master

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.InventoryRepository
import com.example.domain.model.Category
import com.example.domain.model.Customer
import com.example.domain.model.Supplier
import com.example.domain.model.Warehouse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDataScreen(
    masterType: String, // "CATEGORY", "SUPPLIER", "CUSTOMER", "WAREHOUSE"
    repository: InventoryRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val title = when (masterType) {
        "CATEGORY" -> "Kelola Kategori Barang"
        "SUPPLIER" -> "Kelola Supplier / Pemasok"
        "CUSTOMER" -> "Kelola Pelanggan"
        else -> "Kelola Gudang Penyimpanan"
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputDetail by remember { mutableStateOf("") }

    val categories by repository.getAllCategories().collectAsState(initial = emptyList())
    val suppliers by repository.getAllSuppliers().collectAsState(initial = emptyList())
    val warehouses by repository.getAllWarehouses().collectAsState(initial = emptyList())

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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    inputName = ""
                    inputDetail = ""
                    showAddDialog = true
                },
                modifier = Modifier.testTag("add_master_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (masterType) {
                    "CATEGORY" -> {
                        items(categories) { cat ->
                            MasterCardItem(title = cat.name, subtitle = cat.description)
                        }
                    }
                    "SUPPLIER" -> {
                        items(suppliers) { sup ->
                            MasterCardItem(title = sup.name, subtitle = "${sup.phone} • ${sup.address}")
                        }
                    }
                    "WAREHOUSE" -> {
                        items(warehouses) { wh ->
                            MasterCardItem(title = wh.name, subtitle = wh.address)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Tambah $title Baru") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Nama") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = inputDetail,
                            onValueChange = { inputDetail = it },
                            label = { Text("Detail / Alamat / Telepon") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputName.isBlank()) return@Button
                            scope.launch {
                                val timestamp = System.currentTimeMillis()
                                when (masterType) {
                                    "CATEGORY" -> repository.createCategory(
                                        Category(id = "cat_$timestamp", name = inputName, description = inputDetail)
                                    )
                                    "SUPPLIER" -> repository.createSupplier(
                                        Supplier(
                                            id = "sup_$timestamp",
                                            code = "SUP-$timestamp",
                                            name = inputName,
                                            phone = "08123456789",
                                            email = "supplier@mail.com",
                                            address = inputDetail,
                                            notes = ""
                                        )
                                    )
                                    "WAREHOUSE" -> repository.createWarehouse(
                                        Warehouse(
                                            id = "wh_$timestamp",
                                            code = "WH-$timestamp",
                                            name = inputName,
                                            address = inputDetail,
                                            pic = "Admin Gudang",
                                            status = "ACTIVE"
                                        )
                                    )
                                }
                                Toast.makeText(context, "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun MasterCardItem(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }
    }
}
