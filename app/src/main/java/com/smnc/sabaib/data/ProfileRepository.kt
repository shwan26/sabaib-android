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

    /**
     * Records a free scan against [userId]'s rolling 30-day quota.
     * No-ops for premium users, who aren't scan-limited.
     */
    suspend fun incrementFreeScanUsageIfNeeded(userId: String) {
        val profile = getProfile(userId) ?: return
        if (profile.plan != "free") return

        val now = Instant.now()
        val windowExpired = profile.freeScansResetAt == null || profile.freeScansResetAt.isBefore(now)

        postgrest["profiles"].update({
            if (windowExpired) {
                Profile::freeScansUsed setTo 1
                set("free_scans_reset_at", now.plus(30, ChronoUnit.DAYS).toString())
            } else {
                Profile::freeScansUsed setTo (profile.freeScansUsed + 1)
            }
        }) {
            filter { eq("id", userId) }
        }
    }
}
