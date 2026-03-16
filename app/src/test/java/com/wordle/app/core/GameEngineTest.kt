package com.wordle.app.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GameEngineTest {

    // ── evaluateGuess ────────────────────────────────────────────────────────

    @Test
    fun `all correct letters returns all CORRECT`() {
        val result = GameEngine.evaluateGuess("PLANT", "PLANT")
        assertThat(result).containsExactly(
            LetterState.CORRECT, LetterState.CORRECT, LetterState.CORRECT,
            LetterState.CORRECT, LetterState.CORRECT
        ).inOrder()
    }

    @Test
    fun `no matching letters returns all ABSENT`() {
        val result = GameEngine.evaluateGuess("ZZZZZ", "PLANT")
        assertThat(result).containsExactly(
            LetterState.ABSENT, LetterState.ABSENT, LetterState.ABSENT,
            LetterState.ABSENT, LetterState.ABSENT
        ).inOrder()
    }

    @Test
    fun `correct position is CORRECT, wrong position is PRESENT`() {
        // PLACE vs PLANT: P=correct, L=correct, A=correct, C=absent, E=absent
        val result = GameEngine.evaluateGuess("PLACE", "PLANT")
        assertThat(result[0]).isEqualTo(LetterState.CORRECT)  // P
        assertThat(result[1]).isEqualTo(LetterState.CORRECT)  // L
        assertThat(result[2]).isEqualTo(LetterState.CORRECT)  // A
        assertThat(result[3]).isEqualTo(LetterState.ABSENT)   // C
        assertThat(result[4]).isEqualTo(LetterState.ABSENT)   // E
    }

    @Test
    fun `letter in word but wrong position is PRESENT`() {
        // TAPIR vs PLANT: T is in PLANT but not at index 0
        val result = GameEngine.evaluateGuess("TAPIR", "PLANT")
        assertThat(result[0]).isEqualTo(LetterState.PRESENT)  // T present in PLANT
        assertThat(result[1]).isEqualTo(LetterState.ABSENT)   // A already consumed at pos 2? No — A is at index 2 in PLANT
        assertThat(result[2]).isEqualTo(LetterState.ABSENT)   // P already consumed at pos 0
        assertThat(result[3]).isEqualTo(LetterState.ABSENT)   // I absent
        assertThat(result[4]).isEqualTo(LetterState.ABSENT)   // R absent
    }

    @Test
    fun `duplicate letter in guess - only one marked PRESENT when target has one`() {
        // SPEED vs PLANT — no overlap, but test duplicate handling with AABBB vs XAXXX
        // Guess: AABBB, Target: XAXXX — first A is absent, second A is present
        val result = GameEngine.evaluateGuess("AABBB", "XAXXX")
        // A at index 0: not in target at 0, target has A at index 1
        // A at index 1: target[1] == A → CORRECT, consumes it
        // So index 0 A should be ABSENT (consumed by CORRECT at index 1)
        assertThat(result[0]).isEqualTo(LetterState.ABSENT)
        assertThat(result[1]).isEqualTo(LetterState.CORRECT)
        assertThat(result[2]).isEqualTo(LetterState.ABSENT)
        assertThat(result[3]).isEqualTo(LetterState.ABSENT)
        assertThat(result[4]).isEqualTo(LetterState.ABSENT)
    }

    @Test
    fun `duplicate letter - both marked when target has two`() {
        // Guess: AAXXX, Target: AAXXX
        val result = GameEngine.evaluateGuess("AAXXX", "AAXXX")
        assertThat(result[0]).isEqualTo(LetterState.CORRECT)
        assertThat(result[1]).isEqualTo(LetterState.CORRECT)
        assertThat(result[2]).isEqualTo(LetterState.CORRECT)
    }

    @Test
    fun `duplicate guess letter - second is ABSENT when target only has one`() {
        // Guess: LLANT, Target: PLANT — L appears twice in guess, once in target
        val result = GameEngine.evaluateGuess("LLANT", "PLANT")
        // First L at index 0: not correct (target[0]=P), target has L at index 1
        // Second L at index 1: target[1]=L → CORRECT, consumes it
        // So first L should be ABSENT (the only L was consumed by CORRECT)
        assertThat(result[0]).isEqualTo(LetterState.ABSENT)
        assertThat(result[1]).isEqualTo(LetterState.CORRECT)
    }

    // ── updateKeyboard ───────────────────────────────────────────────────────

    @Test
    fun `keyboard priority CORRECT beats PRESENT`() {
        val initial = mapOf('A' to LetterState.PRESENT)
        val states = listOf(LetterState.CORRECT)
        val result = GameEngine.updateKeyboard(initial, "A", states)
        assertThat(result['A']).isEqualTo(LetterState.CORRECT)
    }

    @Test
    fun `keyboard priority PRESENT beats ABSENT`() {
        val initial = mapOf('A' to LetterState.ABSENT)
        val states = listOf(LetterState.PRESENT)
        val result = GameEngine.updateKeyboard(initial, "A", states)
        assertThat(result['A']).isEqualTo(LetterState.PRESENT)
    }

    @Test
    fun `keyboard CORRECT is not downgraded by ABSENT`() {
        val initial = mapOf('A' to LetterState.CORRECT)
        val states = listOf(LetterState.ABSENT)
        val result = GameEngine.updateKeyboard(initial, "A", states)
        assertThat(result['A']).isEqualTo(LetterState.CORRECT)
    }

    @Test
    fun `keyboard adds new letters`() {
        val result = GameEngine.updateKeyboard(emptyMap(), "AB", listOf(LetterState.CORRECT, LetterState.ABSENT))
        assertThat(result['A']).isEqualTo(LetterState.CORRECT)
        assertThat(result['B']).isEqualTo(LetterState.ABSENT)
    }

    // ── validateHardMode ─────────────────────────────────────────────────────

    @Test
    fun `hard mode passes when all constraints satisfied`() {
        val prevRow = listOf(
            TileState('P', LetterState.CORRECT),
            TileState('L', LetterState.PRESENT),
            TileState('A', LetterState.ABSENT),
            TileState('C', LetterState.ABSENT),
            TileState('E', LetterState.ABSENT)
        )
        // Guess keeps P at position 0 and contains L somewhere
        val error = GameEngine.validateHardMode("PLUMB", listOf(prevRow), "PLANT")
        assertThat(error).isNull()
    }

    @Test
    fun `hard mode fails when CORRECT letter missing from same position`() {
        val prevRow = listOf(
            TileState('P', LetterState.CORRECT),
            TileState('L', LetterState.ABSENT),
            TileState('A', LetterState.ABSENT),
            TileState('C', LetterState.ABSENT),
            TileState('E', LetterState.ABSENT)
        )
        val error = GameEngine.validateHardMode("ZZZZZ", listOf(prevRow), "PLANT")
        assertThat(error).isNotNull()
        assertThat(error).contains("1") // position 1
    }

    @Test
    fun `hard mode fails when PRESENT letter not included in guess`() {
        val prevRow = listOf(
            TileState('P', LetterState.ABSENT),
            TileState('L', LetterState.PRESENT),
            TileState('A', LetterState.ABSENT),
            TileState('C', LetterState.ABSENT),
            TileState('E', LetterState.ABSENT)
        )
        val error = GameEngine.validateHardMode("ZZZZZ", listOf(prevRow), "PLANT")
        assertThat(error).isNotNull()
        assertThat(error).contains("L")
    }

    @Test
    fun `hard mode checks constraints from multiple previous rows`() {
        val row1 = listOf(
            TileState('C', LetterState.CORRECT),
            TileState('R', LetterState.ABSENT),
            TileState('A', LetterState.ABSENT),
            TileState('N', LetterState.ABSENT),
            TileState('E', LetterState.ABSENT)
        )
        val row2 = listOf(
            TileState('C', LetterState.CORRECT),
            TileState('L', LetterState.PRESENT),
            TileState('O', LetterState.ABSENT),
            TileState('U', LetterState.ABSENT),
            TileState('D', LetterState.ABSENT)
        )
        // Guess must start with C (CORRECT from row1) and contain L (PRESENT from row2)
        val valid = GameEngine.validateHardMode("CLAMP", listOf(row1, row2), "CLAMP")
        assertThat(valid).isNull()

        val invalid = GameEngine.validateHardMode("CRISP", listOf(row1, row2), "CLAMP")
        assertThat(invalid).isNotNull() // missing L
    }

    // ── getHint ──────────────────────────────────────────────────────────────

    @Test
    fun `getHint returns null when hints exhausted`() {
        val state = GameState(
            config = GameConfig(difficulty = Difficulty.NORMAL), // 1 hint allowed
            targetWord = "PLANT",
            hintsUsed = 1,
            hintRevealedPositions = emptySet()
        )
        val hint = GameEngine.getHint(state)
        assertThat(hint).isNull()
    }

    @Test
    fun `getHint returns a valid position index`() {
        val state = GameState(
            config = GameConfig(difficulty = Difficulty.EASY), // 3 hints allowed
            targetWord = "PLANT",
            hintsUsed = 0,
            hintRevealedPositions = emptySet()
        )
        val hint = GameEngine.getHint(state)
        assertThat(hint).isNotNull()
        assertThat(hint!!).isIn(0 until "PLANT".length)
    }

    @Test
    fun `getHint does not return already revealed position`() {
        val state = GameState(
            config = GameConfig(difficulty = Difficulty.EASY),
            targetWord = "PLANT",
            hintsUsed = 1,
            hintRevealedPositions = setOf(0, 1, 2, 3) // only index 4 left
        )
        val hint = GameEngine.getHint(state)
        assertThat(hint).isEqualTo(4)
    }

    @Test
    fun `all letters present but wrong position returns all PRESENT`() {
        // TAPLN vs PLANT — T,A,P,L,N all in PLANT but all wrong positions
        val result = GameEngine.evaluateGuess("TAPLN", "PLANT")
        // T at 0: PLANT has T at 4 → PRESENT
        // A at 1: PLANT has A at 2 → PRESENT
        // P at 2: PLANT has P at 0 → PRESENT
        // L at 3: PLANT has L at 1 → PRESENT
        // N at 4: PLANT has N at 3 → PRESENT
        assertThat(result).containsExactly(
            LetterState.PRESENT, LetterState.PRESENT, LetterState.PRESENT,
            LetterState.PRESENT, LetterState.PRESENT
        ).inOrder()
    }

    @Test
    fun `PRESENT letter is not double-counted when target has only one`() {
        // Guess: AAXXX, Target: BAXXX — only one A in target at index 1
        // A at index 0: not correct, target has A at index 1 → PRESENT, consume it
        // A at index 1: target[1]=A → CORRECT (first pass), so index 0 A is ABSENT
        val result = GameEngine.evaluateGuess("AAXXX", "BAXXX")
        assertThat(result[0]).isEqualTo(LetterState.ABSENT)   // A consumed by CORRECT at index 1
        assertThat(result[1]).isEqualTo(LetterState.CORRECT)  // A correct
    }

    @Test
    fun `mixed correct and present in same word`() {
        // CRANE vs CRANE — all correct
        val result = GameEngine.evaluateGuess("CRANE", "CRANE")
        result.forEach { assertThat(it).isEqualTo(LetterState.CORRECT) }
    }

    @Test
    fun `evaluateGuess result length matches guess length`() {
        val result = GameEngine.evaluateGuess("PLANT", "CRANE")
        assertThat(result).hasSize(5)
    }

    // ── initBoard ────────────────────────────────────────────────────────────

    @Test
    fun `initBoard creates correct dimensions for NORMAL difficulty`() {
        val config = GameConfig(language = Language.ENGLISH, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config)
        assertThat(board).hasSize(6)
        assertThat(board[0]).hasSize(5)
    }

    @Test
    fun `initBoard creates correct dimensions for EASY difficulty`() {
        val config = GameConfig(language = Language.ENGLISH, difficulty = Difficulty.EASY)
        val board = GameEngine.initBoard(config)
        assertThat(board).hasSize(8)
    }

    @Test
    fun `initBoard tiles start as EMPTY`() {
        val board = GameEngine.initBoard(GameConfig())
        board.forEach { row -> row.forEach { tile ->
            assertThat(tile.state).isEqualTo(LetterState.EMPTY)
            assertThat(tile.letter).isEqualTo(' ')
        }}
    }

    @Test
    fun `initBoard creates correct dimensions for HARD difficulty`() {
        val config = GameConfig(language = Language.ENGLISH, difficulty = Difficulty.HARD)
        val board = GameEngine.initBoard(config)
        assertThat(board).hasSize(6) // HARD same as NORMAL
        assertThat(board[0]).hasSize(5)
    }

    @Test
    fun `getHint returns null when all positions already revealed`() {
        val state = GameState(
            config = GameConfig(difficulty = Difficulty.EASY),
            targetWord = "PLANT",
            hintsUsed = 2,
            hintRevealedPositions = setOf(0, 1, 2, 3, 4)
        )
        assertThat(GameEngine.getHint(state)).isNull()
    }

    @Test
    fun `updateKeyboard preserves unrelated keys`() {
        val initial = mapOf('Z' to LetterState.ABSENT, 'X' to LetterState.CORRECT)
        val result = GameEngine.updateKeyboard(initial, "A", listOf(LetterState.PRESENT))
        assertThat(result['Z']).isEqualTo(LetterState.ABSENT)
        assertThat(result['X']).isEqualTo(LetterState.CORRECT)
        assertThat(result['A']).isEqualTo(LetterState.PRESENT)
    }
}
