package com.wordle.app.ui.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wordle.app.data.local.StatsDao
import com.wordle.app.theme.TileCorrect
import com.wordle.app.theme.TilePresent
import com.wordle.app.ui.game.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel(),
    statsDao: StatsDao
) {
    val state    by viewModel.state.collectAsState()
    val lang     = state.config.language.code
    val diff     = state.config.difficulty.name
    val statsList by statsDao.getStats(lang, diff).collectAsState(initial = emptyList())

    val totalGames    = statsList.size
    val wins          = statsList.count { it.won }
    val winRate       = if (totalGames > 0) (wins * 100 / totalGames) else 0
    val currentStreak = statsList.sortedByDescending { it.timestamp }.takeWhile { it.won }.size
    val bestStreak    = run {
        var best = 0; var cur = 0
        statsList.sortedBy { it.timestamp }.forEach { if (it.won) { cur++; if (cur > best) best = cur } else cur = 0 }
        best
    }

    val distribution = (1..state.config.difficulty.maxAttempts).associateWith { attempt ->
        statsList.count { it.won && it.attemptsUsed == attempt }
    }
    val maxDist = distribution.values.maxOrNull()?.takeIf { it > 0 } ?: 1

    // Animate bars in
    var animateBars by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateBars = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("Played",  "$totalGames",    Modifier.weight(1f))
                StatCard("Win %",   "$winRate",        Modifier.weight(1f))
                StatCard("Streak",  "$currentStreak",  Modifier.weight(1f))
                StatCard("Best",    "$bestStreak",      Modifier.weight(1f))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Distribution
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Guess Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                distribution.forEach { (attempt, count) ->
                    val targetFraction = if (animateBars) (count.toFloat() / maxDist).coerceAtLeast(0.06f) else 0.06f
                    val fraction by animateFloatAsState(
                        targetValue = targetFraction,
                        animationSpec = tween(800, delayMillis = attempt * 80, easing = FastOutSlowInEasing),
                        label = "bar_$attempt"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "$attempt",
                            modifier = Modifier.width(18.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(TileCorrect, TilePresent))
                                    ),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (count > 0) {
                                    Text(
                                        "$count",
                                        modifier = Modifier.padding(end = 8.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
