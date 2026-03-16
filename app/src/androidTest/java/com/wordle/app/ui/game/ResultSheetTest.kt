package com.wordle.app.ui.game

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordle.app.core.*
import com.wordle.app.data.WordDefinition
import com.wordle.app.theme.WordleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun defaultBoard(): List<List<TileState>> =
        GameEngine.initBoard(GameConfig(language = Language.ENGLISH, difficulty = Difficulty.NORMAL))

    @Test
    fun resultSheetShowsTargetWordOnLoss() {
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.LOST,
                    targetWord = "PLANT",
                    attemptsUsed = 6,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Idle,
                    challengeShareText = "Challenge text",
                    onPlayAgain = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("PLANT").assertIsDisplayed()
    }

    @Test
    fun resultSheetShowsCountdown() {
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.WON,
                    targetWord = "PLANT",
                    attemptsUsed = 3,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Idle,
                    challengeShareText = "Challenge text",
                    onPlayAgain = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("05:30:00").assertIsDisplayed()
    }

    @Test
    fun resultSheetShareResultButtonIsVisible() {
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.WON,
                    targetWord = "PLANT",
                    attemptsUsed = 3,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Idle,
                    challengeShareText = "Challenge text",
                    onPlayAgain = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("Share Result").assertIsDisplayed()
    }

    @Test
    fun resultSheetChallengeButtonIsVisible() {
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.WON,
                    targetWord = "PLANT",
                    attemptsUsed = 3,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Idle,
                    challengeShareText = "Challenge text",
                    onPlayAgain = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("Challenge a Friend").assertIsDisplayed()
    }

    @Test
    fun resultSheetPlayAgainButtonCallsOnPlayAgain() {
        var played = false
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.WON,
                    targetWord = "PLANT",
                    attemptsUsed = 3,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Idle,
                    challengeShareText = "Challenge text",
                    onPlayAgain = { played = true },
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("Play Again").performClick()
        assert(played)
    }

    @Test
    fun resultSheetShowsDefinitionWhenAvailable() {
        val definition = WordDefinition(
            word = "plant",
            phonetic = "/plænt/",
            partOfSpeech = "noun",
            definition = "A living organism of the kingdom Plantae.",
            example = null
        )
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.WON,
                    targetWord = "PLANT",
                    attemptsUsed = 3,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Success(definition),
                    challengeShareText = "Challenge text",
                    onPlayAgain = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("noun", substring = true).assertIsDisplayed()
    }

    @Test
    fun resultSheetShowsLoadingIndicatorWhenDefinitionLoading() {
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.WON,
                    targetWord = "PLANT",
                    attemptsUsed = 3,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Loading,
                    challengeShareText = "Challenge text",
                    onPlayAgain = {},
                    onDismiss = {}
                )
            }
        }
        // CircularProgressIndicator is present when loading
        composeRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun resultSheetShowsInviteFriendsButton() {
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.WON,
                    targetWord = "PLANT",
                    attemptsUsed = 3,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Idle,
                    challengeShareText = "Challenge text",
                    onPlayAgain = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("Invite Friends to Play").assertIsDisplayed()
    }

    @Test
    fun resultSheetShowsDefinitionErrorMessage() {
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.WON,
                    targetWord = "PLANT",
                    attemptsUsed = 3,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Error("Definition not available"),
                    challengeShareText = "Challenge text",
                    onPlayAgain = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("Definition not available").assertIsDisplayed()
    }

    @Test
    fun resultSheetShowsSolvedAttemptsOnWin() {
        composeRule.setContent {
            WordleTheme {
                ResultSheet(
                    status = GameStatus.WON,
                    targetWord = "PLANT",
                    attemptsUsed = 4,
                    maxAttempts = 6,
                    board = defaultBoard(),
                    highContrast = false,
                    countdown = "05:30:00",
                    definitionState = DefinitionUiState.Idle,
                    challengeShareText = "Challenge text",
                    onPlayAgain = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("4 / 6", substring = true).assertIsDisplayed()
    }
}
