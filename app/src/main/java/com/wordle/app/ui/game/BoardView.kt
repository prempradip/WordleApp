package com.wordle.app.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.app.core.LetterState
import com.wordle.app.core.TileState
import com.wordle.app.theme.*
import kotlinx.coroutines.delay

@Composable
fun BoardView(
    board: List<List<TileState>>,
    currentRow: Int,
    highContrast: Boolean,
    hintRevealedPositions: Set<Int>,
    shakeRow: Int,
    revealRow: Int,
    tileSize: Int = 58,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        board.forEachIndexed { rowIdx, row ->
            val doShake  = rowIdx == shakeRow
            val doReveal = rowIdx == revealRow

            AnimatedRow(
                tiles = row,
                rowIndex = rowIdx,
                currentRow = currentRow,
                highContrast = highContrast,
                hintRevealedPositions = hintRevealedPositions,
                doShake = doShake,
                doReveal = doReveal,
                tileSize = tileSize
            )
        }
    }
}

@Composable
private fun AnimatedRow(
    tiles: List<TileState>,
    rowIndex: Int,
    currentRow: Int,
    highContrast: Boolean,
    hintRevealedPositions: Set<Int>,
    doShake: Boolean,
    doReveal: Boolean,
    tileSize: Int = 58
) {
    // Shake animation
    val shakeOffset by animateFloatAsState(
        targetValue = if (doShake) 1f else 0f,
        animationSpec = if (doShake) {
            keyframes {
                durationMillis = 500
                0f  at 0
                -12f at 60
                12f  at 120
                -10f at 180
                10f  at 240
                -6f  at 300
                6f   at 360
                0f   at 420
            }
        } else snap(),
        label = "shake_$rowIndex"
    )

    Row(
        modifier = Modifier.offset(x = shakeOffset.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tiles.forEachIndexed { colIdx, tile ->
            val isHinted = rowIndex == currentRow && colIdx in hintRevealedPositions
            TileView(
                tile = tile,
                highContrast = highContrast,
                isHinted = isHinted,
                flipDelay = if (doReveal) colIdx * 300L else 0L,
                shouldFlip = doReveal,
                sizeDp = tileSize
            )
        }
    }
}

@Composable
fun TileView(
    tile: TileState,
    highContrast: Boolean,
    isHinted: Boolean,
    flipDelay: Long = 0L,
    shouldFlip: Boolean = false,
    sizeDp: Int = 58
) {
    // Pop scale when a letter is typed
    val popScale by animateFloatAsState(
        targetValue = if (tile.state == LetterState.TYPED && tile.letter != ' ') 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pop_${tile.letter}"
    )

    // Flip animation (Y-axis rotation simulated via scaleX)
    var flipped by remember(shouldFlip, flipDelay) { mutableStateOf(false) }
    LaunchedEffect(shouldFlip) {
        if (shouldFlip) {
            delay(flipDelay)
            flipped = true
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "flip_rotation"
    )

    // During first half of flip show empty/typed color, second half show result color
    val showResult = rotation > 90f
    val bgColor = when {
        isHinted -> TilePresent.copy(alpha = 0.35f)
        showResult -> tileColor(tile.state, highContrast)
        tile.state == LetterState.ABSENT || tile.state == LetterState.CORRECT || tile.state == LetterState.PRESENT ->
            tileColor(tile.state, highContrast) // already revealed rows (no flip)
        else -> Color.Transparent
    }

    val borderColor = when {
        isHinted -> TilePresent
        tile.state == LetterState.EMPTY  -> TileBorderDefault
        tile.state == LetterState.TYPED  -> TileBorderTyped
        else -> bgColor
    }

    val textColor = when {
        tile.state == LetterState.EMPTY || tile.state == LetterState.TYPED ->
            MaterialTheme.colorScheme.onBackground
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .scale(popScale)
            .graphicsLayer {
                // Simulate card flip with rotationY
                rotationY = if (shouldFlip) rotation else 0f
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (tile.letter != ' ') {
            Text(
                text = tile.letter.toString(),
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

fun tileColor(state: LetterState, highContrast: Boolean): Color = when (state) {
    LetterState.CORRECT -> if (highContrast) TileCorrectHC else TileCorrect
    LetterState.PRESENT -> if (highContrast) TilePresentHC else TilePresent
    LetterState.ABSENT  -> TileAbsent
    else                -> Color.Transparent
}
