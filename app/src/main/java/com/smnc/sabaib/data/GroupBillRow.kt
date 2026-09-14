package com.smnc.sabaib.data

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupBillRow(
    val id: String,
    @SerialName("restaurant_name") val restaurantName: String? = null,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("settled_at")
    @Serializable(with = InstantColumnSerializer::class)
    val settledAt: Instant? = null,
    @SerialName("created_at")
    @Serializable(with = InstantColumnSerializer::class)
    val createdAt: Instant? = null
)
