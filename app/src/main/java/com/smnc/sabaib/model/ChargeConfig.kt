package com.smnc.sabaib.model

data class ChargeConfig(
    val serviceChargeRate: Double = 0.0,
    val vatRate: Double = 0.0,
    val discountAmount: Double = 0.0,
    val isVatIncluded: Boolean = false
)
