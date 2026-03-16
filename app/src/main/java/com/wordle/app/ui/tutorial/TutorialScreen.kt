package com.wordle.app.ui.tutorial

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.app.theme.*

private data class TutorialPage(
    val title: String,
    val body: String,
    val example: List<Pair<Char, Color>>?  // letter + tile color
)

private val PAGES = listOf(
    TutorialPage(
        "How to Play",
        "Guess the hidden word in 6 tries.\nEach guess must be a valid word.\nHit ENTER to submit.",
        null
    ),
    TutorialPage(
        "Green = Correct",
        "The letter W is in the word\nand in the correct position.",
        listOf('W' to TileCorrect, 'O' to TileAbsent, 'R' to TileAbsent, 'D' to TileAbsent, 'S' to TileAbsent)
    ),
    TutorialPage(
        "Yellow = Wrong Position",
        "The letter I is in the word\nbut in the wrong position.",
        listOf('P' to TileAbsent, 'I' to TilePresent, 'L' to TileAbsent, 'L' to TileAbsent, 'S' to TileAbsent)
    ),
    TutorialPage(
        "Gray = Not in Word",
        "The letter U is not in the word\nat all.",
        listOf('V' to TileAbsent, 'A' to TileAbsent, 'U' to TileAbsent, 'L' to TileAbsent, 'T' to TileAbsent)
    ),
    TutorialPage(
        "Modes & Difficulty",
        "Daily: One word per day, shared globally.\nPractice: Unlimited games.\nHard: Must reuse revealed clues.",
        null
    ),
    TutorialPage(
        "Hints & Achievements",
        "Use the 💡 hint button to reveal a letter.\nEarn achievements as you play.\nShare your results with friends!",
        null
    )
)

@Composable
fun TutorialScreen(onDone: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val current = PAGES[page]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Page indicator dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PAGES.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == page) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == page) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "tutorial_page"
            ) { p ->
                val pg = PAGES[p]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        pg.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    pg.example?.let { tiles ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            tiles.forEach { (letter, color) ->
                                TutorialTile(letter, color)
                            }
                        }
                    }

                    Text(
                        pg.body,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (page > 0) {
                    TextButton(onClick = { page-- }) { Text("Back") }
                } else {
                    Spacer(Modifier.width(80.dp))
                }

                if (page < PAGES.lastIndex) {
                    Button(
                        onClick = { page++ },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Next") }
                } else {
                    Button(
                        onClick = onDone,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TileCorrect)
                    ) { Text("Let's Play!", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun TutorialTile(letter: Char, color: Color) {
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(300); revealed = true }
    val bg by animateColorAsState(
        targetValue = if (revealed) color else Color.Transparent,
        animationSpec = tween(400),
        label = "tile_reveal"
    )
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .then(if (!revealed) Modifier.background(Color.Transparent) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(letter.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}
