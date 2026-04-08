package eu.tutorials.mywishlistapp.ui.navigation

sealed class Screen(val route: String) {
    object Splash   : Screen("splash")
    object Login    : Screen("login")
    object Register : Screen("register")
    object Home     : Screen("home")
    object QuizList : Screen("quiz_list")
    object History  : Screen("history")
    object AddQuiz  : Screen("add_quiz")

    object QuizPlay : Screen("quiz_play/{quizId}") {
        fun createRoute(quizId: Int) = "quiz_play/$quizId"
    }

    object Results : Screen("results/{score}/{total}/{quizId}") {
        fun createRoute(score: Int, total: Int, quizId: Int) =
            "results/$score/$total/$quizId"
    }
}