package com.wordle.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wordle.app.core.Difficulty
import com.wordle.app.core.GameConfig
import com.wordle.app.core.GameMode
import com.wordle.app.core.Language
import com.wordle.app.core.WordLength
import com.wordle.app.data.AppTheme
import com.wordle.app.theme.TileCorrect
import com.wordle.app.ui.game.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsState()
    val darkTheme    by viewModel.darkTheme.collectAsState()
    val highContrast by viewModel.highContrast.collectAsState()
    val appTheme     by viewModel.appTheme.collectAsState()
    val fontSize     by viewModel.fontSize.collectAsState()
    val tileSize     by viewModel.tileSize.collectAsState()
    val soundOn      by viewModel.soundOn.collectAsState()
    val hapticLevel  by viewModel.hapticLevel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Game Mode ──────────────────────────────────────────────
            SectionLabel("Game Mode")
            SettingsCard {
                GameMode.entries.forEachIndexed { idx, mode ->
                    if (idx > 0) Divider()
                    RadioRow(
                        label = if (mode == GameMode.DAILY) "Daily" else "Practice",
                        sublabel = if (mode == GameMode.DAILY)
                            "One word per day, shared globally"
                        else
                            "Unlimited games, no streak impact",
                        selected = state.config.mode == mode,
                        onClick = { viewModel.updateConfig(state.config.copy(mode = mode)) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Language ───────────────────────────────────────────────
            SectionLabel("Language")
            SettingsCard {
                Language.entries.forEachIndexed { idx, lang ->
                    if (idx > 0) Divider()
                    RadioRow(
                        label = lang.displayName,
                        sublabel = "Supports ${lang.supportedLengths.size} word lengths",
                        selected = state.config.language == lang,
                        onClick = { viewModel.updateConfig(GameConfig(lang, state.config.wordLength, state.config.difficulty, state.config.mode)) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Word Length ────────────────────────────────────────────
            SectionLabel("Word Length")
            SettingsCard {
                state.config.language.supportedLengths.forEachIndexed { idx, wl ->
                    if (idx > 0) Divider()
                    RadioRow(
                        label = wl.displayName,
                        sublabel = "${wl.value}-letter words",
                        selected = state.config.wordLength == wl,
                        onClick = { viewModel.updateConfig(state.config.copy(wordLength = wl)) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Difficulty ─────────────────────────────────────────────
            SectionLabel("Difficulty")
            SettingsCard {
                Difficulty.entries.forEachIndexed { idx, diff ->
                    if (idx > 0) Divider()
                    RadioRow(
                        label = diff.label,
                        sublabel = "${diff.maxAttempts} attempts · ${diff.hintsAllowed} hint${if (diff.hintsAllowed != 1) "s" else ""}${if (diff == Difficulty.HARD) " · Must use clues" else ""}",
                        selected = state.config.difficulty == diff,
                        onClick = { viewModel.updateConfig(GameConfig(state.config.language, state.config.wordLength, diff, state.config.mode)) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Theme ──────────────────────────────────────────────────
            SectionLabel("Color Theme")
            ThemeSelector(current = appTheme, onSelect = viewModel::setAppTheme)

            Spacer(Modifier.height(4.dp))

            // ── Appearance ─────────────────────────────────────────────
            SectionLabel("Appearance")
            SettingsCard {
                SwitchRow(Icons.Default.DarkMode, "Dark Theme", "Use dark background",
                    darkTheme, viewModel::setDarkTheme)
                Divider()
                SwitchRow(Icons.Default.Contrast, "High Contrast",
                    "Orange & blue instead of green & yellow",
                    highContrast, viewModel::setHighContrast)
                Divider()
                SliderRow(Icons.Default.FormatSize, "Font Size",
                    listOf("Small", "Normal", "Large"), fontSize) { viewModel.setFontSize(it) }
                Divider()
                SliderRow(Icons.Default.GridView, "Tile Size",
                    listOf("Compact", "Normal", "Large"), tileSize) { viewModel.setTileSize(it) }
            }

            Spacer(Modifier.height(4.dp))

            // ── Audio & Haptics ────────────────────────────────────────
            SectionLabel("Audio & Haptics")
            SettingsCard {
                SwitchRow(Icons.AutoMirrored.Filled.VolumeUp, "Sound Effects",
                    "Key clicks and win fanfare", soundOn, viewModel::setSoundOn)
                Divider()
                SliderRow(Icons.Default.Vibration, "Haptic Feedback",
                    listOf("Off", "Light", "Strong"), hapticLevel) { viewModel.setHapticLevel(it) }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Theme Selector ─────────────────────────────────────────────────────────────

@Composable
private fun ThemeSelector(current: AppTheme, onSelect: (AppTheme) -> Unit) {
    val themeColors = mapOf(
        AppTheme.CLASSIC    to listOf(Color(0xFF538D4E), Color(0xFFB59F3B), Color(0xFF121213)),
        AppTheme.AMOLED     to listOf(Color(0xFF00FF88), Color(0xFFFFFF00), Color(0xFF000000)),
        AppTheme.SOLARIZED  to listOf(Color(0xFF859900), Color(0xFFB58900), Color(0xFF002B36)),
        AppTheme.PASTEL     to listOf(Color(0xFF90EE90), Color(0xFFFFD700), Color(0xFFFFF0F5)),
        AppTheme.OCEAN      to listOf(Color(0xFF006994), Color(0xFF40E0D0), Color(0xFF001F3F))
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppTheme.entries.forEach { theme ->
            val colors = themeColors[theme] ?: return@forEach
            val selected = theme == current
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (selected) 2.dp else 0.dp,
                        color = if (selected) TileCorrect else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelect(theme) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    colors.forEach { c ->
                        Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(c))
                    }
                }
                Text(theme.displayName, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp), letterSpacing = 1.sp)
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()) { Column(content = content) }
}

@Composable
private fun Divider() = HorizontalDivider(
    modifier = Modifier.padding(horizontal = 16.dp),
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
)

@Composable
private fun RadioRow(label: String, sublabel: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(sublabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
private fun SwitchRow(icon: ImageVector, label: String, sublabel: String,
                      checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(sublabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderRow(icon: ImageVector, label: String, options: List<String>,
                      value: Int, onValueChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(options.getOrElse(value) { "" }, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..(options.size - 1).toFloat(),
                steps = options.size - 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
