package com.smnc.sabaib.data

import io.github.jan.supabase.storage.storage

private const val PROMPTPAY_QR_BUCKET = "promptpay-qrs"

class PromptPayQrRepository {

    private val bucket = SupabaseProvider.client.storage.from(PROMPTPAY_QR_BUCKET)

    suspend fun upload(path: String, bytes: ByteArray) {
        bucket.upload(path, bytes) { upsert = true }
    }

    fun publicUrl(path: String): String = bucket.publicUrl(path)
}
