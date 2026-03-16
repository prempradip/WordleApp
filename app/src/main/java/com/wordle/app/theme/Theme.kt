package com.wordle.app.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Use system default sans-serif with custom weights
val WordleTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 32.sp, letterSpacing = 6.sp),
    titleLarge   = TextStyle(fontWeight = FontWeight.Bold,  fontSize = 22.sp, letterSpacing = 4.sp),
    titleMedium  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelSmall   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp)
)

private val DarkColorScheme = darkColorScheme(
    primary          = TileCorrect,
    onPrimary        = Color.White,
    secondary        = TilePresent,
    onSecondary      = Color.White,
    tertiary         = Color(0xFF4ECDC4),
    background       = BackgroundDark,
    onBackground     = Color.White,
    surface          = SurfaceDark,
    onSurface        = Color.White,
    surfaceVariant   = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFAAAAAA),
    error            = Color(0xFFCF6679),
    errorContainer   = Color(0xFF8B1A2A),
    onErrorContainer = Color(0xFFFFB3BC)
)

private val LightColorScheme = lightColorScheme(
    primary          = TileCorrect,
    onPrimary        = Color.White,
    secondary        = TilePresent,
    onSecondary      = Color.White,
    tertiary         = Color(0xFF2A9D8F),
    background       = BackgroundLight,
    onBackground     = Color(0xFF1A1A1B),
    surface          = SurfaceLight,
    onSurface        = Color(0xFF1A1A1B),
    surfaceVariant   = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFF555555),
    error            = Color(0xFFB00020),
    errorContainer   = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun WordleTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = WordleTypography,
        content     = content
    )
}
