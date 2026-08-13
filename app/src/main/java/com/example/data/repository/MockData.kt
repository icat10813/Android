package com.example.data.repository

import com.example.domain.model.*

object MockData {
    val initialCategories = listOf(
        Category("cat_1", "Elektronik & Gadget", "Komputer, HP, Aksesoris"),
        Category("cat_2", "Pakaian & Fashion", "Baju, Celana, Aksesoris"),
        Category("cat_3", "Makanan & Minuman", "Sembako, Snack, Minuman"),
        Category("cat_4", "Alat Tulis Kantor", "Kertas, Pena, Buku"),
        Category("cat_5", "Peralatan Rumah Tangga", "Sapu, Kebersihan, Dapur")
    )

    val initialUnits = listOf(
        ProductUnit("unit_1", "Pcs", "pcs"),
        ProductUnit("unit_2", "Box", "box"),
        ProductUnit("unit_3", "Karton", "ctn"),
        ProductUnit("unit_4", "Kilogram", "kg"),
        ProductUnit("unit_5", "Liter", "L")
    )

    val initialSuppliers = listOf(
        Supplier("sup_1", "SUP-001", "PT Distributor Utama Jaya", "081234567890", "sales@distributor.co.id", "Jl. Industry No. 45, Jakarta Pusat", "Pemasok Utama Sembako & Makanan"),
        Supplier("sup_2", "SUP-002", "CV Mega Tech Indonesia", "085678901234", "info@megatech.co.id", "Jl. Mangga Dua Raya No. 12, Jakarta Barat", "Distributor Resmi Gadget & Komputer"),
        Supplier("sup_3", "SUP-003", "Grosir Busana Sentosa", "087812345678", "contact@busanasentosa.com", "Jl. Tanah Abang Blok A No. 8, Jakarta Pusat", "Supplier Pakaian Konveksi")
    )

    val initialCustomers = listOf(
        Customer("cust_1", "CUST-001", "Pelanggan Umum", "-", "-", "-"),
        Customer("cust_2", "CUST-002", "Budi Santoso", "081298765432", "budi.santoso@gmail.com", "Jl. Melati No. 10, Bandung"),
        Customer("cust_3", "CUST-003", "Toko Sinar Jaya", "082134567891", "sinar.jaya@toko.com", "Jl. Sudirman No. 88, Surabaya"),
        Customer("cust_4", "CUST-004", "Siti Aminah", "083811223344", "siti.aminah@yahoo.com", "Jl. Mawar No. 4, Yogyakarta")
    )

    val initialWarehouses = listOf(
        Warehouse("wh_1", "GDG-01", "Gudang Utama Pusat", "Jl. Logistics Hub No. 1, Jakarta", "Ahmad Hidayat", "ACTIVE"),
        Warehouse("wh_2", "GDG-02", "Gudang Cabang Toko Depan", "Jl. Pemuda No. 15, Jakarta", "Rina Kartika", "ACTIVE"),
        Warehouse("wh_3", "GDG-03", "Gudang Transit Sembako", "Jl. Pelabuhan No. 88, Surabaya", "Hendra Wijaya", "ACTIVE")
    )

