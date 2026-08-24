package com.smnc.sabaib.data

import com.smnc.sabaib.data.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {

    private val auth = SupabaseProvider.client.auth

    suspend fun signUp(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
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

    fun isLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }

    fun currentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }

    // Optional: observe session changes reactively (e.g. in a splash screen)
    fun sessionStatusFlow() = auth.sessionStatus
}