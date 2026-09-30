package com.smnc.sabaib.model

data class ReceiptItem(
    val id: String,
    val originalName: String,
    val englishName: String,
    val quantity: Int = 1,
    val price: Double
)