    val initialProducts = listOf(
        Product(
            id = "prod_1",
            name = "Wireless Mouse Bluetooth",
            code = "PRD-001",
            sku = "SKU-ELEK-001",
            barcode = "899123456001",
            qrCode = "PRODUCT:prod_1",
            categoryId = "cat_1",
            categoryName = "Elektronik & Gadget",
            unitId = "unit_1",
            unitName = "Pcs",
            purchasePrice = 75000.0,
            sellingPrice = 125000.0,
            stock = 45,
            minimumStock = 10,
            warehouseId = "wh_1",
            warehouseName = "Gudang Utama Pusat",
            image = "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=400&q=80",
            description = "Mouse nirkabel ergonomic dual mode Bluetooth 5.0 dan USB Dongle 2.4Ghz."
        ),
        Product(
            id = "prod_2",
            name = "Keyboard Mechanical RGB",
            code = "PRD-002",
            sku = "SKU-ELEK-002",
            barcode = "899123456002",
            qrCode = "PRODUCT:prod_2",
            categoryId = "cat_1",
            categoryName = "Elektronik & Gadget",
            unitId = "unit_1",
            unitName = "Pcs",
            purchasePrice = 320000.0,
            sellingPrice = 480000.0,
            stock = 8,
            minimumStock = 10, // Low stock alert!
            warehouseId = "wh_1",
            warehouseName = "Gudang Utama Pusat",
            image = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=400&q=80",
            description = "Keyboard mekanik 87 tombol RGB backlit dengan Blue Switch taktil."
        ),
        Product(
            id = "prod_3",
            name = "Kopi Arabika Premium 250g",
            code = "PRD-003",
            sku = "SKU-FOOD-001",
            barcode = "899123456003",
            qrCode = "PRODUCT:prod_3",
            categoryId = "cat_3",
            categoryName = "Makanan & Minuman",
            unitId = "unit_1",
            unitName = "Pcs",
            purchasePrice = 35000.0,
            sellingPrice = 65000.0,
            stock = 120,
            minimumStock = 15,
            warehouseId = "wh_2",
            warehouseName = "Gudang Cabang Toko Depan",
            image = "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?auto=format&fit=crop&w=400&q=80",
            description = "Biji kopi sangrai Arabika Gayo asli 100% dengan cita rasa fruity dan aroma tajam."
        ),
        Product(
            id = "prod_4",
            name = "Kaos Polos Cotton Combed 30s",
            code = "PRD-004",
            sku = "SKU-FASH-001",
            barcode = "899123456004",
            qrCode = "PRODUCT:prod_4",
            categoryId = "cat_2",
            categoryName = "Pakaian & Fashion",
            unitId = "unit_1",
            unitName = "Pcs",
            purchasePrice = 28000.0,
            sellingPrice = 55000.0,
            stock = 0, // Out of stock!
            minimumStock = 20,
            warehouseId = "wh_2",
            warehouseName = "Gudang Cabang Toko Depan",
            image = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=400&q=80",
            description = "Baju kaos polos katun halus combed 30s standar distro nyaman dipakai sehari-hari."
        ),
        Product(
            id = "prod_5",
            name = "Kertas A4 80gsm SiDU (1 Ream)",
            code = "PRD-005",
            sku = "SKU-ATK-001",
            barcode = "899123456005",
            qrCode = "PRODUCT:prod_5",
            categoryId = "cat_4",
            categoryName = "Alat Tulis Kantor",
            unitId = "unit_2",
            unitName = "Box",
            purchasePrice = 42000.0,
            sellingPrice = 58000.0,
            stock = 65,
            minimumStock = 10,
            warehouseId = "wh_1",
            warehouseName = "Gudang Utama Pusat",
            image = "https://images.unsplash.com/photo-1586075010923-2dd4570fb338?auto=format&fit=crop&w=400&q=80",
            description = "Kertas HVS putih A4 80 gsm isi 500 lembar untuk cetak dokumen jernih."
        ),
        Product(
            id = "prod_6",
            name = "Minyak Goreng Sawit 2 Liter",
            code = "PRD-006",
            sku = "SKU-FOOD-002",
            barcode = "899123456006",
            qrCode = "PRODUCT:prod_6",
            categoryId = "cat_3",
            categoryName = "Makanan & Minuman",
            unitId = "unit_5",
            unitName = "Liter",
            purchasePrice = 28000.0,
            sellingPrice = 36000.0,
            stock = 5,
            minimumStock = 12, // Low stock
            warehouseId = "wh_2",
            warehouseName = "Gudang Cabang Toko Depan",
            image = "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=400&q=80",
            description = "Minyak goreng kelapa sawit olahan murni kemasan pouch 2 Liter."
        )
    )

    val initialTransactions = listOf(
        Transaction(
            id = "POS-20260812-0001",
            date = System.currentTimeMillis() - 3600000 * 4,
            cashierName = "Rudi Kasir",
            cashierId = "usr_kasir",
            customerName = "Budi Santoso",
            items = listOf(
                TransactionItem("prod_1", "Wireless Mouse Bluetooth", "PRD-001", 125000.0, 2, 250000.0),
                TransactionItem("prod_3", "Kopi Arabika Premium 250g", "PRD-003", 65000.0, 1, 65000.0)
            ),
            subtotal = 315000.0,
            discount = 15000.0,
            tax = 0.0,
            additionalFee = 0.0,
            grandTotal = 300000.0,
            paidAmount = 300000.0,
            changeAmount = 0.0,
            paymentMethod = PaymentMethod.QRIS,
            syncStatus = SyncStatus.SYNCED
        ),
        Transaction(
            id = "POS-20260812-0002",
            date = System.currentTimeMillis() - 3600000 * 2,
            cashierName = "Rudi Kasir",
            cashierId = "usr_kasir",
            customerName = "Pelanggan Umum",
            items = listOf(
                TransactionItem("prod_5", "Kertas A4 80gsm SiDU (1 Ream)", "PRD-005", 58000.0, 3, 174000.0)
            ),
            subtotal = 174000.0,
            discount = 0.0,
            tax = 0.0,
            additionalFee = 0.0,
            grandTotal = 174000.0,
            paidAmount = 200000.0,
            changeAmount = 26000.0,
            paymentMethod = PaymentMethod.CASH,
            syncStatus = SyncStatus.SYNCED
        )
    )
}
