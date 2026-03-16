package com.wordle.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wordle.app.data.ChallengeRepository
import com.wordle.app.data.local.StatsDao
import com.wordle.app.notification.DailyNotificationHelper
import com.wordle.app.theme.WordleTheme
import com.wordle.app.ui.game.GameScreen
import com.wordle.app.ui.game.GameViewModel
import com.wordle.app.ui.profile.ProfileScreen
import com.wordle.app.ui.settings.SettingsScreen
import com.wordle.app.ui.stats.StatsScreen
import com.wordle.app.ui.tutorial.TutorialScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private val android.content.Context.mainDataStore
    by preferencesDataStore(name = "main_prefs")
private val KEY_TUTORIAL_DONE = booleanPreferencesKey("tutorial_done")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var statsDao: StatsDao
    @Inject lateinit var challengeRepository: ChallengeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DailyNotificationHelper.schedule(this)

        // Check if tutorial has been shown
        val tutorialDone = runBlocking {
            mainDataStore.data.map { it[KEY_TUTORIAL_DONE] ?: false }.first()
        }

        // Parse challenge deep link if present
        val challengeWord = intent?.data?.let { uri ->
            if (uri.host == "wordle.app" && uri.path == "/challenge") {
                uri.getQueryParameter("w")?.let { token ->
                    challengeRepository.decodeChallengeToken(token)
                }
            } else null
        }

        setContent {
            val navController = rememberNavController()
            val gameViewModel: GameViewModel = hiltViewModel()
            val darkTheme by gameViewModel.darkTheme.collectAsState()

            val startDest = when {
                !tutorialDone -> "tutorial"
                challengeWord != null -> "challenge/{word}"
                else -> "game"
            }

            WordleTheme(darkTheme = darkTheme) {
                NavHost(navController = navController, startDestination = startDest) {

                    composable("tutorial") {
                        TutorialScreen(onDone = {
                            runBlocking {
                                mainDataStore.edit { it[KEY_TUTORIAL_DONE] = true }
                            }
                            navController.navigate("game") {
                                popUpTo("tutorial") { inclusive = true }
                            }
                        })
                    }

                    composable("game") {
                        GameScreen(
                            onNavigateToStats    = { navController.navigate("stats") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToProfile  = { navController.navigate("profile") },
                            viewModel = gameViewModel
                        )
                    }

                    composable(
                        route = "challenge/{word}",
                        arguments = listOf(navArgument("word") { type = NavType.StringType })
                    ) { backStack ->
                        val word = backStack.arguments?.getString("word") ?: ""
                        LaunchedEffect(word) {
                            if (word.isNotBlank()) gameViewModel.startChallenge(word)
                            navController.navigate("game") {
                                popUpTo("challenge/{word}") { inclusive = true }
                            }
                        }
                    }

                    composable("settings") {
                        SettingsScreen(
                            onBack    = { navController.popBackStack() },
                            viewModel = gameViewModel
                        )
                    }

                    composable("stats") {
                        StatsScreen(
                            onBack    = { navController.popBackStack() },
                            viewModel = gameViewModel,
                            statsDao  = statsDao
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            onBack    = { navController.popBackStack() },
                            viewModel = gameViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link when app is already running
        intent.data?.let { uri ->
            if (uri.host == "wordle.app" && uri.path == "/challenge") {
                uri.getQueryParameter("w")?.let { token ->
                    challengeRepository.decodeChallengeToken(token)?.let { word ->
                        // Re-launch with challenge word via intent
                        val relaunch = Intent(this, MainActivity::class.java).apply {
                            data = uri
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(relaunch)
                    }
                }
            }
        }
    }
}
