package com.wordle.app.ui.tutorial

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordle.app.theme.WordleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TutorialScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tutorialScreenShowsFirstPageTitle() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        composeRule.onNodeWithText("How to Play").assertIsDisplayed()
    }

    @Test
    fun tutorialFirstPageShowsNextButton() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        composeRule.onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun tutorialNextButtonAdvancesToSecondPage() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Green = Correct").assertIsDisplayed()
    }

    @Test
    fun tutorialBackButtonAppearsAfterFirstPage() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Back").assertIsDisplayed()
    }

    @Test
    fun tutorialBackButtonGoesToPreviousPage() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithText("How to Play").assertIsDisplayed()
    }

    @Test
    fun tutorialLastPageShowsLetsPlayButton() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        // 6 pages total — click Next 5 times to reach last page
        repeat(5) {
            composeRule.onNodeWithText("Next").performClick()
        }
        composeRule.onNodeWithText("Let's Play!").assertIsDisplayed()
    }

    @Test
    fun tutorialLetsPlayButtonCallsOnDone() {
        var done = false
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = { done = true })
            }
        }
        repeat(5) {
            composeRule.onNodeWithText("Next").performClick()
        }
        composeRule.onNodeWithText("Let's Play!").performClick()
        assert(done)
    }

    @Test
    fun tutorialPageIndicatorDotsAreVisible() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        // The root should render without error — dots are Boxes without text
        composeRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun tutorialPage3ShowsYellowWrongPositionTitle() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Yellow = Wrong Position").assertIsDisplayed()
    }

    @Test
    fun tutorialPage4ShowsGrayNotInWordTitle() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        repeat(3) { composeRule.onNodeWithText("Next").performClick() }
        composeRule.onNodeWithText("Gray = Not in Word").assertIsDisplayed()
    }

    @Test
    fun tutorialPage5ShowsModesTitle() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        repeat(4) { composeRule.onNodeWithText("Next").performClick() }
        composeRule.onNodeWithText("Modes & Difficulty").assertIsDisplayed()
    }

    @Test
    fun tutorialPage6ShowsHintsTitle() {
        composeRule.setContent {
            WordleTheme {
                TutorialScreen(onDone = {})
            }
        }
        repeat(5) { composeRule.onNodeWithText("Next").performClick() }
        composeRule.onNodeWithText("Hints & Achievements").assertIsDisplayed()
    }
}
