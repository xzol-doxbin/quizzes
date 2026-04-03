package eu.tutorials.mywishlistapp.ui.model

fun parseOptions(optionsJson: String): List<String> {
    return optionsJson
        .removePrefix("[")
        .removeSuffix("]")
        .split(",")
        .map { it.trim().removePrefix("\"").removeSuffix("\"") }
        .filter { it.isNotBlank() }
}