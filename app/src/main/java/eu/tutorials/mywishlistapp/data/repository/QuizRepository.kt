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
}