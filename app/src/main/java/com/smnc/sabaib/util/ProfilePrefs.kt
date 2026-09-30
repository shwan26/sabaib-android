package com.smnc.sabaib.util

import android.content.Context
import androidx.core.content.edit

/**
 * Durable, on-device holder for the name entered at signup. Needed because
 * this Supabase project requires email confirmation: `signUpWith(Email)`
 * doesn't establish an active session immediately, so there's no session to
 * write `profiles.display_name` with right after signup. The name is saved
 * here and applied once a real session appears (see AppNavHost).
 */
object ProfilePrefs {
    private const val PREFS_NAME = "profile_prefs"
    private const val KEY_PENDING_DISPLAY_NAME = "pending_display_name"

    fun savePendingDisplayName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_PENDING_DISPLAY_NAME, name)
            }
    }

    /** Returns the pending name, if any, and clears it so it's applied at most once. */
    fun takePendingDisplayName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_PENDING_DISPLAY_NAME, null)
        if (name != null) {
            prefs.edit().remove(KEY_PENDING_DISPLAY_NAME).apply()
        }
        return name
    }
}
