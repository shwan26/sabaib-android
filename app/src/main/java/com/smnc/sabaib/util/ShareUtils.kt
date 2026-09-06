package com.smnc.sabaib.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Fetches [qrUrl] through Coil (already warm from the on-screen AsyncImage),
 * writes it to the app cache, and opens the system share sheet with the
 * image plus [caption]. No-ops on fetch failure rather than crashing.
 */
suspend fun shareQrCode(context: Context, qrUrl: String, caption: String) {
    val request = ImageRequest.Builder(context)
        .data(qrUrl)
        .build()

    val result = SingletonImageLoader.get(context).execute(request)
    if (result !is SuccessResult) return

    val bitmap: Bitmap = result.image.toBitmap()

    val uri = withContext(Dispatchers.IO) {
        val qrDir = File(context.cacheDir, "qr").apply { mkdirs() }
        val file = File(qrDir, "promptpay_qr.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, caption)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(sendIntent, "Share PromptPay QR"))
}
