package eu.tutorials.mywishlistapp.data.repository

import eu.tutorials.mywishlistapp.data.local.dao.QuizDao
import eu.tutorials.mywishlistapp.data.local.dao.QuizResultDao
import eu.tutorials.mywishlistapp.data.local.entity.QuestionEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

class QuizRepository(
    private val quizDao: QuizDao,
    private val quizResultDao: QuizResultDao
) {
    fun getAllQuizzes(): Flow<List<QuizEntity>> = quizDao.getAllQuizzes()

    suspend fun getQuizById(id: Int): QuizEntity? = quizDao.getQuizById(id)

    suspend fun getQuestionsForQuiz(quizId: Int): List<QuestionEntity> =
        quizDao.getQuestionsForQuiz(quizId)

    suspend fun saveQuizWithQuestions(quiz: QuizEntity, questions: List<QuestionEntity>): Int {
        val quizId = quizDao.insertQuiz(quiz).toInt()
        quizDao.insertQuestions(questions.map { it.copy(quizId = quizId) })
        return quizId
    }

    suspend fun deleteQuiz(quiz: QuizEntity) = quizDao.deleteQuiz(quiz)

    suspend fun saveResult(result: QuizResultEntity) = quizResultDao.insertResult(result)

    fun getResultsForUser(userId: Int): Flow<List<QuizResultEntity>> =
        quizResultDao.getResultsForUser(userId)

    suspend fun getRecentAverageRatio(userId: Int): Float =
        quizResultDao.getRecentAverageRatio(userId) ?: 0f

    suspend fun seedDemoQuizzesIfEmpty() {
        val existing = quizDao.getAllQuizzesOnce()
        if (existing.isNotEmpty()) return

        saveQuizWithQuestions(
            quiz = QuizEntity(
                title = "Kotlin Basics",
                description = "5 базових питань по Kotlin",
                category = "Програмування",
                source = "LOCAL"
            ),
            questions = listOf(
                QuestionEntity(
                    quizId = 0,
                    text = "Яке ключове слово використовується для оголошення незмінної змінної в Kotlin?",
                    optionsJson = toOptionsJson(listOf("var", "val", "let", "const")),
                    correctIndex = 1,
                    explanation = "val створює read-only змінну."
                ),
                QuestionEntity(
                    quizId = 0,
                    text = "Яка функція потрібна, щоб перетворити nullable String у Int без винятку?",
                    optionsJson = toOptionsJson(listOf("toInt()", "parseInt()", "toIntOrNull()", "safeInt()")),
                    correctIndex = 2,
                    explanation = "toIntOrNull() повертає null замість exception."
                ),
                QuestionEntity(
                    quizId = 0,
                    text = "Який блок коду виконується, коли умова істинна?",
                    optionsJson = toOptionsJson(listOf("when", "if", "for", "try")),
                    correctIndex = 1,
                    explanation = "if використовується для перевірки умови."
                ),
                QuestionEntity(
                    quizId = 0,
                    text = "Який тип колекції зберігає унікальні значення?",
                    optionsJson = toOptionsJson(listOf("List", "Map", "Set", "Array")),
                    correctIndex = 2,
                    explanation = "Set не допускає дублювання елементів."
                ),
                QuestionEntity(
                    quizId = 0,
                    text = "Що таке Jetpack Compose?",
                    optionsJson = toOptionsJson(
                        listOf(
                            "ORM для SQLite",
                            "Декларативний UI toolkit",
                            "Бібліотека для мережі",
                            "Система DI"
                        )
                    ),
                    correctIndex = 1,
                    explanation = "Jetpack Compose — сучасний декларативний UI toolkit для Android."
                )
            )
        )

        saveQuizWithQuestions(
            quiz = QuizEntity(
                title = "Android Fundamentals",
                description = "Перевір базові знання Android",
                category = "Android",
                source = "LOCAL"
            ),
            questions = listOf(
                QuestionEntity(
                    quizId = 0,
                    text = "Який файл описує екранні переходи у Compose Navigation у цьому проєкті?",
                    optionsJson = toOptionsJson(listOf("Theme.kt", "Screen.kt", "NavGraph.kt", "MainActivity.kt")),
                    correctIndex = 2,
                    explanation = "NavGraph.kt містить конфігурацію навігації."
                ),
                QuestionEntity(
                    quizId = 0,
                    text = "Що використовується як локальна БД у вимогах?",
                    optionsJson = toOptionsJson(listOf("Firebase", "Realm", "SQLite через Room", "PostgreSQL")),
                    correctIndex = 2,
                    explanation = "У вимогах вказано SQLite через Room."
                ),
                QuestionEntity(
                    quizId = 0,
                    text = "Який компонент зберігає сесію користувача у цьому проєкті?",
                    optionsJson = toOptionsJson(listOf("Retrofit", "SessionManager", "NavController", "SnackbarHost")),
                    correctIndex = 1,
                    explanation = "SessionManager працює через DataStore."
                )
            )
        )
    }

    private fun toOptionsJson(options: List<String>): String {
        return options.joinToString(
            prefix = "[\"",
            postfix = "\"]",
            separator = "\",\""
        ) { it.replace("\"", "\\\"") }
    }
}