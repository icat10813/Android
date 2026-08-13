package com.example.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeUtils {

    fun generateQrBitmap(content: String, width: Int = 512, height: Int = 512): Bitmap? {
        if (content.isEmpty()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            }
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseScannedProductCode(scannedContent: String): String {
        val trimmed = scannedContent.trim()
        if (trimmed.startsWith("PRODUCT:")) {
            return trimmed.substringAfter("PRODUCT:")
        }
        if (trimmed.startsWith("{") && trimmed.contains("\"id\":")) {
            try {
                val regex = "\"id\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val match = regex.find(trimmed)
                if (match != null) {
                    return match.groupValues[1]
                }
            } catch (_: Exception) {}
        }
        return trimmed
    }
}
