package com.wordle.app.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GameStateTest {

    @Test
    fun `default GameState has IN_PROGRESS status`() {
        val state = GameState()
        assertThat(state.status).isEqualTo(GameStatus.IN_PROGRESS)
    }

    @Test
    fun `default GameState has empty board`() {
        val state = GameState()
        assertThat(state.board).isEmpty()
    }

    @Test
    fun `default GameConfig uses ENGLISH NORMAL DAILY`() {
        val config = GameConfig()
        assertThat(config.language).isEqualTo(Language.ENGLISH)
        assertThat(config.difficulty).isEqualTo(Difficulty.NORMAL)
        assertThat(config.mode).isEqualTo(GameMode.DAILY)
    }

    @Test
    fun `EASY difficulty has 8 attempts and 3 hints`() {
        assertThat(Difficulty.EASY.maxAttempts).isEqualTo(8)
        assertThat(Difficulty.EASY.hintsAllowed).isEqualTo(3)
    }

    @Test
    fun `HARD difficulty has 0 hints`() {
        assertThat(Difficulty.HARD.hintsAllowed).isEqualTo(0)
    }

    @Test
    fun `NORMAL difficulty has 6 attempts and 1 hint`() {
        assertThat(Difficulty.NORMAL.maxAttempts).isEqualTo(6)
        assertThat(Difficulty.NORMAL.hintsAllowed).isEqualTo(1)
    }

    @Test
    fun `all languages support all four word lengths`() {
        val expectedLengths = setOf(WordLength.FOUR, WordLength.FIVE, WordLength.SIX, WordLength.SEVEN)
        Language.entries.forEach { lang ->
            assertThat(lang.supportedLengths).isEqualTo(expectedLengths)
        }
    }

    @Test
    fun `default GameConfig uses FIVE word length`() {
        val config = GameConfig()
        assertThat(config.wordLength).isEqualTo(WordLength.FIVE)
    }

    @Test
    fun `TileState defaults to space and EMPTY`() {
        val tile = TileState()
        assertThat(tile.letter).isEqualTo(' ')
        assertThat(tile.state).isEqualTo(LetterState.EMPTY)
    }
}
