package com.smnc.sabaib.util

import android.content.Context

object OnboardingPrefs {
    private const val PREFS_NAME = "onboarding_prefs"
    private const val KEY_SEEN_LANDING = "seen_landing"

    fun hasSeenLanding(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SEEN_LANDING, false)

    fun markLandingSeen(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SEEN_LANDING, true)
            .apply()
    }
}
