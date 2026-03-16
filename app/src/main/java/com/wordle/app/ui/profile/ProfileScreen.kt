package com.wordle.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wordle.app.data.AchievementRepository
import com.wordle.app.theme.TileCorrect
import com.wordle.app.theme.TilePresent
import com.wordle.app.ui.game.GameViewModel

private val AVATARS = listOf("🦊","🐺","🦁","🐯","🐻","🦝","🐸","🦄","🐙","🦋","🐬","🦅")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val username    by viewModel.username.collectAsState()
    val avatarIdx   by viewModel.avatarIdx.collectAsState()
    val unlockedIds by viewModel.unlockedAchievements.collectAsState()
    var editingName by remember { mutableStateOf(false) }
    var nameInput   by remember(username) { mutableStateOf(username) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Avatar
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showAvatarPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(AVATARS.getOrElse(avatarIdx) { "🦊" }, fontSize = 48.sp)
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(TileCorrect)
                        .clickable { showAvatarPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            // Username
            if (editingName) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(20) },
                    label = { Text("Username") },
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = {
                            viewModel.setUsername(nameInput.trim().ifBlank { "Wordler" })
                            editingName = false
                        }) { Text("Save") }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { editingName = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Achievements
            Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth())
            Text("${unlockedIds.size} / ${AchievementRepository.ALL.size} unlocked",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth())

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 600.dp)
            ) {
                items(AchievementRepository.ALL) { achievement ->
                    val unlocked = achievement.id in unlockedIds
                    AchievementCard(achievement.icon, achievement.title, achievement.description, unlocked)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = { Text("Choose Avatar") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AVATARS) { emoji ->
                        val idx = AVATARS.indexOf(emoji)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == avatarIdx) TileCorrect.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(if (idx == avatarIdx) 2.dp else 0.dp, TileCorrect, CircleShape)
                                .clickable { viewModel.setAvatarIdx(idx); showAvatarPicker = false },
                            contentAlignment = Alignment.Center
                        ) { Text(emoji, fontSize = 28.sp) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAvatarPicker = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun AchievementCard(icon: String, title: String, description: String, unlocked: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) TileCorrect.copy(alpha = 0.15f)
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(if (unlocked) icon else "🔒", fontSize = 24.sp)
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                color = if (unlocked) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text(description, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (unlocked) 1f else 0.4f),
                lineHeight = 14.sp)
        }
    }
}
