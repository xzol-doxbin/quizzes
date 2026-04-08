package eu.tutorials.mywishlistapp.data.remote

import android.util.Log
import eu.tutorials.mywishlistapp.data.remote.model.OpenAiQuestionPayload
import eu.tutorials.mywishlistapp.data.remote.model.OpenAiQuizPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
class OpenAiQuizService(
    private val client: OkHttpClient
) {

    suspend fun generateQuiz(topic: String, questionCount: Int): OpenAiQuizPayload = withContext(Dispatchers.IO) {
        if (!OpenAiConfig.isConfigured()) {
            throw IllegalStateException(
                "Ключ OpenAI не заданий. Додай OPENAI_API_KEY у файл local.properties у корені проєкту."
            )
        }

        val count = questionCount.coerceIn(MIN_QUESTIONS, MAX_QUESTIONS)
        val systemPrompt = """
            Ти генеруєш дані для мобільного квізу. Відповідай ЛИШЕ валідним JSON без markdown і без пояснень навколо.
            Мова питань і відповідей: українська.
            Схема JSON:
            {
              "title": "коротка назва квізу",
              "description": "1-2 речення про квіз",
              "questions": [
                {
                  "text": "текст питання",
                  "options": ["варіант1","варіант2","варіант3","варіант4"],
                  "correctIndex": 0,
                  "explanation": "коротке пояснення чому ця відповідь правильна"
                }
              ]
            }
            Поле correctIndex — індекс правильного варіанту від 0 до 3.
            Рівно 4 варіанти в кожному питанні. Кількість питань: рівно $count.
        """.trimIndent()

        val userPrompt = "Тема квізу: $topic. Згенеруй рівно $count питань з однаковою складністю для загального рівня."

        val requestJson = JSONObject().apply {
            put("model", MODEL)
            put("temperature", 0.6)
            put("response_format", JSONObject().put("type", "json_object"))
            put(
                "messages",
                JSONArray().apply {
                    put(JSONObject().put("role", "system").put("content", systemPrompt))
                    put(JSONObject().put("role", "user").put("content", userPrompt))
                }
            )
        }

        val httpRequest = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${OpenAiConfig.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val responseBody = client.newCall(httpRequest).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val err = runCatching { JSONObject(body).optJSONObject("error")?.optString("message") }.getOrNull()
                Log.e(TAG, "OpenAI error ${response.code}: $body")
                throw IllegalStateException(err ?: "Помилка API OpenAI: ${response.code}")
            }
            body
        }

        val root = JSONObject(responseBody)
        val content = root.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        parseQuizJson(content)
    }

    private fun parseQuizJson(content: String): OpenAiQuizPayload {
        val obj = JSONObject(content.trim())
        val title = obj.optString("title").trim().ifBlank { "Квіз" }
        val description = obj.optString("description").trim()
        val arr = obj.optJSONArray("questions")
            ?: throw IllegalStateException("У відповіді немає масиву questions")

        val questions = mutableListOf<OpenAiQuestionPayload>()
        for (i in 0 until arr.length()) {
            val q = arr.getJSONObject(i)
            val text = q.optString("text").trim()
            if (text.isBlank()) continue

            val optsJson = q.optJSONArray("options")
                ?: throw IllegalStateException("Питання ${i + 1}: немає options")
            val options = mutableListOf<String>()
            for (o in 0 until optsJson.length()) {
                options += optsJson.getString(o).trim()
            }
            if (options.size != 4) {
                throw IllegalStateException("Питання ${i + 1}: потрібно рівно 4 варіанти, отримано ${options.size}")
            }

            var correct = q.optInt("correctIndex", 0)
            if (correct !in 0..3) correct = correct.coerceIn(0, 3)

            val explanation = q.optString("explanation").trim()
            questions += OpenAiQuestionPayload(
                text = text,
                options = options,
                correctIndex = correct,
                explanation = explanation
            )
        }

        if (questions.isEmpty()) {
            throw IllegalStateException("Модель не повернула жодного питання")
        }

        return OpenAiQuizPayload(title = title, description = description, questions = questions)
    }

    companion object {
        private const val TAG = "OpenAiQuizService"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val MODEL = "gpt-4o-mini"
        const val MIN_QUESTIONS = 3
        const val MAX_QUESTIONS = 10
    }
}
