package com.wordle.app.ui.game

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordle.app.core.*
import com.wordle.app.theme.WordleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end instrumented tests for word-length variant board rendering.
 * Verifies that BoardView correctly adapts to 4, 5, 6, and 7-letter configs.
 */
@RunWith(AndroidJUnit4::class)
class WordLengthBoardTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ── Board dimensions per word length ─────────────────────────────────────

    @Test
    fun boardRendersCorrectly_fourLetterConfig() {
        val config = GameConfig(wordLength = WordLength.FOUR, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config)

        composeRule.setContent {
            WordleTheme {
                BoardView(
                    board = board,
                    currentRow = 0,
                    highContrast = false,
                    hintRevealedPositions = emptySet(),
                    shakeRow = -1,
                    revealRow = -1,
                    tileSize = 68
                )
            }
        }
        composeRule.onRoot().assertIsDisplayed()
        // 6 rows × 4 tiles — board should render without crash
    }

    @Test
    fun boardRendersCorrectly_fiveLetterConfig() {
        val config = GameConfig(wordLength = WordLength.FIVE, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config)

        composeRule.setContent {
            WordleTheme {
                BoardView(
                    board = board,
                    currentRow = 0,
                    highContrast = false,
                    hintRevealedPositions = emptySet(),
                    shakeRow = -1,
                    revealRow = -1,
                    tileSize = 58
                )
            }
        }
        composeRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun boardRendersCorrectly_sixLetterConfig() {
        val config = GameConfig(wordLength = WordLength.SIX, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config)

        composeRule.setContent {
            WordleTheme {
                BoardView(
                    board = board,
                    currentRow = 0,
                    highContrast = false,
                    hintRevealedPositions = emptySet(),
                    shakeRow = -1,
                    revealRow = -1,
                    tileSize = 50
                )
            }
        }
        composeRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun boardRendersCorrectly_sevenLetterConfig() {
        val config = GameConfig(wordLength = WordLength.SEVEN, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config)

        composeRule.setContent {
            WordleTheme {
                BoardView(
                    board = board,
                    currentRow = 0,
                    highContrast = false,
                    hintRevealedPositions = emptySet(),
                    shakeRow = -1,
                    revealRow = -1,
                    tileSize = 44
                )
            }
        }
        composeRule.onRoot().assertIsDisplayed()
    }

    // ── Typed letters display correctly per word length ───────────────────────

    @Test
    fun fourLetterBoard_showsTypedLetters() {
        val config = GameConfig(wordLength = WordLength.FOUR, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config).toMutableList()
        board[0] = listOf(
            TileState('W', LetterState.TYPED),
            TileState('O', LetterState.TYPED),
            TileState('R', LetterState.TYPED),
            TileState('D', LetterState.TYPED)
        )

        composeRule.setContent {
            WordleTheme {
                BoardView(
                    board = board,
                    currentRow = 0,
                    highContrast = false,
                    hintRevealedPositions = emptySet(),
                    shakeRow = -1,
                    revealRow = -1,
                    tileSize = 68
                )
            }
        }
        composeRule.onNodeWithText("W").assertIsDisplayed()
        composeRule.onNodeWithText("O").assertIsDisplayed()
        composeRule.onNodeWithText("R").assertIsDisplayed()
        composeRule.onNodeWithText("D").assertIsDisplayed()
    }

    @Test
    fun sixLetterBoard_showsTypedLetters() {
        val config = GameConfig(wordLength = WordLength.SIX, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config).toMutableList()
        board[0] = listOf(
            TileState('P', LetterState.TYPED),
            TileState('L', LetterState.TYPED),
            TileState('A', LetterState.TYPED),
            TileState('N', LetterState.TYPED),
            TileState('E', LetterState.TYPED),
            TileState('T', LetterState.TYPED)
        )

        composeRule.setContent {
            WordleTheme {
                BoardView(
                    board = board,
                    currentRow = 0,
                    highContrast = false,
                    hintRevealedPositions = emptySet(),
                    shakeRow = -1,
                    revealRow = -1,
                    tileSize = 50
                )
            }
        }
        composeRule.onNodeWithText("P").assertIsDisplayed()
        composeRule.onNodeWithText("T").assertIsDisplayed()
    }

    @Test
    fun sevenLetterBoard_showsRevealedStates() {
        val config = GameConfig(wordLength = WordLength.SEVEN, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config).toMutableList()
        board[0] = listOf(
            TileState('F', LetterState.CORRECT),
            TileState('R', LetterState.ABSENT),
            TileState('E', LetterState.PRESENT),
            TileState('E', LetterState.ABSENT),
            TileState('D', LetterState.ABSENT),
            TileState('O', LetterState.ABSENT),
            TileState('M', LetterState.CORRECT)
        )

        composeRule.setContent {
            WordleTheme {
                BoardView(
                    board = board,
                    currentRow = 1,
                    highContrast = false,
                    hintRevealedPositions = emptySet(),
                    shakeRow = -1,
                    revealRow = -1,
                    tileSize = 44
                )
            }
        }
        composeRule.onNodeWithText("F").assertIsDisplayed()
        composeRule.onNodeWithText("M").assertIsDisplayed()
    }

    // ── GameEngine board dimensions match WordLength ──────────────────────────

    @Test
    fun initBoard_fourLetters_hasCorrectColumnCount() {
        val config = GameConfig(wordLength = WordLength.FOUR, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config)
        assert(board.all { row -> row.size == 4 }) {
            "Expected 4 tiles per row for FOUR word length"
        }
    }

    @Test
    fun initBoard_sevenLetters_hasCorrectColumnCount() {
        val config = GameConfig(wordLength = WordLength.SEVEN, difficulty = Difficulty.EASY)
        val board = GameEngine.initBoard(config)
        assert(board.all { row -> row.size == 7 }) {
            "Expected 7 tiles per row for SEVEN word length"
        }
        assert(board.size == 8) {
            "Expected 8 rows for EASY difficulty"
        }
    }
}
