package ua.edu.quizapp.ui.screens.quizplay

import ua.edu.quizapp.data.local.entity.QuestionEntity
import kotlin.math.abs

class AdaptiveQuizEngine {
    fun initialOrder(
        questions: List<QuestionEntity>,
        baselineRatio: Float
    ): List<QuestionEntity> {
        if (questions.isEmpty()) return questions
        return when {
            baselineRatio < 0.35f -> questions.sortedWith(
                compareBy<QuestionEntity> { estimateDifficulty(it) }
                    .thenBy { it.id }
            )
            baselineRatio > 0.75f -> questions.sortedWith(
                compareByDescending<QuestionEntity> { estimateDifficulty(it) }
                    .thenBy { it.id }
            )
            else -> {
                val target = targetDifficultyForRatio(baselineRatio)
                questions.sortedWith(
                    compareBy<QuestionEntity> { abs(estimateDifficulty(it) - target) }
                        .thenBy { it.id }
                )
            }
        }
    }

    fun reorderRemaining(
        orderedQuestions: List<QuestionEntity>,
        currentQuestionIndex: Int,
        currentAccuracyRatio: Float
    ): List<QuestionEntity> {
        if (orderedQuestions.size < 3) return orderedQuestions
        if (currentQuestionIndex !in orderedQuestions.indices) return orderedQuestions
        if (currentQuestionIndex == orderedQuestions.lastIndex) return orderedQuestions

        val fixedPrefix = orderedQuestions.take(currentQuestionIndex + 1)
        val remaining = orderedQuestions.drop(currentQuestionIndex + 1)
        val target = targetDifficultyForRatio(currentAccuracyRatio)
        val reorderedTail = remaining.sortedWith(
            compareBy<QuestionEntity> { abs(estimateDifficulty(it) - target) }
                .thenBy { it.id }
        )
        return fixedPrefix + reorderedTail
    }

    fun estimateDifficulty(question: QuestionEntity): Float {
        val textScore = normalized(question.text.length, 30f, 200f)
        val options = parseOptions(question.optionsJson)
        val avgOptionLength = if (options.isEmpty()) 0f else options.sumOf { it.length }.toFloat() / options.size
        val optionScore = normalized(avgOptionLength, 6f, 40f)
        val explanationScore = if (question.explanation.isBlank()) 0f else 0.12f
        val combined = (textScore * 0.45f) + (optionScore * 0.43f) + explanationScore
        return combined.coerceIn(0f, 1f)
    }

    private fun targetDifficultyForRatio(ratio: Float): Float {
        return when {
            ratio >= 0.8f -> 0.85f
            ratio >= 0.65f -> 0.7f
            ratio >= 0.45f -> 0.55f
            ratio >= 0.25f -> 0.35f
            else -> 0.2f
        }
    }

    private fun normalized(value: Int, min: Float, max: Float): Float =
        normalized(value.toFloat(), min, max)

    private fun normalized(value: Float, min: Float, max: Float): Float {
        if (max <= min) return 0f
        return ((value - min) / (max - min)).coerceIn(0f, 1f)
    }

    private fun parseOptions(optionsJson: String): List<String> {
        return optionsJson
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim().removePrefix("\"").removeSuffix("\"") }
            .filter { it.isNotBlank() }
    }
}
