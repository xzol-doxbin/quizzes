package eu.tutorials.mywishlistapp.data.remote.model

data class RemoteQuizDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val source: String,
    val createdAt: Long,
    val questions: List<RemoteQuestionDto>
)

data class RemoteQuestionDto(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)