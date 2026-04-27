package ua.edu.quizapp.di

import android.content.Context
import androidx.room.Room
import ua.edu.quizapp.data.local.QuizDatabase
import ua.edu.quizapp.data.local.SessionManager
import ua.edu.quizapp.data.remote.OpenAiQuizService
import ua.edu.quizapp.data.remote.SupabaseService
import ua.edu.quizapp.data.repository.QuizRepository
import ua.edu.quizapp.data.repository.UserRepository
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
