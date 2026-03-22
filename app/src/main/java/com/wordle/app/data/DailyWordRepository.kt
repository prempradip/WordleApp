package com.wordle.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wordle.app.core.Language
import com.wordle.app.core.WordLength
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dailyDataStore by preferencesDataStore(name = "daily_word")

@Singleton
class DailyWordRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordRepository: WordRepository
) {
    companion object {
        val KEY_DAILY_WORD       = stringPreferencesKey("daily_word")
        val KEY_DAILY_DATE       = longPreferencesKey("daily_date")
        val KEY_DAILY_COMPLETED  = stringPreferencesKey("daily_completed") // "WON"|"LOST"|""
        val KEY_DAILY_BOARD      = stringPreferencesKey("daily_board")     // JSON snapshot
        val KEY_DAILY_LANG       = stringPreferencesKey("daily_lang")
    }

    /** Returns today's word for the given language and word length, deterministically seeded by date. */
    fun getDailyWord(language: Language, wordLength: WordLength): String {
        val today = todayEpochDay()
        // Seed the word selection by day + language + wordLength so it's consistent per device
        val words = wordRepository.getAllWords(language, wordLength)
        val idx = ((today * 31L + language.ordinal * 7L + wordLength.ordinal * 13L) % words.size).toInt()
        return words[idx].uppercase()
    }

    /** Returns ms until midnight (next daily reset). */
    fun msUntilNextWord(): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis - System.currentTimeMillis()
    }

    val dailyCompletedStatus: Flow<String> = context.dailyDataStore.data
        .map { it[KEY_DAILY_COMPLETED] ?: "" }

    val savedDailyBoard: Flow<String> = context.dailyDataStore.data
        .map { it[KEY_DAILY_BOARD] ?: "" }

    suspend fun saveDailyResult(status: String, boardJson: String, lang: String) {
        context.dailyDataStore.edit { prefs ->
            prefs[KEY_DAILY_COMPLETED] = status
            prefs[KEY_DAILY_BOARD]     = boardJson
            prefs[KEY_DAILY_DATE]      = todayEpochDay()
            prefs[KEY_DAILY_LANG]      = lang
        }
    }

    suspend fun isTodayCompleted(language: Language): Boolean {
        val prefs = context.dailyDataStore.data.first()
        val savedDate = prefs[KEY_DAILY_DATE] ?: -1L
        val savedLang = prefs[KEY_DAILY_LANG] ?: ""
        return savedDate == todayEpochDay() && savedLang == language.code &&
               (prefs[KEY_DAILY_COMPLETED] ?: "").isNotEmpty()
    }

    suspend fun clearDailyIfNewDay() {
        val prefs = context.dailyDataStore.data.first()
        val savedDate = prefs[KEY_DAILY_DATE] ?: -1L
        if (savedDate != todayEpochDay()) {
            context.dailyDataStore.edit { it.clear() }
        }
    }

    private fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) * 1000L + cal.get(Calendar.DAY_OF_YEAR)
    }
}
