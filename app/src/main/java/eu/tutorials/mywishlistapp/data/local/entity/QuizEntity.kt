package eu.tutorials.mywishlistapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: String? = null,
    /** null = спільний (Supabase, демо); інакше лише цей локальний користувач */
    val ownerUserId: Int? = null,
    val title: String,
    val description: String,
    val category: String = "Загальне",
    val source: String = "LOCAL",
    val createdAt: Long = System.currentTimeMillis()
)