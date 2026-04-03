package eu.tutorials.mywishlistapp.data.remote

object SupabaseConfig {
    val baseUrl: String = "https://gfwdayffqjqbghotnata.supabase.co".trim().removeSuffix("/")
    val anonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imdmd2RheWZmcWpxYmdob3RuYXRhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzUyNDk4NzIsImV4cCI6MjA5MDgyNTg3Mn0.h8irp3fjCh6KWp3AhbRR2ouW4ZE3kjheXV5GeNvbBKM".trim()

    fun isConfigured(): Boolean {
        return baseUrl.isNotBlank() &&
                anonKey.isNotBlank()
    }
}