package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.domain.model.Transaction
import java.io.File
import java.io.FileOutputStream

object PdfReceiptGenerator {

    fun generateReceiptPdf(context: Context, transaction: Transaction): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val centerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        var y = 30f

        // Header
        canvas.drawText("INVENTORY POS STORE", 150f, y, titlePaint)
        y += 18f
        canvas.drawText("Jl. Merdeka No. 123, Jakarta", 150f, y, centerPaint)
        y += 14f
        canvas.drawText("Telp: (021) 555-0199", 150f, y, centerPaint)
        y += 20f

        // Line
        canvas.drawLine(10f, y, 290f, y, paint)
        y += 15f

        // Details
        paint.textSize = 10f
        canvas.drawText("No. Trx : ${transaction.id}", 10f, y, paint)
        y += 14f
        canvas.drawText("Tgl     : ${DateFormatter.formatDate(transaction.date)}", 10f, y, paint)
        y += 14f
        canvas.drawText("Kasir   : ${transaction.cashierName}", 10f, y, paint)
        y += 14f
        canvas.drawText("Pelanggan: ${transaction.customerName}", 10f, y, paint)
        y += 15f

        // Line
        canvas.drawLine(10f, y, 290f, y, paint)
        y += 15f

        // Items Header
        paint.isFakeBoldText = true
        canvas.drawText("ITEM", 10f, y, paint)
        canvas.drawText("QTY", 180f, y, paint)
        canvas.drawText("SUBTOTAL", 220f, y, paint)
        paint.isFakeBoldText = false
        y += 12f

        // Items
        transaction.items.forEach { item ->
            canvas.drawText(item.productName.take(22), 10f, y, paint)
            y += 12f
            canvas.drawText("${item.qty} x ${CurrencyFormatter.formatRupiah(item.price)}", 20f, y, paint)
            canvas.drawText(CurrencyFormatter.formatRupiah(item.subtotal), 220f, y, paint)
            y += 16f
        }

        // Line
        canvas.drawLine(10f, y, 290f, y, paint)
        y += 15f

        // Totals
        canvas.drawText("Subtotal:", 130f, y, paint)
        canvas.drawText(CurrencyFormatter.formatRupiah(transaction.subtotal), 220f, y, paint)
        y += 14f

        if (transaction.discount > 0) {
            canvas.drawText("Diskon:", 130f, y, paint)
            canvas.drawText("-${CurrencyFormatter.formatRupiah(transaction.discount)}", 220f, y, paint)
            y += 14f
        }

        paint.isFakeBoldText = true
        canvas.drawText("TOTAL:", 130f, y, paint)
        canvas.drawText(CurrencyFormatter.formatRupiah(transaction.grandTotal), 220f, y, paint)
        paint.isFakeBoldText = false
        y += 14f

        canvas.drawText("Bayar (${transaction.paymentMethod}):", 130f, y, paint)
        canvas.drawText(CurrencyFormatter.formatRupiah(transaction.paidAmount), 220f, y, paint)
        y += 14f

        canvas.drawText("Kembali:", 130f, y, paint)
        canvas.drawText(CurrencyFormatter.formatRupiah(transaction.changeAmount), 220f, y, paint)
        y += 25f

        // Footer
        canvas.drawText("--- Terima kasih telah berbelanja ---", 150f, y, centerPaint)
        y += 14f
        canvas.drawText("Barang yang dibeli tidak dapat ditukar", 150f, y, centerPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Struk_${transaction.id}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
