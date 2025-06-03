package dev.jalves.estg.trabalhopratico.services

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.auth.Auth
import dev.jalves.estg.trabalhopratico.BuildConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalSettings

object SupabaseAdminService {
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth) {
            minimalSettings()
        }
    }

    suspend fun initAdminSession() {
        supabase.auth.importAuthToken(BuildConfig.SUPABASE_ADMINKEY)
    }
}
