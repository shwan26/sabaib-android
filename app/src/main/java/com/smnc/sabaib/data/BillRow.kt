package com.smnc.sabaib.data

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillRow(
    val id: String? = null,
    val code: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("restaurant_name") val restaurantName: String,
    val subtotal: Double,
    @SerialName("service_charge_percent") val serviceChargePercent: Double,
    @SerialName("service_charge_amount") val serviceChargeAmount: Double,
    @SerialName("vat_percent") val vatPercent: Double,
    @SerialName("vat_amount") val vatAmount: Double,
    @SerialName("discount_amount") val discountAmount: Double,
    @SerialName("total_amount") val totalAmount: Double,
    // No default: kotlinx.serialization omits properties that equal their
    // declared default (encodeDefaults = false), which would silently drop
    // this field and let Postgres fall back to its own column default
    // ('draft'), violating bills_status_check.
    val status: String,
    // No default, same rationale as [status] above - every insert should
    // state this explicitly rather than relying on encodeDefaults=false to
    // omit it and fall back to the column default.
    @SerialName("is_split_evenly") val isSplitEvenly: Boolean,
    @SerialName("delete_after")
    @Serializable(with = InstantColumnSerializer::class)
    val deleteAfter: Instant? = null
)
