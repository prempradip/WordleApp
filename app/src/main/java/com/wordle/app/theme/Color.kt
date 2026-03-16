package com.wordle.app.theme

import androidx.compose.ui.graphics.Color

// Tile states
val TileCorrect = Color(0xFF538D4E)
val TilePresent = Color(0xFFB59F3B)
val TileAbsent  = Color(0xFF3A3A3C)
val TileBorderDefault = Color(0xFF3A3A3C)
val TileBorderTyped   = Color(0xFF999999)

// High contrast
val TileCorrectHC = Color(0xFFFF8C00)
val TilePresentHC = Color(0xFF1A9FFF)

// Keyboard
val KeyBackground        = Color(0xFF818384)
val KeyBackgroundLight   = Color(0xFFD3D6DA)

// App backgrounds
val BackgroundDark  = Color(0xFF121213)
val BackgroundLight = Color(0xFFFFFFFF)
val SurfaceDark     = Color(0xFF1A1A1B)
val SurfaceLight    = Color(0xFFF6F7F8)

// Celebration confetti palette
val ConfettiColors = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFF4ECDC4),
    Color(0xFF45B7D1), Color(0xFF96CEB4), Color(0xFFFF8B94),
    Color(0xFFA8E6CF), Color(0xFFFFD3B6), Color(0xFFD4A5A5),
    Color(0xFF9B59B6), Color(0xFF3498DB), Color(0xFF2ECC71)
)

// Gradient accent
val GoldGradientStart = Color(0xFFFFD700)
val GoldGradientEnd   = Color(0xFFFFA500)
val GreenGlow         = Color(0xFF538D4E).copy(alpha = 0.4f)
