package com.smnc.sabaib.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReceiptItemRow(
    @SerialName("bill_id") val billId: String,
    @SerialName("original_name") val originalName: String,
    @SerialName("translated_name") val translatedName: String? = null,
    val quantity: Double,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("total_price") val totalPrice: Double
)
