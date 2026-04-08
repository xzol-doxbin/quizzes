package eu.tutorials.mywishlistapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eu.tutorials.mywishlistapp.data.local.entity.QuizResultEntity
import eu.tutorials.mywishlistapp.data.local.model.QuizResultListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: QuizResultEntity): Long

    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY completedAt DESC")
    fun getResultsForUser(userId: Int): Flow<List<QuizResultEntity>>

    @Query(
        """
        SELECT r.id, r.userId, r.quizId, r.score, r.totalQuestions, r.timeTakenSeconds, r.completedAt,
               q.title AS quizTitle
        FROM quiz_results AS r
        INNER JOIN quizzes AS q ON q.id = r.quizId
        WHERE r.userId = :userId
        ORDER BY r.completedAt DESC
        """
    )
    fun observeResultsWithQuizTitle(userId: Int): Flow<List<QuizResultListItem>>

    @Query("""
        SELECT AVG(CAST(score AS FLOAT) / totalQuestions) 
        FROM (SELECT score, totalQuestions FROM quiz_results 
              WHERE userId = :userId ORDER BY completedAt DESC LIMIT 3)
    """)
    suspend fun getRecentAverageRatio(userId: Int): Float?
}