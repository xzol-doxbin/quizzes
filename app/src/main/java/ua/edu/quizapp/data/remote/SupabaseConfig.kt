package ua.edu.quizapp.data.remote

import ua.edu.quizapp.BuildConfig

object SupabaseConfig {
    val baseUrl: String = BuildConfig.SUPABASE_URL.trim().removeSuffix("/")
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    fun isConfigured(): Boolean {
        return baseUrl.isNotBlank() &&
                anonKey.isNotBlank()
    }
}
