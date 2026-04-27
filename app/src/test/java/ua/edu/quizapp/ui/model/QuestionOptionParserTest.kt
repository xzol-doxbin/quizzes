package ua.edu.quizapp.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionOptionParserTest {
    @Test fun parses_basic_json_1() { assertEquals(listOf("A", "B"), parseOptions("[\"A\",\"B\"]")) }
    @Test fun parses_basic_json_2() { assertEquals(listOf("1", "2", "3"), parseOptions("[\"1\",\"2\",\"3\"]")) }
    @Test fun parses_basic_json_3() { assertEquals(listOf("alpha"), parseOptions("[\"alpha\"]")) }
    @Test fun parses_basic_json_4() { assertEquals(listOf("A", "B", "C", "D"), parseOptions("[\"A\",\"B\",\"C\",\"D\"]")) }
    @Test fun parses_basic_json_5() { assertEquals(listOf("x", "y"), parseOptions("[\"x\", \"y\"]")) }

    @Test fun trims_spaces_1() { assertEquals(listOf("A", "B"), parseOptions("[ \"A\" , \"B\" ]")) }
    @Test fun trims_spaces_2() { assertEquals(listOf("left", "right"), parseOptions("[  \"left\"  ,  \"right\"  ]")) }
    @Test fun trims_spaces_3() { assertEquals(listOf("kotlin", "java"), parseOptions(" [\"kotlin\" ,\"java\"] ")) }
    @Test fun trims_spaces_4() { assertEquals(listOf("a", "b", "c"), parseOptions("[ \"a\", \"b\", \"c\" ]")) }
    @Test fun trims_spaces_5() { assertEquals(listOf("A"), parseOptions(" [ \"A\" ] ")) }

    @Test fun ignores_blank_1() { assertEquals(listOf("A"), parseOptions("[\"A\",\"\"]")) }
    @Test fun ignores_blank_2() { assertEquals(listOf("A", "B"), parseOptions("[\"A\",\" \",\"B\"]")) }
    @Test fun ignores_blank_3() { assertEquals(emptyList<String>(), parseOptions("[\"\"]")) }
    @Test fun ignores_blank_4() { assertEquals(emptyList<String>(), parseOptions("[]")) }
    @Test fun ignores_blank_5() { assertEquals(emptyList<String>(), parseOptions("")) }

    @Test fun supports_unicode_1() { assertEquals(listOf("Київ", "Харків"), parseOptions("[\"Київ\",\"Харків\"]")) }
    @Test fun supports_unicode_2() { assertEquals(listOf("Привіт"), parseOptions("[\"Привіт\"]")) }
    @Test fun supports_unicode_3() { assertEquals(listOf("ї", "є", "ґ"), parseOptions("[\"ї\",\"є\",\"ґ\"]")) }
    @Test fun supports_unicode_4() { assertEquals(listOf("A1", "Б2"), parseOptions("[\"A1\",\"Б2\"]")) }
    @Test fun supports_unicode_5() { assertEquals(listOf("テスト", "データ"), parseOptions("[\"テスト\",\"データ\"]")) }

    @Test fun robust_raw_input_1() { assertEquals(listOf("A"), parseOptions("A")) }
    @Test fun robust_raw_input_2() { assertEquals(listOf("A", "B"), parseOptions("A,B")) }
    @Test fun robust_raw_input_3() { assertEquals(listOf("A", "B"), parseOptions("\"A\",\"B\"")) }
    @Test fun robust_raw_input_4() { assertEquals(listOf("A"), parseOptions("\"A\"")) }
    @Test fun robust_raw_input_5() { assertEquals(listOf("A", "B", "C"), parseOptions("[A,B,C]")) }
    @Test fun robust_raw_input_6() { assertEquals(listOf("A", "B"), parseOptions("[\"A\",B]")) }
}
