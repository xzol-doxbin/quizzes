package ua.edu.quizapp.data.local.model

data class QuizResultListItem(
    val id: Int,
    val userId: Int,
    val quizId: Int,
    val score: Int,
    val totalQuestions: Int,
    val timeTakenSeconds: Int,
    val completedAt: Long,
    val quizTitle: String
)

