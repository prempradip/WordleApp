package com.wordle.app.core

enum class Difficulty(val label: String, val maxAttempts: Int, val hintsAllowed: Int) {
    EASY("Easy", 8, 3),
    NORMAL("Normal", 6, 1),
    HARD("Hard", 6, 0)
}

enum class Language(val code: String, val displayName: String, val wordLength: Int) {
    ENGLISH("en", "English", 5),
    SPANISH("es", "Español", 5),
    FRENCH("fr", "Français", 5),
    GERMAN("de", "Deutsch", 5)
}

enum class GameMode {
    DAILY,    // Same word for everyone each day, one attempt per day
    PRACTICE  // Unlimited random games, no streak impact
}

data class GameConfig(
    val language: Language = Language.ENGLISH,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val mode: GameMode = GameMode.DAILY
)
