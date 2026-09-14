package com.smnc.sabaib.data

import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType

private const val PROMPTPAY_QR_BUCKET = "promptpay-qrs"

class PromptPayQrRepository {

    private val bucket = SupabaseProvider.client.storage.from(PROMPTPAY_QR_BUCKET)

    suspend fun upload(path: String, bytes: ByteArray, contentType: String? = null) {
        bucket.upload(path, bytes) {
            upsert = true
            contentType?.let { this.contentType = ContentType.parse(it) }
        }
    }

    fun publicUrl(path: String): String = bucket.publicUrl(path)
}
