package com.wordle.app.data

import com.wordle.app.data.local.AchievementDao
import com.wordle.app.data.local.AchievementEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String   // emoji icon
)

@Singleton
class AchievementRepository @Inject constructor(
    private val dao: AchievementDao
) {
    companion object {
        val ALL = listOf(
            Achievement("first_win",       "First Blood",        "Win your first game",                    "🏆"),
            Achievement("genius",          "Genius",             "Solve in 1 attempt",                     "🧠"),
            Achievement("streak_3",        "On Fire",            "Win 3 games in a row",                   "🔥"),
            Achievement("streak_7",        "Week Warrior",       "Win 7 games in a row",                   "⚡"),
            Achievement("streak_30",       "Monthly Master",     "Win 30 games in a row",                  "👑"),
            Achievement("hard_mode_win",   "Hard Mode Hero",     "Win on Hard difficulty",                 "💪"),
            Achievement("no_hints",        "No Peeking",         "Win without using any hints",            "🙈"),
            Achievement("polyglot",        "Polyglot",           "Win in all 4 languages",                 "🌍"),
            Achievement("daily_7",         "Daily Devotee",      "Complete 7 daily puzzles",               "📅"),
            Achievement("daily_30",        "Monthly Devotee",    "Complete 30 daily puzzles",              "🗓️"),
            Achievement("practice_10",     "Practice Makes Perfect", "Play 10 practice games",             "🎯"),
            Achievement("speed_demon",     "Speed Demon",        "Solve in 2 attempts",                    "⚡"),
            Achievement("challenge_sent",  "Challenger",         "Send a word challenge to a friend",      "🤝"),
            Achievement("definition_fan",  "Word Nerd",          "View 10 word definitions",               "📖")
        )
    }

    val unlockedIds: Flow<List<String>> = dao.getUnlockedIds()

    /** Returns the Achievement if it was newly unlocked, null if already had it. */
    suspend fun tryUnlock(id: String): Achievement? {
        val entity = AchievementEntity(id)
        val inserted = dao.unlock(entity)
        return if (inserted != -1L) ALL.find { it.id == id } else null
    }

    suspend fun isUnlocked(id: String): Boolean = dao.isUnlocked(id) > 0
}
