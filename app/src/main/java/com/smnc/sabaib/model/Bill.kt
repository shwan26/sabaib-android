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

    val total: Double = 0.0,

    val isVatIncluded: Boolean = false,

    val isSplitEvenly: Boolean = false,

    // Whether the host has made the evenly-vs-by-item call yet. Item taps
    // and the split-evenly toggle are both locked out for everyone until
    // this flips true, so no one can start claiming dishes before the host
    // has decided how the bill is being split at all.
    val splitDecided: Boolean = false,

    val promptPayQrUrl: String? = null,

    val stage: BillStage = BillStage.WAITING
)