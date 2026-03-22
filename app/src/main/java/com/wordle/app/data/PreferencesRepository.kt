package com.wordle.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wordle.app.core.Difficulty
import com.wordle.app.core.GameConfig
import com.wordle.app.core.GameMode
import com.wordle.app.core.Language
import com.wordle.app.core.WordLength
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "wordle_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_LANGUAGE    = stringPreferencesKey("language")
        val KEY_DIFFICULTY  = stringPreferencesKey("difficulty")
        val KEY_GAME_MODE   = stringPreferencesKey("game_mode")
        val KEY_WORD_LENGTH = stringPreferencesKey("word_length")
        val KEY_DARK_THEME  = booleanPreferencesKey("dark_theme")
        val KEY_HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
    }

    val gameConfig: Flow<GameConfig> = context.dataStore.data.map { prefs ->
        GameConfig(
            language   = Language.entries.find { it.code == prefs[KEY_LANGUAGE] } ?: Language.ENGLISH,
            difficulty = Difficulty.entries.find { it.name == prefs[KEY_DIFFICULTY] } ?: Difficulty.NORMAL,
            mode       = GameMode.entries.find { it.name == prefs[KEY_GAME_MODE] } ?: GameMode.DAILY,
            wordLength = WordLength.entries.find { it.name == prefs[KEY_WORD_LENGTH] } ?: WordLength.FIVE
        )
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { it[KEY_DARK_THEME] ?: false }
    val highContrast: Flow<Boolean> = context.dataStore.data.map { it[KEY_HIGH_CONTRAST] ?: false }

    suspend fun saveConfig(config: GameConfig) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE]    = config.language.code
            prefs[KEY_DIFFICULTY]  = config.difficulty.name
            prefs[KEY_GAME_MODE]   = config.mode.name
            prefs[KEY_WORD_LENGTH] = config.wordLength.name
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HIGH_CONTRAST] = enabled }
    }
}
