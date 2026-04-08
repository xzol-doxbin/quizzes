package eu.tutorials.mywishlistapp.data.remote.model

data class OpenAiQuizPayload(
    val title: String,
    val description: String,
    val questions: List<OpenAiQuestionPayload>
)

data class OpenAiQuestionPayload(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = ""
)
