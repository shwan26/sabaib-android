package com.smnc.sabaib.data

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemClaimRow(
    val id: String? = null,
    @SerialName("item_id") val itemId: String,
    @SerialName("participant_id") val participantId: String,
    val share: Double = 1.0,
    @SerialName("created_at")
    @Serializable(with = InstantColumnSerializer::class)
    val createdAt: Instant? = null
)
