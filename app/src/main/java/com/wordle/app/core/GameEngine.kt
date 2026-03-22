package com.wordle.app.core

object GameEngine {

    fun initBoard(config: GameConfig): List<List<TileState>> {
        val wordLen = config.wordLength.value
        return List(config.difficulty.maxAttempts) {
            List(wordLen) { TileState() }
        }
    }

    fun evaluateGuess(guess: String, target: String): List<LetterState> {
        val result = MutableList(guess.length) { LetterState.ABSENT }
        val targetChars = target.toMutableList()

        // First pass: mark CORRECT
        for (i in guess.indices) {
            if (guess[i] == target[i]) {
                result[i] = LetterState.CORRECT
                targetChars[i] = '#' // consume
            }
        }

        // Second pass: mark PRESENT
        for (i in guess.indices) {
            if (result[i] == LetterState.CORRECT) continue
            val idx = targetChars.indexOf(guess[i])
            if (idx != -1) {
                result[i] = LetterState.PRESENT
                targetChars[idx] = '#'
            }
        }

        return result
    }

    fun updateKeyboard(
        current: Map<Char, LetterState>,
        guess: String,
        states: List<LetterState>
    ): Map<Char, LetterState> {
        val updated = current.toMutableMap()
        for (i in guess.indices) {
            val ch = guess[i]
            val new = states[i]
            val existing = updated[ch]
            // Priority: CORRECT > PRESENT > ABSENT
            if (existing == null || new.priority() > existing.priority()) {
                updated[ch] = new
            }
        }
        return updated
    }

    fun getHint(state: GameState): Int? {
        if (state.hintsUsed >= state.config.difficulty.hintsAllowed) return null
        val unrevealed = state.targetWord.indices.filter { it !in state.hintRevealedPositions }
        return unrevealed.randomOrNull()
    }

    fun validateHardMode(
        guess: String,
        previousRows: List<List<TileState>>,
        target: String
    ): String? {
        // In hard mode, all revealed CORRECT letters must be reused in same position
        // All PRESENT letters must appear somewhere in the guess
        for (row in previousRows) {
            for (i in row.indices) {
                val tile = row[i]
                if (tile.state == LetterState.CORRECT && guess.getOrNull(i) != tile.letter) {
                    return "Position ${i + 1} must be '${tile.letter}'"
                }
                if (tile.state == LetterState.PRESENT && !guess.contains(tile.letter)) {
                    return "Guess must contain '${tile.letter}'"
                }
            }
        }
        return null
    }

    private fun LetterState.priority(): Int = when (this) {
        LetterState.CORRECT -> 3
        LetterState.PRESENT -> 2
        LetterState.ABSENT -> 1
        else -> 0
    }
}
