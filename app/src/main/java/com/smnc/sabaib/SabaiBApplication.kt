package com.smnc.sabaib

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

class SabaiBApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Skipped until a real key is set (e.g. pre-launch, RevenueCat dashboard not configured yet) —
        // Purchases.configure() throws on a blank key, and would otherwise crash the whole app on startup.
        if (!Purchases.isConfigured && BuildConfig.REVENUECAT_API_KEY.isNotBlank()) {
            Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.INFO
            Purchases.configure(
                PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY).build()
            )
        }
    }
}
