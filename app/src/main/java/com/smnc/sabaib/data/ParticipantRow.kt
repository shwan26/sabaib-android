package com.smnc.sabaib.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantRow(
    val id: String? = null,
    @SerialName("bill_id") val billId: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    // No default: every call site should state "host" or "member"
    // explicitly rather than silently relying on the DB default.
    val role: String,
    @SerialName("is_ready") val isReady: Boolean = false,
    @SerialName("guest_token_hash") val guestTokenHash: String? = null
)
