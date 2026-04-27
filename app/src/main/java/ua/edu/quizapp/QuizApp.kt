package ua.edu.quizapp

import android.app.Application
import ua.edu.quizapp.di.AppContainer

class QuizApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
