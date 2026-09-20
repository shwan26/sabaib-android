package com.smnc.sabaib.data

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ConsentRepository {

    private val postgrest = SupabaseProvider.client.postgrest

    suspend fun recordConsent(userId: String, consentType: String = "terms_and_privacy") {
        postgrest["user_consents"].insert(
            ConsentInsert(userId = userId, consentType = consentType, policyVersion = POLICY_VERSION)
        )
    }

    companion object {
        // Bump this whenever the Terms/Privacy content at sabaib.vercel.app changes.
        const val POLICY_VERSION = "v1.0-2026-09-13"
    }
}

@Serializable
private data class ConsentInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("consent_type") val consentType: String,
    @SerialName("policy_version") val policyVersion: String
)
