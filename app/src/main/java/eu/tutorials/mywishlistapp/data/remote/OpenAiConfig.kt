package eu.tutorials.mywishlistapp.data.remote

import eu.tutorials.mywishlistapp.BuildConfig

object OpenAiConfig {
    val apiKey: String = BuildConfig.OPENAI_API_KEY.trim()

    fun isConfigured(): Boolean = apiKey.isNotBlank()
}
