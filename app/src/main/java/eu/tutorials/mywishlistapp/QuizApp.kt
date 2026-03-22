package eu.tutorials.mywishlistapp

import android.app.Application
import eu.tutorials.mywishlistapp.di.AppContainer

class QuizApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}