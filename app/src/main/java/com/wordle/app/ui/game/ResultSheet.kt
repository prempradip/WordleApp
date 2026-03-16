package com.wordle.app.ui.game

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.app.core.GameStatus
import com.wordle.app.core.LetterState
import com.wordle.app.core.TileState
import com.wordle.app.theme.*
import kotlinx.coroutines.delay

private val WinMessages = listOf(
    "Genius!", "Magnificent!", "Impressive!", "Splendid!", "Great!", "Phew!"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSheet(
    status: GameStatus,
    targetWord: String,
    attemptsUsed: Int,
    maxAttempts: Int,
    board: List<List<TileState>>,
    highContrast: Boolean,
    countdown: String,
    definitionState: DefinitionUiState,
    challengeShareText: String,
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); visible = true }

    val winMessage = if (status == GameStatus.WON)
        WinMessages.getOrElse(attemptsUsed - 1) { "Nice!" } else "Better luck next time"

    val infiniteTransition = rememberInfiniteTransition(label = "trophy")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "trophy_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                .width(40.dp).height(4.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Trophy / sad icon
            AnimatedVisibility(visible, enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) {
                Box(
                    modifier = Modifier.size(80.dp)
                        .scale(if (status == GameStatus.WON) trophyScale else 1f)
                        .clip(CircleShape)
                        .background(
                            if (status == GameStatus.WON)
                                Brush.radialGradient(listOf(GoldGradientStart, GoldGradientEnd))
                            else Brush.radialGradient(listOf(Color(0xFF555555), Color(0xFF333333)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (status == GameStatus.WON) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                        null, tint = Color.White, modifier = Modifier.size(44.dp)
                    )
                }
            }

            // Win/lose message
            AnimatedVisibility(visible, enter = slideInVertically { it / 2 } + fadeIn(tween(400, 100))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(winMessage, style = MaterialTheme.typography.displayLarge,
                        color = if (status == GameStatus.WON) GoldGradientStart else MaterialTheme.colorScheme.onSurface)
                    if (status == GameStatus.WON) {
                        Text("Solved in $attemptsUsed / $maxAttempts",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("The word was", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(targetWord, fontSize = 28.sp, fontWeight = FontWeight.Black,
                            letterSpacing = 6.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Emoji grid preview
            AnimatedVisibility(visible, enter = fadeIn(tween(400, 200))) {
                EmojiGrid(board, highContrast)
            }

            // Word definition card
            AnimatedVisibility(visible, enter = fadeIn(tween(400, 250))) {
                DefinitionCard(definitionState)
            }

            // Daily countdown
            if (status == GameStatus.WON || status == GameStatus.LOST) {
                AnimatedVisibility(visible, enter = fadeIn(tween(400, 300))) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Next word in", fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(countdown, fontSize = 22.sp, fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface)
                            }
                            Icon(Icons.Default.Timer, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Action buttons
            AnimatedVisibility(visible, enter = slideInVertically { it } + fadeIn(tween(400, 300))) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()) {

                    // Share result
                    Button(
                        onClick = {
                            val text = buildShareText(status, targetWord, attemptsUsed, maxAttempts, board, highContrast)
                            context.startActivity(Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) },
                                "Share your result"))
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TileCorrect)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Share Result", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Challenge a friend
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, challengeShareText) },
                                "Challenge a friend"))
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.SportsEsports, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Challenge a Friend", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    // Invite friends
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, buildInviteText())
                                }, "Invite friends"))
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Invite Friends to Play", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    // Play again (practice only)
                    TextButton(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Play Again", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DefinitionCard(state: DefinitionUiState) {
    when (state) {
        is DefinitionUiState.Loading -> {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
        is DefinitionUiState.Success -> {
            val def = state.definition
            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.MenuBook, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(def.word.lowercase(), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        if (def.phonetic.isNotBlank())
                            Text(def.phonetic, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (def.partOfSpeech.isNotBlank())
                        Text(def.partOfSpeech, fontSize = 12.sp, fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.secondary)
                    Text(def.definition, fontSize = 14.sp, lineHeight = 20.sp)
                    def.example?.let {
                        Text("\"$it\"", fontSize = 13.sp, fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        is DefinitionUiState.Error -> {
            Text(state.message, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        else -> {}
    }
}

@Composable
private fun EmojiGrid(board: List<List<TileState>>, highContrast: Boolean) {
    val filledRows = board.filter { row ->
        row.any { it.state != LetterState.TYPED && it.state != LetterState.EMPTY && it.letter != ' ' }
    }
    if (filledRows.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        filledRows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                row.forEach { tile ->
                    Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(3.dp))
                        .background(tileColor(tile.state, highContrast)))
                }
            }
        }
    }
}

private fun buildShareText(status: GameStatus, targetWord: String, attemptsUsed: Int,
                            maxAttempts: Int, board: List<List<TileState>>, highContrast: Boolean): String {
    val result = if (status == GameStatus.WON) "$attemptsUsed/$maxAttempts" else "X/$maxAttempts"
    val grid = board
        .filter { row -> row.any { it.state == LetterState.CORRECT || it.state == LetterState.PRESENT || it.state == LetterState.ABSENT } }
        .joinToString("\n") { row ->
            row.joinToString("") { tile ->
                when (tile.state) {
                    LetterState.CORRECT -> if (highContrast) "🟧" else "🟩"
                    LetterState.PRESENT -> if (highContrast) "🟦" else "🟨"
                    LetterState.ABSENT  -> "⬛"
                    else -> "⬜"
                }
            }
        }
    return "Wordle $result\n\n$grid\n\nPlay: https://play.google.com/store/apps/details?id=com.wordle.app"
}

private fun buildInviteText() =
    "Hey! I've been playing Wordle — a fun daily word puzzle. Can you guess the 5-letter word in 6 tries? 🟩🟨⬛\n\n" +
    "Download: https://play.google.com/store/apps/details?id=com.wordle.app"
