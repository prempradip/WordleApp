package com.wordle.app.core

data class TileState(
    val letter: Char = ' ',
    val state: LetterState = LetterState.EMPTY
)

data class GameState(
    val config: GameConfig = GameConfig(),
    val targetWord: String = "",
    val board: List<List<TileState>> = emptyList(),
    val currentRow: Int = 0,
    val currentInput: String = "",
    val keyboardStates: Map<Char, LetterState> = emptyMap(),
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val hintsUsed: Int = 0,
    val hintRevealedPositions: Set<Int> = emptySet(),
    val errorMessage: String? = null,
    val shakeRow: Int = -1,
    val revealRow: Int = -1,
    val isChallengeMode: Boolean = false
)

enum class GameStatus {
    IN_PROGRESS,
    WON,
    LOST
}
