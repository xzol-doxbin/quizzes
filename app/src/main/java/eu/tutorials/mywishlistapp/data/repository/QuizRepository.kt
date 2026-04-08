package eu.tutorials.mywishlistapp.data.repository

import android.util.Log
import eu.tutorials.mywishlistapp.data.local.SessionManager
import eu.tutorials.mywishlistapp.data.local.dao.QuizDao
import eu.tutorials.mywishlistapp.data.local.dao.QuizResultDao
import eu.tutorials.mywishlistapp.data.local.dao.UserDao
import eu.tutorials.mywishlistapp.data.local.entity.QuestionEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizResultEntity
import eu.tutorials.mywishlistapp.data.local.model.QuizResultListItem
import eu.tutorials.mywishlistapp.data.remote.SupabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class QuizRepository(
    private val quizDao: QuizDao,
    private val quizResultDao: QuizResultDao,
    private val userDao: UserDao,
    private val supabaseService: SupabaseService,
    private val sessionManager: SessionManager
) {
    fun getAllQuizzes(): Flow<List<QuizEntity>> = quizDao.getAllQuizzes()

    fun observeQuizzesForUser(userId: Int): Flow<List<QuizEntity>> =
        if (userId > 0) quizDao.getQuizzesVisibleToUser(userId)
        else quizDao.getGlobalQuizzesOnly()

    suspend fun getQuizById(id: Int): QuizEntity? = quizDao.getQuizById(id)

    suspend fun getQuizByIdVisibleToUser(quizId: Int, userId: Int): QuizEntity? =
        if (userId > 0) quizDao.getQuizByIdVisibleToUser(quizId, userId)
        else quizDao.getQuizByIdIfGlobal(quizId)

    suspend fun getQuestionsForQuiz(quizId: Int): List<QuestionEntity> =
        quizDao.getQuestionsForQuiz(quizId)

    suspend fun saveQuizWithQuestions(quiz: QuizEntity, questions: List<QuestionEntity>): Int {
        val quizId = quizDao.insertQuiz(quiz).toInt()
        quizDao.insertQuestions(questions.map { it.copy(quizId = quizId) })
        return quizId
    }

    suspend fun deleteQuiz(quiz: QuizEntity) = quizDao.deleteQuiz(quiz)

    suspend fun saveResult(result: QuizResultEntity) {
        val sessionUserId = sessionManager.userId.first()
        val sessionUsername = sessionManager.username.first()

        val validUser = when {
            sessionUserId > 0 -> {
                userDao.getUserById(sessionUserId)
                    ?: if (sessionUsername.isNotBlank()) userDao.getUserByUsername(sessionUsername) else null
            }
            sessionUsername.isNotBlank() -> {
                userDao.getUserByUsername(sessionUsername)
            }
            else -> null
        }

        if (validUser == null) {
            Log.e(
                "QuizRepository",
                "saveResult skipped: valid user not found. sessionUserId=$sessionUserId username=$sessionUsername"
            )
            return
        }

        val quiz = getQuizByIdVisibleToUser(result.quizId, validUser.id)
        if (quiz == null) {
            Log.e("QuizRepository", "saveResult skipped: квіз недоступний для userId=${validUser.id} quizId=${result.quizId}")
            return
        }

        val safeResult = result.copy(
            userId = validUser.id,
            quizId = quiz.id
        )

        runCatching {
            quizResultDao.insertResult(safeResult)
        }.onFailure {
            Log.e(
                "QuizRepository",
                "saveResult local insert failed: userId=${safeResult.userId} quizId=${safeResult.quizId} error=${it.message}",
                it
            )
            return
        }

        runCatching {
            withContext(Dispatchers.IO) {
                supabaseService.uploadQuizResult(
                    username = validUser.username,
                    userLocalId = validUser.id,
                    quizRemoteId = quiz.remoteId,
                    quizTitle = quiz.title,
                    score = safeResult.score,
                    totalQuestions = safeResult.totalQuestions,
                    completedAt = safeResult.completedAt
                )
            }
        }.onFailure {
            Log.e("QuizRepository", "uploadQuizResult failed: ${it.message}", it)
        }
    }

    fun getResultsForUser(userId: Int): Flow<List<QuizResultEntity>> =
        quizResultDao.getResultsForUser(userId)

    fun observeResultsWithQuizTitles(userId: Int): Flow<List<QuizResultListItem>> =
        quizResultDao.observeResultsWithQuizTitle(userId)

    suspend fun getRecentAverageRatio(userId: Int): Float =
        quizResultDao.getRecentAverageRatio(userId) ?: 0f

    suspend fun seedDemoQuizzesIfEmpty() {
        if (quizDao.countGlobalQuizzes() > 0) return

        saveQuizWithQuestions(
            quiz = QuizEntity(
                ownerUserId = null,
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
                )
            )
        )
    }

    suspend fun syncSupabaseQuizzes() = withContext(Dispatchers.IO) {
        val remoteQuizzes = supabaseService.fetchQuizzes()
        if (remoteQuizzes.isEmpty()) {
            Log.d("QuizRepository", "syncSupabaseQuizzes: remote list is empty")
            return@withContext
        }

        remoteQuizzes.forEach { remoteQuiz ->
            val existingQuiz = quizDao.getQuizByRemoteId(remoteQuiz.id)

            val localQuizId = if (existingQuiz == null) {
                quizDao.insertQuiz(
                    QuizEntity(
                        remoteId = remoteQuiz.id,
                        ownerUserId = null,
                        title = remoteQuiz.title,
                        description = remoteQuiz.description,
                        category = remoteQuiz.category,
                        source = "SUPABASE",
                        createdAt = remoteQuiz.createdAt
                    )
                ).toInt()
            } else {
                quizDao.updateQuiz(
                    existingQuiz.copy(
                        ownerUserId = null,
                        title = remoteQuiz.title,
                        description = remoteQuiz.description,
                        category = remoteQuiz.category,
                        source = "SUPABASE",
                        createdAt = remoteQuiz.createdAt
                    )
                )
                existingQuiz.id
            }

            quizDao.deleteQuestionsForQuiz(localQuizId)

            val localQuestions = remoteQuiz.questions.map { remoteQuestion ->
                QuestionEntity(
                    quizId = localQuizId,
                    text = remoteQuestion.text,
                    optionsJson = toOptionsJson(remoteQuestion.options),
                    correctIndex = remoteQuestion.correctIndex,
                    explanation = remoteQuestion.explanation
                )
            }

            quizDao.insertQuestions(localQuestions)
        }

        Log.d("QuizRepository", "syncSupabaseQuizzes: synced ${remoteQuizzes.size} quizzes")
    }

    private fun toOptionsJson(options: List<String>): String {
        return options.joinToString(
            prefix = "[\"",
            postfix = "\"]",
            separator = "\",\""
        ) { it.replace("\"", "\\\"") }
    }
}