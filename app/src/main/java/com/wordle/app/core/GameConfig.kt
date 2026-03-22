package com.wordle.app.core

enum class WordLength(val value: Int, val displayName: String) {
    FOUR(4, "4 letters"),
    FIVE(5, "5 letters"),
    SIX(6, "6 letters"),
    SEVEN(7, "7 letters")
}

enum class Difficulty(val label: String, val maxAttempts: Int, val hintsAllowed: Int) {
    EASY("Easy", 8, 3),
    NORMAL("Normal", 6, 1),
    HARD("Hard", 6, 0)
}

enum class Language(
    val code: String,
    val displayName: String,
    val supportedLengths: Set<WordLength>
) {
    ENGLISH("en", "English", setOf(WordLength.FOUR, WordLength.FIVE, WordLength.SIX, WordLength.SEVEN)),
    SPANISH("es", "Español", setOf(WordLength.FOUR, WordLength.FIVE, WordLength.SIX, WordLength.SEVEN)),
    FRENCH("fr", "Français", setOf(WordLength.FOUR, WordLength.FIVE, WordLength.SIX, WordLength.SEVEN)),
    GERMAN("de", "Deutsch", setOf(WordLength.FOUR, WordLength.FIVE, WordLength.SIX, WordLength.SEVEN))
}

enum class GameMode {
    DAILY,    // Same word for everyone each day, one attempt per day
    PRACTICE  // Unlimited random games, no streak impact
}

data class GameConfig(
    val language: Language = Language.ENGLISH,
    val wordLength: WordLength = WordLength.FIVE,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val mode: GameMode = GameMode.DAILY
)
