package com.wordle.app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordle.app.core.*
import com.wordle.app.data.*
import com.wordle.app.data.local.StatsDao
import com.wordle.app.data.local.StatsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val statsDao: StatsDao,
    private val prefsRepository: PreferencesRepository,
    private val profileRepository: ProfileRepository,
    private val dailyWordRepository: DailyWordRepository,
    private val achievementRepository: AchievementRepository,
    private val definitionRepository: DefinitionRepository,
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _newAchievement = MutableSharedFlow<Achievement>()
    val newAchievement: SharedFlow<Achievement> = _newAchievement.asSharedFlow()

    private val _definition = MutableStateFlow<DefinitionUiState>(DefinitionUiState.Idle)
    val definition: StateFlow<DefinitionUiState> = _definition.asStateFlow()

    val darkTheme    = prefsRepository.darkTheme.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val highContrast = prefsRepository.highContrast.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val appTheme     = profileRepository.appTheme.stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.CLASSIC)
    val username     = profileRepository.username.stateIn(viewModelScope, SharingStarted.Eagerly, "Wordler")
    val avatarIdx    = profileRepository.avatarIdx.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val fontSize     = profileRepository.fontSize.stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    val tileSize     = profileRepository.tileSize.stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    val soundOn      = profileRepository.soundOn.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val hapticLevel  = profileRepository.hapticLevel.stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    val unlockedAchievements = achievementRepository.unlockedIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Countdown to next daily word
    private val _countdown = MutableStateFlow("")
    val countdown: StateFlow<String> = _countdown.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepository.gameConfig.collect { config ->
                startNewGame(config)
            }
        }
        startCountdown()
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (true) {
                val ms = dailyWordRepository.msUntilNextWord()
                val h = ms / 3_600_000
                val m = (ms % 3_600_000) / 60_000
                val s = (ms % 60_000) / 1_000
                _countdown.value = "%02d:%02d:%02d".format(h, m, s)
                delay(1_000)
            }
        }
    }

    fun startNewGame(config: GameConfig = _state.value.config) {
        viewModelScope.launch {
            dailyWordRepository.clearDailyIfNewDay()
            val target = when (config.mode) {
                GameMode.DAILY    -> dailyWordRepository.getDailyWord(config.language)
                GameMode.PRACTICE -> wordRepository.getRandomWord(config.language)
            }
            _state.value = GameState(
                config = config,
                targetWord = target,
                board = GameEngine.initBoard(config),
                keyboardStates = emptyMap(),
                status = GameStatus.IN_PROGRESS
            )
            _definition.value = DefinitionUiState.Idle
        }
    }

    fun startChallenge(word: String, config: GameConfig = _state.value.config) {
        _state.value = GameState(
            config = config.copy(mode = GameMode.PRACTICE),
            targetWord = word.uppercase(),
            board = GameEngine.initBoard(config),
            keyboardStates = emptyMap(),
            status = GameStatus.IN_PROGRESS,
            isChallengeMode = true
        )
        _definition.value = DefinitionUiState.Idle
    }

    fun onKey(char: Char) {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS) return
        if (s.currentInput.length >= s.config.language.wordLength) return
        _state.value = s.copy(currentInput = s.currentInput + char.uppercaseChar(), errorMessage = null, shakeRow = -1)
        updateBoardInput()
    }

    fun onDelete() {
        val s = _state.value
        if (s.currentInput.isEmpty()) return
        _state.value = s.copy(currentInput = s.currentInput.dropLast(1), errorMessage = null, shakeRow = -1)
        updateBoardInput()
    }

    fun onSubmit() {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS) return
        val wordLen = s.config.language.wordLength
        if (s.currentInput.length < wordLen) { triggerShake(s.currentRow, "Not enough letters"); return }
        if (s.config.difficulty == Difficulty.HARD) {
            val error = GameEngine.validateHardMode(s.currentInput, s.board.take(s.currentRow), s.targetWord)
            if (error != null) { triggerShake(s.currentRow, error); return }
        }
        if (!wordRepository.isValidWord(s.currentInput, s.config.language)) {
            triggerShake(s.currentRow, "Not in word list"); return
        }
        val letterStates = GameEngine.evaluateGuess(s.currentInput, s.targetWord)
        val newBoard = s.board.toMutableList()
        newBoard[s.currentRow] = s.currentInput.mapIndexed { i, c -> TileState(c, letterStates[i]) }
        val newKeyboard = GameEngine.updateKeyboard(s.keyboardStates, s.currentInput, letterStates)
        val won = letterStates.all { it == LetterState.CORRECT }
        val nextRow = s.currentRow + 1
        val lost = !won && nextRow >= s.config.difficulty.maxAttempts
        val newStatus = when { won -> GameStatus.WON; lost -> GameStatus.LOST; else -> GameStatus.IN_PROGRESS }
        _state.value = s.copy(
            board = newBoard, currentRow = nextRow, currentInput = "",
            keyboardStates = newKeyboard, status = newStatus,
            errorMessage = null, shakeRow = -1, revealRow = s.currentRow
        )
        viewModelScope.launch {
            delay((wordLen * 300L) + 500L)
            _state.value = _state.value.copy(revealRow = -1)
            if (newStatus != GameStatus.IN_PROGRESS) {
                saveStats(newStatus == GameStatus.WON, nextRow)
                checkAchievements(newStatus == GameStatus.WON, nextRow)
                if (newStatus == GameStatus.WON || newStatus == GameStatus.LOST) {
                    fetchDefinition(_state.value.targetWord)
                }
            }
        }
    }

    fun useHint() {
        val s = _state.value
        if (s.status != GameStatus.IN_PROGRESS) return
        val hintPos = GameEngine.getHint(s) ?: return
        _state.value = s.copy(hintsUsed = s.hintsUsed + 1, hintRevealedPositions = s.hintRevealedPositions + hintPos)
    }

    fun fetchDefinition(word: String) {
        _definition.value = DefinitionUiState.Loading
        viewModelScope.launch {
            val result = definitionRepository.getDefinition(word)
            _definition.value = result.fold(
                onSuccess = { DefinitionUiState.Success(it) },
                onFailure = { DefinitionUiState.Error("Definition not available") }
            )
            profileRepository.incrementDefinitionViews()
            checkAchievements(false, 0) // re-check for word nerd achievement
        }
    }

    fun buildChallengeLink(): String = challengeRepository.buildChallengeLink(_state.value.targetWord)
    fun buildChallengeShareText(): String = challengeRepository.buildChallengeShareText(_state.value.targetWord)

    fun updateConfig(config: GameConfig) { viewModelScope.launch { prefsRepository.saveConfig(config) } }
    fun setDarkTheme(enabled: Boolean)   { viewModelScope.launch { prefsRepository.setDarkTheme(enabled) } }
    fun setHighContrast(enabled: Boolean){ viewModelScope.launch { prefsRepository.setHighContrast(enabled) } }
    fun setAppTheme(theme: AppTheme)     { viewModelScope.launch { profileRepository.setAppTheme(theme) } }
    fun setUsername(name: String)        { viewModelScope.launch { profileRepository.setUsername(name) } }
    fun setAvatarIdx(idx: Int)           { viewModelScope.launch { profileRepository.setAvatarIdx(idx) } }
    fun setFontSize(size: Int)           { viewModelScope.launch { profileRepository.setFontSize(size) } }
    fun setTileSize(size: Int)           { viewModelScope.launch { profileRepository.setTileSize(size) } }
    fun setSoundOn(on: Boolean)          { viewModelScope.launch { profileRepository.setSoundOn(on) } }
    fun setHapticLevel(level: Int)       { viewModelScope.launch { profileRepository.setHapticLevel(level) } }
    fun dismissError()                   { _state.value = _state.value.copy(errorMessage = null, shakeRow = -1) }
    fun clearDefinition()                { _definition.value = DefinitionUiState.Idle }

    private fun triggerShake(row: Int, message: String) {
        _state.value = _state.value.copy(errorMessage = message, shakeRow = row)
        viewModelScope.launch { delay(600); _state.value = _state.value.copy(shakeRow = -1) }
    }

    private fun updateBoardInput() {
        val s = _state.value
        val wordLen = s.config.language.wordLength
        val updatedRow = List(wordLen) { i ->
            val ch = s.currentInput.getOrNull(i) ?: ' '
            TileState(ch, if (ch != ' ') LetterState.TYPED else LetterState.EMPTY)
        }
        val newBoard = s.board.toMutableList()
        if (s.currentRow < newBoard.size) newBoard[s.currentRow] = updatedRow
        _state.value = s.copy(board = newBoard)
    }

    private fun saveStats(won: Boolean, attempts: Int) {
        viewModelScope.launch {
            val s = _state.value
            if (s.config.mode == GameMode.PRACTICE && s.isChallengeMode) return@launch // don't count challenge games
            statsDao.insert(StatsEntity(
                languageCode = s.config.language.code,
                difficulty   = s.config.difficulty.name,
                won = won, attemptsUsed = attempts
            ))
        }
    }

    private fun checkAchievements(won: Boolean, attempts: Int) {
        viewModelScope.launch {
            val s = _state.value
            val statsList = statsDao.getStats(s.config.language.code, s.config.difficulty.name)
                .first()

            suspend fun tryUnlock(id: String) {
                achievementRepository.tryUnlock(id)?.let { _newAchievement.emit(it) }
            }

            if (won) {
                tryUnlock("first_win")
                if (attempts == 1) tryUnlock("genius")
                if (attempts == 2) tryUnlock("speed_demon")
                if (s.config.difficulty == Difficulty.HARD) tryUnlock("hard_mode_win")
                if (s.hintsUsed == 0) tryUnlock("no_hints")

                val streak = statsList.sortedByDescending { it.timestamp }.takeWhile { it.won }.size
                if (streak >= 3)  tryUnlock("streak_3")
                if (streak >= 7)  tryUnlock("streak_7")
                if (streak >= 30) tryUnlock("streak_30")
            }

            // Polyglot: won in all 4 languages
            val wonLanguages = Language.entries.count { lang ->
                statsDao.getWinCount(lang.code, s.config.difficulty.name) > 0
            }
            if (wonLanguages == Language.entries.size) tryUnlock("polyglot")

            // Daily devotee
            val dailyWins = statsDao.getStats(s.config.language.code, Difficulty.NORMAL.name)
                .first().count { it.won }
            if (dailyWins >= 7)  tryUnlock("daily_7")
            if (dailyWins >= 30) tryUnlock("daily_30")

            // Practice 10
            val practiceGames = statsDao.getTotalGames(s.config.language.code, s.config.difficulty.name)
            if (practiceGames >= 10) tryUnlock("practice_10")

            // Word nerd: 10 definition views
            val defViews = profileRepository.definitionViews.first()
            if (defViews >= 10) tryUnlock("definition_fan")
        }
    }
}

sealed class DefinitionUiState {
    object Idle    : DefinitionUiState()
    object Loading : DefinitionUiState()
    data class Success(val definition: WordDefinition) : DefinitionUiState()
    data class Error(val message: String) : DefinitionUiState()
}
