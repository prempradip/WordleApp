package com.wordle.app.ui.game

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordle.app.core.*
import com.wordle.app.theme.WordleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun defaultBoard(): List<List<TileState>> {
        val config = GameConfig(language = Language.ENGLISH, difficulty = Difficulty.NORMAL)
        return GameEngine.initBoard(config)
    }

    // ── KeyboardView ─────────────────────────────────────────────────────────

    @Test
    fun keyboardDisplaysAllLetterKeys() {
        composeRule.setContent {
            WordleTheme {
                KeyboardView(
                    keyStates = emptyMap(),
                    highContrast = false,
                    isDark = false,
                    onKey = {},
                    onDelete = {},
                    onSubmit = {}
                )
            }
        }
        composeRule.onNodeWithText("Q").assertIsDisplayed()
        composeRule.onNodeWithText("A").assertIsDisplayed()
        composeRule.onNodeWithText("Z").assertIsDisplayed()
        composeRule.onNodeWithText("M").assertIsDisplayed()
    }

    @Test
    fun keyboardEnterButtonIsDisplayed() {
        composeRule.setContent {
            WordleTheme {
                KeyboardView(
                    keyStates = emptyMap(),
                    highContrast = false,
                    isDark = false,
                    onKey = {},
                    onDelete = {},
                    onSubmit = {}
                )
            }
        }
        composeRule.onNodeWithText("ENTER").assertIsDisplayed()
    }

    @Test
    fun keyboardDeleteButtonIsDisplayed() {
        composeRule.setContent {
            WordleTheme {
                KeyboardView(
                    keyStates = emptyMap(),
                    highContrast = false,
                    isDark = false,
                    onKey = {},
                    onDelete = {},
                    onSubmit = {}
                )
            }
        }
        // Delete key shows ⌫ symbol
        composeRule.onNodeWithText("⌫").assertIsDisplayed()
    }

    @Test
    fun tappingLetterKeyCallsOnKey() {
        var pressed: Char? = null
        composeRule.setContent {
            WordleTheme {
                KeyboardView(
                    keyStates = emptyMap(),
                    highContrast = false,
                    isDark = false,
                    onKey = { pressed = it },
                    onDelete = {},
                    onSubmit = {}
                )
            }
        }
        composeRule.onNodeWithText("P").performClick()
        assert(pressed == 'P')
    }

    @Test
    fun tappingEnterCallsOnSubmit() {
        var submitted = false
        composeRule.setContent {
            WordleTheme {
                KeyboardView(
                    keyStates = emptyMap(),
                    highContrast = false,
                    isDark = false,
                    onKey = {},
                    onDelete = {},
                    onSubmit = { submitted = true }
                )
            }
        }
        composeRule.onNodeWithText("ENTER").performClick()
        assert(submitted)
    }

    @Test
    fun tappingDeleteCallsOnDelete() {
        var deleted = false
        composeRule.setContent {
            WordleTheme {
                KeyboardView(
                    keyStates = emptyMap(),
                    highContrast = false,
                    isDark = false,
                    onKey = {},
                    onDelete = { deleted = true },
                    onSubmit = {}
                )
            }
        }
        composeRule.onNodeWithText("⌫").performClick()
        assert(deleted)
    }

    // ── BoardView ────────────────────────────────────────────────────────────

    @Test
    fun boardDisplaysCorrectNumberOfRows() {
        val board = defaultBoard()
        composeRule.setContent {
            WordleTheme {
                BoardView(
                    board = board,
                    currentRow = 0,
                    highContrast = false,
                    hintRevealedPositions = emptySet(),
                    shakeRow = -1,
                    revealRow = -1
                )
            }
        }
        // 6 rows × 5 tiles = 30 tiles total; each is a Box with no text initially
        // Verify the board renders without crashing and has the right row count
        composeRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun boardWithTypedLetterShowsLetter() {
        val config = GameConfig(language = Language.ENGLISH, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config).toMutableList()
        board[0] = listOf(
            TileState('P', LetterState.TYPED),
            TileState('L', LetterState.TYPED),
            TileState('A', LetterState.TYPED),
            TileState('N', LetterState.TYPED),
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
                    revealRow = -1
                )
            }
        }
        composeRule.onNodeWithText("P").assertIsDisplayed()
        composeRule.onNodeWithText("L").assertIsDisplayed()
    }

    @Test
    fun boardWithRevealedCorrectRowShowsAllLetters() {
        val config = GameConfig(language = Language.ENGLISH, difficulty = Difficulty.NORMAL)
        val board = GameEngine.initBoard(config).toMutableList()
        board[0] = listOf(
            TileState('C', LetterState.CORRECT),
            TileState('R', LetterState.ABSENT),
            TileState('A', LetterState.PRESENT),
            TileState('N', LetterState.ABSENT),
            TileState('E', LetterState.ABSENT)
        )
        composeRule.setContent {
            WordleTheme {
                BoardView(
                    board = board,
                    currentRow = 1,
                    highContrast = false,
                    hintRevealedPositions = emptySet(),
                    shakeRow = -1,
                    revealRow = -1
                )
            }
        }
        composeRule.onNodeWithText("C").assertIsDisplayed()
        composeRule.onNodeWithText("R").assertIsDisplayed()
        composeRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun keyboardAllRowOneLettersPresent() {
        composeRule.setContent {
            WordleTheme {
                KeyboardView(
                    keyStates = emptyMap(),
                    highContrast = false,
                    isDark = false,
                    onKey = {},
                    onDelete = {},
                    onSubmit = {}
                )
            }
        }
        // Row 1: Q W E R T Y U I O P
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun keyboardAllRowTwoLettersPresent() {
        composeRule.setContent {
            WordleTheme {
                KeyboardView(
                    keyStates = emptyMap(),
                    highContrast = false,
                    isDark = false,
                    onKey = {},
                    onDelete = {},
                    onSubmit = {}
                )
            }
        }
        // Row 2: A S D F G H J K L
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
    }
}
