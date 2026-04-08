package eu.tutorials.mywishlistapp.di

import android.content.Context
import androidx.room.Room
import eu.tutorials.mywishlistapp.data.local.QuizDatabase
import eu.tutorials.mywishlistapp.data.local.SessionManager
import eu.tutorials.mywishlistapp.data.remote.OpenAiQuizService
import eu.tutorials.mywishlistapp.data.remote.SupabaseService
import eu.tutorials.mywishlistapp.data.repository.QuizRepository
import eu.tutorials.mywishlistapp.data.repository.UserRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val database: QuizDatabase = Room.databaseBuilder(
        context,
        QuizDatabase::class.java,
        "quiz_db"
    ).fallbackToDestructiveMigration().build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val supabaseService = SupabaseService(okHttpClient)

    val openAiQuizService = OpenAiQuizService(okHttpClient)

    val sessionManager = SessionManager(context)

    val userRepository = UserRepository(database.userDao())

    val quizRepository = QuizRepository(
        quizDao = database.quizDao(),
        quizResultDao = database.quizResultDao(),
        userDao = database.userDao(),
        supabaseService = supabaseService,
        sessionManager = sessionManager
    )
}