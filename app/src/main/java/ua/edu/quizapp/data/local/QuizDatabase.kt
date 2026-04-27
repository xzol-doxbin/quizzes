package ua.edu.quizapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ua.edu.quizapp.data.local.dao.QuizDao
import ua.edu.quizapp.data.local.dao.QuizResultDao
import ua.edu.quizapp.data.local.dao.UserDao
import ua.edu.quizapp.data.local.entity.QuestionEntity
import ua.edu.quizapp.data.local.entity.QuizEntity
import ua.edu.quizapp.data.local.entity.QuizResultEntity
import ua.edu.quizapp.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        QuizEntity::class,
        QuestionEntity::class,
        QuizResultEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun quizDao(): QuizDao
    abstract fun quizResultDao(): QuizResultDao
}
