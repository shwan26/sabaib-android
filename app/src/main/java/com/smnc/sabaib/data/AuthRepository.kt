package com.smnc.sabaib.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    private val auth = SupabaseProvider.client.auth

    suspend fun signUp(email: String, password: String, name: String, birthYear: Int) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            // Stored as user metadata; a DB trigger copies these to the matching
            // profiles columns. full_name is also the key Supabase Auth's own
            // dashboard reads for its "Display Name" column.
            data = buildJsonObject {
                put("display_name", name)
                put("full_name", name)
                put("birth_year", birthYear)
            }
        }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.resetPasswordForEmail(email, redirectUrl = "https://sabaib.vercel.app/reset-password")
    }

    fun isLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }

    fun currentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }

    fun currentUserEmail(): String? {
        return auth.currentUserOrNull()?.email
    }

    // Optional: observe session changes reactively (e.g. in a splash screen)
    fun sessionStatusFlow() = auth.sessionStatus
}