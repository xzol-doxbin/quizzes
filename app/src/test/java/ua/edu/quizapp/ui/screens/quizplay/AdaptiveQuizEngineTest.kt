package ua.edu.quizapp.ui.screens.quizplay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.edu.quizapp.data.local.entity.QuestionEntity

class AdaptiveQuizEngineTest {
    private val engine = AdaptiveQuizEngine()

    @Test fun difficulty_in_range_1() { assertInRange(questionOf(1, "Short?", listOf("a", "b", "c", "d"), "")) }
    @Test fun difficulty_in_range_2() { assertInRange(questionOf(2, "This is a medium sized question text for testing.", listOf("aa", "bb", "cc", "dd"), "")) }
    @Test fun difficulty_in_range_3() { assertInRange(questionOf(3, "Very long question text ".repeat(20), listOf("a".repeat(50), "b".repeat(40), "c".repeat(30), "d".repeat(20)), "Because")) }
    @Test fun difficulty_in_range_4() { assertInRange(questionOf(4, "", listOf("", "", "", ""), "")) }
    @Test fun difficulty_in_range_5() { assertInRange(questionOf(5, "Edge", listOf("one", "two", "three", "four"), "explain")) }

    @Test fun explanation_increases_difficulty_1() {
        val noExp = questionOf(10, "Question medium text", listOf("one", "two", "three", "four"), "")
        val withExp = noExp.copy(explanation = "some explanation")
        assertTrue(engine.estimateDifficulty(withExp) >= engine.estimateDifficulty(noExp))
    }
    @Test fun explanation_increases_difficulty_2() {
        val noExp = questionOf(11, "Q text", listOf("a", "b", "c", "d"), "")
        val withExp = noExp.copy(explanation = "x")
        assertTrue(engine.estimateDifficulty(withExp) >= engine.estimateDifficulty(noExp))
    }
    @Test fun explanation_increases_difficulty_3() {
        val noExp = questionOf(12, "Another question for tests", listOf("abc", "def", "ghi", "jkl"), "")
        val withExp = noExp.copy(explanation = "long answer")
        assertTrue(engine.estimateDifficulty(withExp) >= engine.estimateDifficulty(noExp))
    }
    @Test fun explanation_increases_difficulty_4() {
        val noExp = questionOf(13, "12345678901234567890", listOf("a", "b", "c", "d"), "")
        val withExp = noExp.copy(explanation = "details")
        assertTrue(engine.estimateDifficulty(withExp) >= engine.estimateDifficulty(noExp))
    }
    @Test fun explanation_increases_difficulty_5() {
        val noExp = questionOf(14, "tiny", listOf("tiny", "tiny", "tiny", "tiny"), "")
        val withExp = noExp.copy(explanation = "tiny")
        assertTrue(engine.estimateDifficulty(withExp) >= engine.estimateDifficulty(noExp))
    }

    @Test fun initial_order_keeps_count_1() { assertEquals(5, engine.initialOrder(sampleQuestions(), 0.2f).size) }
    @Test fun initial_order_keeps_count_2() { assertEquals(5, engine.initialOrder(sampleQuestions(), 0.5f).size) }
    @Test fun initial_order_keeps_count_3() { assertEquals(5, engine.initialOrder(sampleQuestions(), 0.9f).size) }
    @Test fun initial_order_keeps_count_4() { assertEquals(2, engine.initialOrder(sampleQuestions().take(2), 0.5f).size) }
    @Test fun initial_order_keeps_count_5() { assertEquals(1, engine.initialOrder(sampleQuestions().take(1), 0.5f).size) }

    @Test fun initial_order_for_low_ratio_prefers_easy_1() {
        val ordered = engine.initialOrder(sampleQuestions(), 0.1f)
        assertTrue(engine.estimateDifficulty(ordered.first()) <= engine.estimateDifficulty(ordered.last()))
    }
    @Test fun initial_order_for_low_ratio_prefers_easy_2() {
        val ordered = engine.initialOrder(sampleQuestions(), 0.2f)
        assertTrue(engine.estimateDifficulty(ordered[0]) <= engine.estimateDifficulty(ordered[1]))
    }
    @Test fun initial_order_for_high_ratio_prefers_hard_1() {
        val ordered = engine.initialOrder(sampleQuestions(), 0.95f)
        assertTrue(engine.estimateDifficulty(ordered.first()) >= engine.estimateDifficulty(ordered[1]))
    }
    @Test fun initial_order_for_high_ratio_prefers_hard_2() {
        val ordered = engine.initialOrder(sampleQuestions(), 0.8f)
        assertTrue(engine.estimateDifficulty(ordered.first()) >= engine.estimateDifficulty(ordered.last()) || ordered.size == 1)
    }
    @Test fun initial_order_stable_by_id_when_equal_distance() {
        val q1 = questionOf(1, "same", listOf("a", "b", "c", "d"), "")
        val q2 = questionOf(2, "same", listOf("a", "b", "c", "d"), "")
        val ordered = engine.initialOrder(listOf(q2, q1), 0.5f)
        assertEquals(1, ordered.first().id)
    }

    @Test fun reorder_remaining_keeps_prefix_1() {
        val base = sampleQuestions()
        val reordered = engine.reorderRemaining(base, 1, 0.9f)
        assertEquals(base.take(2), reordered.take(2))
    }
    @Test fun reorder_remaining_keeps_prefix_2() {
        val base = sampleQuestions()
        val reordered = engine.reorderRemaining(base, 0, 0.1f)
        assertEquals(base.take(1), reordered.take(1))
    }
    @Test fun reorder_remaining_keeps_prefix_3() {
        val base = sampleQuestions()
        val reordered = engine.reorderRemaining(base, 2, 0.5f)
        assertEquals(base.take(3), reordered.take(3))
    }
    @Test fun reorder_remaining_invalid_index_returns_same_1() {
        val base = sampleQuestions()
        assertEquals(base, engine.reorderRemaining(base, -1, 0.5f))
    }
    @Test fun reorder_remaining_invalid_index_returns_same_2() {
        val base = sampleQuestions()
        assertEquals(base, engine.reorderRemaining(base, 99, 0.5f))
    }
    @Test fun reorder_remaining_last_index_returns_same() {
        val base = sampleQuestions()
        assertEquals(base, engine.reorderRemaining(base, base.lastIndex, 0.5f))
    }

    private fun assertInRange(question: QuestionEntity) {
        val value = engine.estimateDifficulty(question)
        assertTrue(value in 0f..1f)
    }

    private fun sampleQuestions(): List<QuestionEntity> = listOf(
        questionOf(1, "2+2=?", listOf("1", "2", "3", "4"), ""),
        questionOf(2, "What is polymorphism in object-oriented programming and how does it differ from inheritance?", listOf("a", "b", "c", "d"), "Detailed explanation"),
        questionOf(3, "Capital of France?", listOf("Paris", "London", "Berlin", "Rome"), ""),
        questionOf(4, "Explain coroutine cancellation in Kotlin with structured concurrency principles.", listOf("ans1", "ans2", "ans3", "ans4"), "More details"),
        questionOf(5, "Kotlin keyword for immutable variable?", listOf("var", "val", "let", "const"), "")
    )

    private fun questionOf(
        id: Int,
        text: String,
        options: List<String>,
        explanation: String
    ): QuestionEntity {
        return QuestionEntity(
            id = id,
            quizId = 1,
            text = text,
            optionsJson = options.joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\""),
            correctIndex = 0,
            explanation = explanation
        )
    }
}
