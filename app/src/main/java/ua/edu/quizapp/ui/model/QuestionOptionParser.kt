package ua.edu.quizapp.ui.model

fun parseOptions(optionsJson: String): List<String> {
    val normalized = optionsJson.trim()

    return normalized
        .removePrefix("[")
        .removeSuffix("]")
        .split(",")
        .map { it.trim().removePrefix("\"").removeSuffix("\"") }
        .filter { it.isNotBlank() }
}
