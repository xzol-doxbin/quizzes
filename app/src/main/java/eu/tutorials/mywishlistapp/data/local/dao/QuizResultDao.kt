package eu.tutorials.mywishlistapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eu.tutorials.mywishlistapp.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: QuizResultEntity): Long

    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY completedAt DESC")
    fun getResultsForUser(userId: Int): Flow<List<QuizResultEntity>>

    @Query("""
        SELECT AVG(CAST(score AS FLOAT) / totalQuestions) 
        FROM (SELECT score, totalQuestions FROM quiz_results 
              WHERE userId = :userId ORDER BY completedAt DESC LIMIT 3)
    """)
    suspend fun getRecentAverageRatio(userId: Int): Float?
}