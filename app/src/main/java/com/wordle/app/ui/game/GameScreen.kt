package com.wordle.app.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wordle.app.core.GameMode
import com.wordle.app.core.GameStatus
import com.wordle.app.core.WordLength
import com.wordle.app.data.Achievement
import com.wordle.app.theme.GoldGradientStart
import com.wordle.app.theme.TileCorrect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsState()
    val highContrast by viewModel.highContrast.collectAsState()
    val darkTheme    by viewModel.darkTheme.collectAsState()
    val countdown    by viewModel.countdown.collectAsState()
    val definition   by viewModel.definition.collectAsState()

    var showResult   by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var toastAchievement by remember { mutableStateOf<Achievement?>(null) }

    // Achievement toast
    LaunchedEffect(Unit) {
        viewModel.newAchievement.collectLatest { achievement ->
            toastAchievement = achievement
            delay(3000)
            toastAchievement = null
        }
    }

    // Show result sheet after flip animation
    LaunchedEffect(state.revealRow) {
        if (state.revealRow == -1 && state.status != GameStatus.IN_PROGRESS) {
            delay(200)
            showResult = true
            if (state.status == GameStatus.WON) {
                showConfetti = true
                delay(4000)
                showConfetti = false
            }
        }
    }

    // Auto-dismiss error
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) { delay(1800); viewModel.dismissError() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                WordleTopBar(
                    language = state.config.language.displayName,
                    difficulty = state.config.difficulty.label,
                    mode = state.config.mode,
                    hintsLeft = state.config.difficulty.hintsAllowed - state.hintsUsed,
                    gameInProgress = state.status == GameStatus.IN_PROGRESS,
                    onHint = viewModel::useHint,
                    onStats = onNavigateToStats,
                    onSettings = onNavigateToSettings,
                    onProfile = onNavigateToProfile
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Error toast
                AnimatedVisibility(
                    visible = state.errorMessage != null,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit  = slideOutVertically { -it } + fadeOut()
                ) {
                    state.errorMessage?.let { msg ->
                        Box(modifier = Modifier.padding(top = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.onBackground)
                            .padding(horizontal = 20.dp, vertical = 10.dp)) {
                            Text(msg, color = MaterialTheme.colorScheme.background,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                // Daily mode banner
                if (state.config.mode == GameMode.DAILY) {
                    DailyBanner(countdown = countdown, completed = state.status != GameStatus.IN_PROGRESS)
                }

                Spacer(Modifier.height(4.dp))

                val adaptiveTileSize = when (state.config.wordLength) {
                    WordLength.FOUR  -> 68
                    WordLength.FIVE  -> 58
                    WordLength.SIX   -> 50
                    WordLength.SEVEN -> 44
                }
                BoardView(
                    board = state.board,
                    currentRow = state.currentRow,
                    highContrast = highContrast,
                    hintRevealedPositions = state.hintRevealedPositions,
                    shakeRow = state.shakeRow,
                    revealRow = state.revealRow,
                    tileSize = adaptiveTileSize,
                    modifier = Modifier.weight(1f).wrapContentHeight()
                )

                Spacer(Modifier.height(8.dp))

                KeyboardView(
                    keyStates = state.keyboardStates,
                    highContrast = highContrast,
                    isDark = darkTheme,
                    onKey = viewModel::onKey,
                    onDelete = viewModel::onDelete,
                    onSubmit = viewModel::onSubmit,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        }

        // Confetti
        ConfettiView(active = showConfetti)

        // Achievement toast overlay
        AnimatedVisibility(
            visible = toastAchievement != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit  = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
        ) {
            toastAchievement?.let { a ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(a.icon, fontSize = 28.sp)
                        Column {
                            Text("Achievement Unlocked!", fontSize = 11.sp,
                                color = GoldGradientStart, fontWeight = FontWeight.Bold)
                            Text(a.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(a.description, fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showResult) {
        ResultSheet(
            status = state.status,
            targetWord = state.targetWord,
            attemptsUsed = state.currentRow,
            maxAttempts = state.config.difficulty.maxAttempts,
            board = state.board,
            highContrast = highContrast,
            countdown = countdown,
            definitionState = definition,
            challengeShareText = viewModel.buildChallengeShareText(),
            onPlayAgain = { showResult = false; showConfetti = false; viewModel.startNewGame() },
            onDismiss = { showResult = false }
        )
    }
}

@Composable
private fun DailyBanner(countdown: String, completed: Boolean) {
    AnimatedVisibility(visible = completed, enter = fadeIn(), exit = fadeOut()) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = TileCorrect, modifier = Modifier.size(16.dp))
                    Text("Today's puzzle complete!", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text(countdown, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordleTopBar(
    language: String, difficulty: String, mode: GameMode,
    hintsLeft: Int, gameInProgress: Boolean,
    onHint: () -> Unit, onStats: () -> Unit,
    onSettings: () -> Unit, onProfile: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("WORDLE", style = MaterialTheme.typography.titleLarge,
                        letterSpacing = 6.sp, fontWeight = FontWeight.Black)
                    if (mode == GameMode.DAILY) {
                        Surface(shape = RoundedCornerShape(6.dp), color = TileCorrect.copy(alpha = 0.2f)) {
                            Text("DAILY", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp, fontWeight = FontWeight.Black,
                                color = TileCorrect, letterSpacing = 1.sp)
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            actions = {
                if (hintsLeft > 0 && gameInProgress) {
                    IconButton(onClick = onHint) {
                        BadgedBox(badge = {
                            Badge(containerColor = TileCorrect) {
                                Text("$hintsLeft", color = Color.White, fontSize = 10.sp)
                            }
                        }) {
                            Icon(Icons.Default.Lightbulb, "Hint",
                                tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
                IconButton(onClick = onProfile) {
                    Icon(Icons.Default.Person, "Profile", tint = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = onStats) {
                    Icon(Icons.Default.BarChart, "Stats", tint = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(
            Brush.horizontalGradient(listOf(Color.Transparent,
                MaterialTheme.colorScheme.onSurface.copy(0.15f), Color.Transparent))))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestionChip(onClick = {}, label = { Text(language, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Language, null, Modifier.size(14.dp)) },
                shape = RoundedCornerShape(20.dp))
            SuggestionChip(onClick = {}, label = { Text(difficulty, fontSize = 11.sp) },
                shape = RoundedCornerShape(20.dp))
        }
    }
}
