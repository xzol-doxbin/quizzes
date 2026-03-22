package eu.tutorials.mywishlistapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String = "Загальне",
    val source: String = "LOCAL",   // LOCAL | API | AI
    val createdAt: Long = System.currentTimeMillis()
)