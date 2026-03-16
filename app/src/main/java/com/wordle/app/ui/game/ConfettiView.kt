package com.wordle.app.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.wordle.app.theme.ConfettiColors
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float,          // 0..1 normalized
    val color: Color,
    val size: Float,
    val speed: Float,
    val wobble: Float,
    val rotation: Float,
    val shape: Int         // 0=rect, 1=circle, 2=line
)

@Composable
fun ConfettiView(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    if (!active) return

    val particles = remember {
        List(120) {
            Particle(
                x        = Random.nextFloat(),
                color    = ConfettiColors.random(),
                size     = Random.nextFloat() * 10f + 6f,
                speed    = Random.nextFloat() * 0.4f + 0.2f,
                wobble   = Random.nextFloat() * 4f - 2f,
                rotation = Random.nextFloat() * 360f,
                shape    = Random.nextInt(3)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val y = ((progress * p.speed * 1.5f) % 1f) * (h + 60f) - 30f
            val x = p.x * w + sin(progress * 6.28f * 2f + p.wobble) * 30f
            val rot = p.rotation + progress * 360f * p.speed

            when (p.shape) {
                0 -> rotate(rot, pivot = Offset(x, y)) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(x - p.size / 2, y - p.size / 2),
                        size = Size(p.size, p.size * 0.6f)
                    )
                }
                1 -> drawCircle(
                    color = p.color,
                    radius = p.size / 2,
                    center = Offset(x, y)
                )
                else -> rotate(rot, pivot = Offset(x, y)) {
                    drawLine(
                        color = p.color,
                        start = Offset(x - p.size, y),
                        end   = Offset(x + p.size, y),
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}
