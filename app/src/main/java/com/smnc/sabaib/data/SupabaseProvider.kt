package com.smnc.sabaib.data

import com.smnc.sabaib.BuildConfig
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.HttpTimeout

object SupabaseProvider {

    @OptIn(SupabaseInternal::class)
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth.Companion)
        install(Postgrest.Companion)
        install(Realtime.Companion)
        install(Storage.Companion)
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
            }
        }
    }
}