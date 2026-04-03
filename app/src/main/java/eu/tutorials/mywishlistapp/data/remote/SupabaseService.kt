package eu.tutorials.mywishlistapp.data.remote

import android.util.Log
import eu.tutorials.mywishlistapp.data.remote.model.RemoteQuestionDto
import eu.tutorials.mywishlistapp.data.remote.model.RemoteQuizDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class SupabaseService(
    private val client: OkHttpClient
) {
    private val jsonMediaType = "application/json".toMediaType()

    fun fetchQuizzes(): List<RemoteQuizDto> {
        if (!SupabaseConfig.isConfigured()) {
            Log.e("SupabaseService", "Supabase не налаштований: перевір SUPABASE_URL / SUPABASE_ANON_KEY")
            return emptyList()
        }

        val quizzesUrl =
            "${SupabaseConfig.baseUrl}/rest/v1/quizzes?select=id,title,description,category,source,created_at"

        Log.d("SupabaseService", "Запит квізів: $quizzesUrl")

        val quizzesRequest = Request.Builder()
            .url(quizzesUrl)
            .get()
            .addHeader("apikey", SupabaseConfig.anonKey)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.anonKey}")
            .build()

        val quizzesJson = client.newCall(quizzesRequest).execute().use { response ->
            val body = response.body?.string().orEmpty()
            Log.d("SupabaseService", "Відповідь квізів code=${response.code} body=$body")

            if (!response.isSuccessful) {
                throw IllegalStateException("Не вдалося отримати квізи: ${response.code} $body")
            }
            body
        }

        val quizzesArray = JSONArray(quizzesJson)
        val result = mutableListOf<RemoteQuizDto>()

        for (i in 0 until quizzesArray.length()) {
            val quizObject = quizzesArray.getJSONObject(i)
            val remoteQuizId = quizObject.getString("id")

            val questionsUrl =
                "${SupabaseConfig.baseUrl}/rest/v1/questions" +
                        "?quiz_id=eq.$remoteQuizId" +
                        "&select=text,options,correct_index,explanation" +
                        "&order=question_order.asc"

            Log.d("SupabaseService", "Запит питань: $questionsUrl")

            val questionsRequest = Request.Builder()
                .url(questionsUrl)
                .get()
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.anonKey}")
                .build()

            val questionsJson = client.newCall(questionsRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                Log.d("SupabaseService", "Відповідь питань code=${response.code} body=$body")

                if (!response.isSuccessful) {
                    throw IllegalStateException("Не вдалося отримати питання: ${response.code} $body")
                }
                body
            }

            val questionsArray = JSONArray(questionsJson)
            val questions = mutableListOf<RemoteQuestionDto>()

            for (q in 0 until questionsArray.length()) {
                val questionObject = questionsArray.getJSONObject(q)
                val optionsJson = questionObject.getJSONArray("options")
                val options = mutableListOf<String>()

                for (o in 0 until optionsJson.length()) {
                    options += optionsJson.getString(o)
                }

                questions += RemoteQuestionDto(
                    text = questionObject.getString("text"),
                    options = options,
                    correctIndex = questionObject.getInt("correct_index"),
                    explanation = questionObject.optString("explanation", "")
                )
            }

            result += RemoteQuizDto(
                id = remoteQuizId,
                title = quizObject.getString("title"),
                description = quizObject.optString("description", ""),
                category = quizObject.optString("category", "Загальне"),
                source = quizObject.optString("source", "SUPABASE"),
                createdAt = quizObject.optLong("created_at", System.currentTimeMillis()),
                questions = questions
            )
        }

        Log.d("SupabaseService", "Отримано квізів: ${result.size}")
        return result
    }

    fun uploadQuizResult(
        username: String,
        userLocalId: Int,
        quizRemoteId: String?,
        quizTitle: String,
        score: Int,
        totalQuestions: Int,
        completedAt: Long
    ) {
        if (!SupabaseConfig.isConfigured()) {
            Log.e("SupabaseService", "uploadQuizResult: Supabase не налаштований")
            return
        }

        val payload = JSONObject().apply {
            put("username", username)
            put("user_local_id", userLocalId)
            put("quiz_remote_id", quizRemoteId?.let { JSONObject.wrap(it) } ?: JSONObject.NULL)
            put("quiz_title", quizTitle)
            put("score", score)
            put("total_questions", totalQuestions)
            put("completed_at", completedAt)
        }

        val request = Request.Builder()
            .url("${SupabaseConfig.baseUrl}/rest/v1/quiz_results")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .addHeader("apikey", SupabaseConfig.anonKey)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.anonKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            Log.d("SupabaseService", "uploadQuizResult code=${response.code} body=$body")

            if (!response.isSuccessful) {
                throw IllegalStateException("Не вдалося відправити результат: ${response.code} $body")
            }
        }
    }
}