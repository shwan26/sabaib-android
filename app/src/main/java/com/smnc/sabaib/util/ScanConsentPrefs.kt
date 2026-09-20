package com.smnc.sabaib.util

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "scan_consent_prefs"
private const val KEY_DONT_SHOW_AGAIN = "gemini_dont_show_again"

fun hasSuppressedGeminiConsent(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_DONT_SHOW_AGAIN, false)

fun suppressGeminiConsent(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit { putBoolean(KEY_DONT_SHOW_AGAIN, true) }
}
