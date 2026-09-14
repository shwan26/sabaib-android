package com.smnc.sabaib.data

import io.github.jan.supabase.postgrest.postgrest
import java.time.Instant
import java.time.temporal.ChronoUnit

class ProfileRepository {

    private val postgrest = SupabaseProvider.client.postgrest

    suspend fun getProfile(userId: String): Profile? =
        postgrest["profiles"].select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<Profile>()

    suspend fun updateDisplayName(userId: String, displayName: String) {
        postgrest["profiles"].update({
            Profile::displayName setTo displayName
        }) {
            filter { eq("id", userId) }
        }
    }

    suspend fun updatePlan(userId: String, plan: String) {
        postgrest["profiles"].update({
            Profile::plan setTo plan
        }) {
            filter { eq("id", userId) }
        }
    }

    sealed class ScanQuotaResult {
        data object Allowed : ScanQuotaResult()
        data object LimitReached : ScanQuotaResult()
    }

    /**
     * Checks [userId]'s rolling 30-day free-scan quota and, if a scan is
     * allowed, records it. No-ops (always Allowed) for premium users, who
     * aren't scan-limited.
     */
    suspend fun consumeFreeScan(userId: String): ScanQuotaResult {
        val profile = getProfile(userId) ?: return ScanQuotaResult.Allowed
        if (profile.plan != "free") return ScanQuotaResult.Allowed

        val now = Instant.now()
        val windowExpired = profile.freeScansResetAt == null || profile.freeScansResetAt.isBefore(now)

        if (windowExpired) {
            postgrest["profiles"].update({
                Profile::freeScansUsed setTo 1
                set("free_scans_reset_at", now.plus(30, ChronoUnit.DAYS).toString())
            }) {
                filter { eq("id", userId) }
            }
            return ScanQuotaResult.Allowed
        }

        if (profile.freeScansUsed >= FREE_SCAN_LIMIT) {
            return ScanQuotaResult.LimitReached
        }

        postgrest["profiles"].update({
            Profile::freeScansUsed setTo (profile.freeScansUsed + 1)
        }) {
            filter { eq("id", userId) }
        }
        return ScanQuotaResult.Allowed
    }

    companion object {
        const val FREE_SCAN_LIMIT = 1
    }
}
