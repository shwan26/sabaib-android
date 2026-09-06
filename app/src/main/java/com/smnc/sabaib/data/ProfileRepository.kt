package com.smnc.sabaib.data

import io.github.jan.supabase.postgrest.postgrest

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
}
