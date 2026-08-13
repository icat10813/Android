package com.example.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace(",00", "")
    }

    fun formatNumber(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }
}

object DateFormatter {
    fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy, HH:mm"): String {
        val sdf = SimpleDateFormat(pattern, Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun generatePosTransactionNo(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomNum = (1000..9999).random()
        return "POS-$dateStr-$randomNum"
    }

    fun generateStockTransactionNo(prefix: String = "IN"): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomNum = (1000..9999).random()
        return "$prefix-$dateStr-$randomNum"
    }
}
