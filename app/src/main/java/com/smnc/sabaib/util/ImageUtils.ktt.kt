package com.smnc.sabaib.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream

/**
 * Creates a fresh, empty file under the app's cache dir and returns a
 * content:// Uri for it via FileProvider - this is where the camera app
 * will write the full-resolution photo.
 */
fun createScanImageUri(context: Context): Uri {
    val scansDir = File(context.cacheDir, "scans").apply { mkdirs() }
    val file = File(scansDir, "receipt_${System.currentTimeMillis()}.jpg")

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

/**
 * Loads a Bitmap from [uri] and corrects for EXIF rotation. Camera photos
 * are frequently stored "sideways" with a rotation tag rather than
 * pre-rotated pixels, which throws off OCR if left uncorrected.
 */
fun loadRotatedBitmap(context: Context, uri: Uri): Bitmap? {
    val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input)
    } ?: return null

    val rotationDegrees = context.contentResolver
        .openInputStream(uri)
        ?.use { input -> readExifRotationDegrees(input) }
        ?: 0

    if (rotationDegrees == 0) return bitmap

    val matrix = Matrix().apply {
        postRotate(rotationDegrees.toFloat())
    }

    return Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
    )
}

private fun readExifRotationDegrees(input: InputStream): Int {
    val exif = ExifInterface(input)

    return when (
        exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}