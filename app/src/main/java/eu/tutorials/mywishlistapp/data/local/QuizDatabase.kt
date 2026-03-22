package eu.tutorials.mywishlistapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import eu.tutorials.mywishlistapp.data.local.dao.QuizDao
import eu.tutorials.mywishlistapp.data.local.dao.QuizResultDao
import eu.tutorials.mywishlistapp.data.local.dao.UserDao
import eu.tutorials.mywishlistapp.data.local.entity.QuestionEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizResultEntity
import eu.tutorials.mywishlistapp.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        QuizEntity::class,
        QuestionEntity::class,
        QuizResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun quizDao(): QuizDao
    abstract fun quizResultDao(): QuizResultDao
}