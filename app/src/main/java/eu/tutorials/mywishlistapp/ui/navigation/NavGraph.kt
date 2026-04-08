package eu.tutorials.mywishlistapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eu.tutorials.mywishlistapp.ui.screens.addquiz.AddQuizScreen
import eu.tutorials.mywishlistapp.ui.screens.history.HistoryScreen
import eu.tutorials.mywishlistapp.ui.screens.home.HomeScreen
import eu.tutorials.mywishlistapp.ui.screens.login.LoginScreen
import eu.tutorials.mywishlistapp.ui.screens.login.RegisterScreen
import eu.tutorials.mywishlistapp.ui.screens.quizlist.QuizListScreen
import eu.tutorials.mywishlistapp.ui.screens.quizplay.QuizPlayScreen
import eu.tutorials.mywishlistapp.ui.screens.results.ResultsScreen
import eu.tutorials.mywishlistapp.ui.screens.splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onSessionFound = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNoSession = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToQuizList = { navController.navigate(Screen.QuizList.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToAddQuiz  = { navController.navigate(Screen.AddQuiz.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QuizList.route) {
            QuizListScreen(
                onQuizSelected = { quizId ->
                    navController.navigate(Screen.QuizPlay.createRoute(quizId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.QuizPlay.route,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { backStack ->
            val quizId = backStack.arguments?.getInt("quizId") ?: 0
            QuizPlayScreen(
                quizId = quizId,
                onQuizFinished = { score, total ->
                    navController.navigate(Screen.Results.createRoute(score, total, quizId)) {
                        popUpTo(Screen.QuizList.route)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Results.route,
            arguments = listOf(
                navArgument("score")  { type = NavType.IntType },
                navArgument("total")  { type = NavType.IntType },
                navArgument("quizId") { type = NavType.IntType }
            )
        ) { backStack ->
            val score  = backStack.arguments?.getInt("score")  ?: 0
            val total  = backStack.arguments?.getInt("total")  ?: 0
            val quizId = backStack.arguments?.getInt("quizId") ?: 0
            ResultsScreen(
                score  = score,
                total  = total,
                quizId = quizId,
                onHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetry = {
                    navController.navigate(Screen.QuizPlay.createRoute(quizId)) {
                        popUpTo(Screen.QuizList.route)
                    }
                }
            )
        }

        composable(Screen.AddQuiz.route) {
            AddQuizScreen(
                onSaved        = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}