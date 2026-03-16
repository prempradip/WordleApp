package com.wordle.app.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.app.core.LetterState
import com.wordle.app.theme.*

private val ROW1 = listOf('Q','W','E','R','T','Y','U','I','O','P')
private val ROW2 = listOf('A','S','D','F','G','H','J','K','L')
private val ROW3 = listOf('Z','X','C','V','B','N','M')

@Composable
fun KeyboardView(
    keyStates: Map<Char, LetterState>,
    highContrast: Boolean,
    isDark: Boolean,
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KeyRow(ROW1, keyStates, highContrast, isDark, onKey)
        KeyRow(ROW2, keyStates, highContrast, isDark, onKey)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionKey(label = "ENTER", isDark = isDark, onClick = onSubmit)
            ROW3.forEach { ch ->
                LetterKey(ch, keyStates[ch], highContrast, isDark, onKey)
            }
            ActionKey(label = "⌫", isDark = isDark, onClick = onDelete)
        }
    }
}

@Composable
private fun KeyRow(
    letters: List<Char>,
    keyStates: Map<Char, LetterState>,
    highContrast: Boolean,
    isDark: Boolean,
    onKey: (Char) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        letters.forEach { ch ->
            LetterKey(ch, keyStates[ch], highContrast, isDark, onKey)
        }
    }
}

@Composable
private fun LetterKey(
    char: Char,
    state: LetterState?,
    highContrast: Boolean,
    isDark: Boolean,
    onKey: (Char) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val targetBg = when (state) {
        LetterState.CORRECT -> if (highContrast) TileCorrectHC else TileCorrect
        LetterState.PRESENT -> if (highContrast) TilePresentHC else TilePresent
        LetterState.ABSENT  -> TileAbsent
        else -> if (isDark) KeyBackground else KeyBackgroundLight
    }
    val bg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = 300),
        label = "key_color_$char"
    )
    val textColor = when (state) {
        LetterState.ABSENT, LetterState.CORRECT, LetterState.PRESENT -> Color.White
        else -> if (isDark) Color.White else Color(0xFF1A1A1B)
    }

    Box(
        modifier = Modifier
            .width(33.dp)
            .height(58.dp)
            .shadow(2.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = Color.White.copy(alpha = 0.3f))
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onKey(char)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionKey(label: String, isDark: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val bg = if (isDark) KeyBackground else KeyBackgroundLight
    Box(
        modifier = Modifier
            .width(54.dp)
            .height(58.dp)
            .shadow(2.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = Color.White.copy(alpha = 0.3f))
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isDark) Color.White else Color(0xFF1A1A1B),
            fontSize = if (label == "⌫") 18.sp else 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
