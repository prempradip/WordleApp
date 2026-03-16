package com.wordle.app.ui.game

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.wordle.app.core.*
import com.wordle.app.data.*
import com.wordle.app.data.local.StatsDao
import com.wordle.app.data.local.StatsEntity
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var wordRepository: WordRepository
    private lateinit var statsDao: StatsDao
    private lateinit var prefsRepository: PreferencesRepository
    private lateinit var profileRepository: ProfileRepository
    private lateinit var dailyWordRepository: DailyWordRepository
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var definitionRepository: DefinitionRepository
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var viewModel: GameViewModel

    private val defaultConfig = GameConfig(
        language = Language.ENGLISH,
        difficulty = Difficulty.NORMAL,
        mode = GameMode.PRACTICE
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        wordRepository = mockk()
        statsDao = mockk(relaxed = true)
        prefsRepository = mockk()
        profileRepository = mockk()
        dailyWordRepository = mockk()
        achievementRepository = mockk()
        definitionRepository = mockk()
        challengeRepository = mockk()

        // Default stubs
        every { prefsRepository.gameConfig } returns flowOf(defaultConfig)
        every { prefsRepository.darkTheme } returns flowOf(false)
        every { prefsRepository.highContrast } returns flowOf(false)
        every { profileRepository.appTheme } returns flowOf(AppTheme.CLASSIC)
        every { profileRepository.username } returns flowOf("Wordler")
        every { profileRepository.avatarIdx } returns flowOf(0)
        every { profileRepository.fontSize } returns flowOf(1)
        every { profileRepository.tileSize } returns flowOf(1)
        every { profileRepository.soundOn } returns flowOf(true)
        every { profileRepository.hapticLevel } returns flowOf(1)
        every { achievementRepository.unlockedIds } returns flowOf(emptyList())
        every { wordRepository.getRandomWord(any()) } returns "PLANT"
        every { wordRepository.isValidWord(any(), any()) } returns true
        coEvery { dailyWordRepository.clearDailyIfNewDay() } just Runs
        every { dailyWordRepository.msUntilNextWord() } returns 3_600_000L
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): GameViewModel {
        return GameViewModel(
            wordRepository, statsDao, prefsRepository, profileRepository,
            dailyWordRepository, achievementRepository, definitionRepository, challengeRepository
        )
    }

    @Test
    fun `initial state has IN_PROGRESS status`() = runTest {
        viewModel = createViewModel()
        assertThat(viewModel.state.value.status).isEqualTo(GameStatus.IN_PROGRESS)
    }

    @Test
    fun `initial state target word is PLANT`() = runTest {
        viewModel = createViewModel()
        assertThat(viewModel.state.value.targetWord).isEqualTo("PLANT")
    }

    @Test
    fun `onKey appends letter to currentInput`() = runTest {
        viewModel = createViewModel()
        viewModel.onKey('P')
        viewModel.onKey('L')
        assertThat(viewModel.state.value.currentInput).isEqualTo("PL")
    }

    @Test
    fun `onKey does not exceed word length`() = runTest {
        viewModel = createViewModel()
        repeat(10) { viewModel.onKey('A') }
        assertThat(viewModel.state.value.currentInput.length).isEqualTo(5)
    }

    @Test
    fun `onDelete removes last character`() = runTest {
        viewModel = createViewModel()
        viewModel.onKey('P')
        viewModel.onKey('L')
        viewModel.onDelete()
        assertThat(viewModel.state.value.currentInput).isEqualTo("P")
    }

    @Test
    fun `onDelete on empty input does nothing`() = runTest {
        viewModel = createViewModel()
        viewModel.onDelete()
        assertThat(viewModel.state.value.currentInput).isEmpty()
    }

    @Test
    fun `onSubmit with incomplete word triggers shake`() = runTest {
        viewModel = createViewModel()
        viewModel.onKey('P')
        viewModel.onKey('L')
        viewModel.onSubmit()
        assertThat(viewModel.state.value.shakeRow).isEqualTo(0)
        assertThat(viewModel.state.value.errorMessage).isNotNull()
    }

    @Test
    fun `onSubmit with invalid word triggers shake`() = runTest {
        every { wordRepository.isValidWord("ZZZZZ", any()) } returns false
        viewModel = createViewModel()
        "ZZZZZ".forEach { viewModel.onKey(it) }
        viewModel.onSubmit()
        assertThat(viewModel.state.value.shakeRow).isEqualTo(0)
    }

    @Test
    fun `correct guess sets status to WON`() = runTest {
        coEvery { statsDao.getStats(any(), any()) } returns flowOf(emptyList())
        coEvery { achievementRepository.tryUnlock(any()) } returns null
        coEvery { profileRepository.definitionViews } returns flowOf(0)
        coEvery { definitionRepository.getDefinition(any()) } returns Result.failure(Exception())

        viewModel = createViewModel()
        "PLANT".forEach { viewModel.onKey(it) }
        viewModel.onSubmit()

        assertThat(viewModel.state.value.status).isEqualTo(GameStatus.WON)
    }

    @Test
    fun `wrong guess advances currentRow`() = runTest {
        every { wordRepository.isValidWord("CRANE", any()) } returns true
        viewModel = createViewModel()
        "CRANE".forEach { viewModel.onKey(it) }
        viewModel.onSubmit()
        assertThat(viewModel.state.value.currentRow).isEqualTo(1)
    }

    @Test
    fun `losing all attempts sets status to LOST`() = runTest {
        every { wordRepository.isValidWord(any(), any()) } returns true
        coEvery { statsDao.getStats(any(), any()) } returns flowOf(emptyList())
        coEvery { achievementRepository.tryUnlock(any()) } returns null
        coEvery { profileRepository.definitionViews } returns flowOf(0)
        coEvery { definitionRepository.getDefinition(any()) } returns Result.failure(Exception())

        viewModel = createViewModel()
        // Submit 6 wrong guesses (NORMAL = 6 attempts)
        val wrongWords = listOf("CRANE", "BRAVE", "DELTA", "EAGLE", "FLAME", "GRACE")
        wrongWords.forEach { word ->
            word.forEach { viewModel.onKey(it) }
            viewModel.onSubmit()
        }
        assertThat(viewModel.state.value.status).isEqualTo(GameStatus.LOST)
    }

    @Test
    fun `useHint increments hintsUsed`() = runTest {
        viewModel = createViewModel()
        viewModel.useHint()
        assertThat(viewModel.state.value.hintsUsed).isEqualTo(1)
    }

    @Test
    fun `useHint adds position to hintRevealedPositions`() = runTest {
        viewModel = createViewModel()
        viewModel.useHint()
        assertThat(viewModel.state.value.hintRevealedPositions).isNotEmpty()
    }

    @Test
    fun `useHint does nothing when hints exhausted`() = runTest {
        viewModel = createViewModel()
        // NORMAL allows 1 hint
        viewModel.useHint()
        viewModel.useHint() // second call should be ignored
        assertThat(viewModel.state.value.hintsUsed).isEqualTo(1)
    }

    @Test
    fun `startChallenge sets isChallengeMode true`() = runTest {
        viewModel = createViewModel()
        viewModel.startChallenge("CRANE")
        assertThat(viewModel.state.value.isChallengeMode).isTrue()
        assertThat(viewModel.state.value.targetWord).isEqualTo("CRANE")
    }

    @Test
    fun `dismissError clears errorMessage`() = runTest {
        viewModel = createViewModel()
        viewModel.onKey('P')
        viewModel.onSubmit() // triggers error
        viewModel.dismissError()
        assertThat(viewModel.state.value.errorMessage).isNull()
    }

    @Test
    fun `newAchievement emits when achievement unlocked on win`() = runTest {
        coEvery { statsDao.getStats(any(), any()) } returns flowOf(emptyList())
        coEvery { achievementRepository.tryUnlock("first_win") } returns
            Achievement("first_win", "First Blood", "Win your first game", "🏆")
        coEvery { achievementRepository.tryUnlock(any()) } returns null
        coEvery { profileRepository.definitionViews } returns flowOf(0)
        coEvery { definitionRepository.getDefinition(any()) } returns Result.failure(Exception())

        viewModel = createViewModel()

        viewModel.newAchievement.test {
            "PLANT".forEach { viewModel.onKey(it) }
            viewModel.onSubmit()
            val achievement = awaitItem()
            assertThat(achievement.id).isEqualTo("first_win")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hard mode validation blocks invalid guess`() = runTest {
        val hardConfig = defaultConfig.copy(difficulty = Difficulty.HARD)
        every { prefsRepository.gameConfig } returns flowOf(hardConfig)
        every { wordRepository.isValidWord(any(), any()) } returns true

        viewModel = createViewModel()

        // Submit first guess to establish a CORRECT letter
        "PLANT".forEach { viewModel.onKey(it) } // This wins immediately, so use a wrong word
        // Reset and use a word that establishes constraints
        every { wordRepository.getRandomWord(any()) } returns "CRANE"
        viewModel.startNewGame(hardConfig)

        // Submit BRAVE — establishes R as PRESENT at index 1 (target CRANE has R at index 2)
        every { wordRepository.isValidWord("BRAVE", any()) } returns true
        "BRAVE".forEach { viewModel.onKey(it) }
        viewModel.onSubmit()

        // Now try a guess without R — should fail hard mode
        every { wordRepository.isValidWord("DELTA", any()) } returns true
        "DELTA".forEach { viewModel.onKey(it) }
        viewModel.onSubmit()

        // Should have shaken (hard mode violation)
        assertThat(viewModel.state.value.errorMessage).isNotNull()
    }

    @Test
    fun `onKey is ignored when game is WON`() = runTest {
        coEvery { statsDao.getStats(any(), any()) } returns flowOf(emptyList())
        coEvery { achievementRepository.tryUnlock(any()) } returns null
        coEvery { profileRepository.definitionViews } returns flowOf(0)
        coEvery { definitionRepository.getDefinition(any()) } returns Result.failure(Exception())

        viewModel = createViewModel()
        "PLANT".forEach { viewModel.onKey(it) }
        viewModel.onSubmit() // game is now WON

        viewModel.onKey('X')
        assertThat(viewModel.state.value.currentInput).isEmpty()
    }

    @Test
    fun `onSubmit is ignored when game is already over`() = runTest {
        coEvery { statsDao.getStats(any(), any()) } returns flowOf(emptyList())
        coEvery { achievementRepository.tryUnlock(any()) } returns null
        coEvery { profileRepository.definitionViews } returns flowOf(0)
        coEvery { definitionRepository.getDefinition(any()) } returns Result.failure(Exception())

        viewModel = createViewModel()
        "PLANT".forEach { viewModel.onKey(it) }
        viewModel.onSubmit() // WON

        val rowAfterWin = viewModel.state.value.currentRow
        viewModel.onSubmit() // should be ignored
        assertThat(viewModel.state.value.currentRow).isEqualTo(rowAfterWin)
    }

    @Test
    fun `board is updated after a valid guess submission`() = runTest {
        viewModel = createViewModel()
        "CRANE".forEach { viewModel.onKey(it) }
        viewModel.onSubmit()

        val row0 = viewModel.state.value.board[0]
        assertThat(row0.map { it.letter }).containsExactly('C', 'R', 'A', 'N', 'E').inOrder()
    }

    @Test
    fun `board row letters have correct LetterState after evaluation`() = runTest {
        viewModel = createViewModel() // target = PLANT
        "PLACE".forEach { viewModel.onKey(it) }
        viewModel.onSubmit()

        val row0 = viewModel.state.value.board[0]
        assertThat(row0[0].state).isEqualTo(LetterState.CORRECT)  // P
        assertThat(row0[1].state).isEqualTo(LetterState.CORRECT)  // L
        assertThat(row0[2].state).isEqualTo(LetterState.CORRECT)  // A
        assertThat(row0[3].state).isEqualTo(LetterState.ABSENT)   // C
        assertThat(row0[4].state).isEqualTo(LetterState.ABSENT)   // E
    }

    @Test
    fun `buildChallengeLink delegates to challengeRepository`() = runTest {
        every { challengeRepository.buildChallengeLink(any()) } returns "https://wordle.app/challenge?w=ABCD"
        viewModel = createViewModel()
        val link = viewModel.buildChallengeLink()
        assertThat(link).isEqualTo("https://wordle.app/challenge?w=ABCD")
    }

    @Test
    fun `clearDefinition resets definition state to Idle`() = runTest {
        viewModel = createViewModel()
        viewModel.clearDefinition()
        assertThat(viewModel.definition.value).isEqualTo(DefinitionUiState.Idle)
    }

    @Test
    fun `startNewGame resets board and status`() = runTest {
        coEvery { statsDao.getStats(any(), any()) } returns flowOf(emptyList())
        coEvery { achievementRepository.tryUnlock(any()) } returns null
        coEvery { profileRepository.definitionViews } returns flowOf(0)
        coEvery { definitionRepository.getDefinition(any()) } returns Result.failure(Exception())

        viewModel = createViewModel()
        "PLANT".forEach { viewModel.onKey(it) }
        viewModel.onSubmit() // WON

        viewModel.startNewGame(defaultConfig)

        assertThat(viewModel.state.value.status).isEqualTo(GameStatus.IN_PROGRESS)
        assertThat(viewModel.state.value.currentRow).isEqualTo(0)
        assertThat(viewModel.state.value.currentInput).isEmpty()
    }

    @Test
    fun `countdown is non-empty after init`() = runTest {
        viewModel = createViewModel()
        assertThat(viewModel.countdown.value).isNotEmpty()
    }
