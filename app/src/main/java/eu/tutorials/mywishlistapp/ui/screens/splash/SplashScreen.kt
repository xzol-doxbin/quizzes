package eu.tutorials.mywishlistapp.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import eu.tutorials.mywishlistapp.QuizApp

@Composable
fun SplashScreen(
    onSessionFound: () -> Unit,
    onNoSession: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = (context.applicationContext as QuizApp).container.sessionManager

    LaunchedEffect(Unit) {
        if (sessionManager.isLoggedIn()) onSessionFound() else onNoSession()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}