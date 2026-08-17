package com.smnc.sabaib.model

data class Bill(
    val id: String,
    val code: String,
    val restaurantName: String = "",
    val items: List<ReceiptItem> = emptyList(),

    val subtotal: Double = 0.0,

    val serviceChargeRate: Double = 0.0,
    val serviceChargeAmount: Double = 0.0,

    val vatRate: Double = 0.0,
    val vatAmount: Double = 0.0,

    val discount: Double = 0.0,

    val total: Double = 0.0
)