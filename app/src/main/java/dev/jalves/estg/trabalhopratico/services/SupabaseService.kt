package dev.jalves.estg.trabalhopratico.services

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import dev.jalves.estg.trabalhopratico.BuildConfig
import io.github.jan.supabase.auth.Auth

object SupabaseService {
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Storage)
        install(Auth)
        install(io.github.jan.supabase.functions.Functions)
    }
}