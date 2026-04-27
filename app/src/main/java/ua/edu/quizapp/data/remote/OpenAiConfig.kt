package ua.edu.quizapp.data.remote

import ua.edu.quizapp.BuildConfig

object OpenAiConfig {
    val apiKey: String = BuildConfig.OPENAI_API_KEY.trim()

    fun isConfigured(): Boolean = apiKey.isNotBlank()
}

