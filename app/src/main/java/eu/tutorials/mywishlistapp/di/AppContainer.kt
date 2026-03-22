package eu.tutorials.mywishlistapp.di

import android.content.Context
import androidx.room.Room
import eu.tutorials.mywishlistapp.data.local.QuizDatabase
import eu.tutorials.mywishlistapp.data.local.SessionManager
import eu.tutorials.mywishlistapp.data.repository.QuizRepository
import eu.tutorials.mywishlistapp.data.repository.UserRepository

class AppContainer(context: Context) {

    private val database: QuizDatabase = Room.databaseBuilder(
        context,
        QuizDatabase::class.java,
        "quiz_db"
    ).fallbackToDestructiveMigration().build()

    val sessionManager = SessionManager(context)

    val userRepository  = UserRepository(database.userDao())
    val quizRepository  = QuizRepository(database.quizDao(), database.quizResultDao())
}