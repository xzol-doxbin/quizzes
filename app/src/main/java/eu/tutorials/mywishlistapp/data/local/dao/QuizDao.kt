package eu.tutorials.mywishlistapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import eu.tutorials.mywishlistapp.data.local.entity.QuestionEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    suspend fun getAllQuizzesOnce(): List<QuizEntity>

    @Query(
        """
        SELECT * FROM quizzes
        WHERE ownerUserId IS NULL OR ownerUserId = :userId
        ORDER BY createdAt DESC
        """
    )
    fun getQuizzesVisibleToUser(userId: Int): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE ownerUserId IS NULL ORDER BY createdAt DESC")
    fun getGlobalQuizzesOnly(): Flow<List<QuizEntity>>

    @Query("SELECT COUNT(*) FROM quizzes WHERE ownerUserId IS NULL")
    suspend fun countGlobalQuizzes(): Int

    @Query("SELECT * FROM quizzes WHERE id = :id LIMIT 1")
    suspend fun getQuizById(id: Int): QuizEntity?

    @Query(
        """
        SELECT * FROM quizzes
        WHERE id = :quizId AND (ownerUserId IS NULL OR ownerUserId = :userId)
        LIMIT 1
        """
    )
    suspend fun getQuizByIdVisibleToUser(quizId: Int, userId: Int): QuizEntity?

    @Query("SELECT * FROM quizzes WHERE id = :quizId AND ownerUserId IS NULL LIMIT 1")
    suspend fun getQuizByIdIfGlobal(quizId: Int): QuizEntity?

    @Query("SELECT * FROM quizzes WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getQuizByRemoteId(remoteId: String): QuizEntity?

    @Query("SELECT * FROM questions WHERE quizId = :quizId ORDER BY id ASC")
    suspend fun getQuestionsForQuiz(quizId: Int): List<QuestionEntity>

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: Int)
}