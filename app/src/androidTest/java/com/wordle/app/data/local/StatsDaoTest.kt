package com.wordle.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatsDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: StatsDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.statsDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun insertAndQueryStats() = runTest {
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = true, attemptsUsed = 3))
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = false, attemptsUsed = 6))

        dao.getStats("en", "NORMAL").test {
            val list = awaitItem()
            assertThat(list).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getWinCountReturnsOnlyWins() = runTest {
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = true, attemptsUsed = 3))
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = true, attemptsUsed = 4))
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = false, attemptsUsed = 6))

        val wins = dao.getWinCount("en", "NORMAL")
        assertThat(wins).isEqualTo(2)
    }

    @Test
    fun getTotalGamesCountsAll() = runTest {
        repeat(5) {
            dao.insert(StatsEntity(languageCode = "en", difficulty = "HARD", won = it % 2 == 0, attemptsUsed = 3))
        }
        val total = dao.getTotalGames("en", "HARD")
        assertThat(total).isEqualTo(5)
    }

    @Test
    fun statsAreFilteredByLanguage() = runTest {
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = true, attemptsUsed = 3))
        dao.insert(StatsEntity(languageCode = "es", difficulty = "NORMAL", won = true, attemptsUsed = 4))

        val enWins = dao.getWinCount("en", "NORMAL")
        val esWins = dao.getWinCount("es", "NORMAL")
        assertThat(enWins).isEqualTo(1)
        assertThat(esWins).isEqualTo(1)
    }

    @Test
    fun statsAreFilteredByDifficulty() = runTest {
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = true, attemptsUsed = 3))
        dao.insert(StatsEntity(languageCode = "en", difficulty = "HARD", won = true, attemptsUsed = 5))

        val normalTotal = dao.getTotalGames("en", "NORMAL")
        val hardTotal = dao.getTotalGames("en", "HARD")
        assertThat(normalTotal).isEqualTo(1)
        assertThat(hardTotal).isEqualTo(1)
    }

    @Test
    fun clearAllRemovesAllRows() = runTest {
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = true, attemptsUsed = 3))
        dao.clearAll()
        val total = dao.getTotalGames("en", "NORMAL")
        assertThat(total).isEqualTo(0)
    }

    @Test
    fun emptyDatabaseReturnsZeroWins() = runTest {
        val wins = dao.getWinCount("en", "NORMAL")
        assertThat(wins).isEqualTo(0)
    }

    @Test
    fun statsOrderedByTimestampDescending() = runTest {
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = true, attemptsUsed = 3, timestamp = 1000L))
        dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = false, attemptsUsed = 6, timestamp = 2000L))

        dao.getStats("en", "NORMAL").test {
            val list = awaitItem()
            assertThat(list[0].timestamp).isGreaterThan(list[1].timestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun statsFromOtherLanguageDoNotAffectWinCount() = runTest {
        // Insert wins for all 4 languages
        listOf("en", "es", "fr", "de").forEach { lang ->
            repeat(3) {
                dao.insert(StatsEntity(languageCode = lang, difficulty = "NORMAL", won = true, attemptsUsed = 3))
            }
        }
        // Each language should have exactly 3 wins, not 12
        assertThat(dao.getWinCount("en", "NORMAL")).isEqualTo(3)
        assertThat(dao.getWinCount("es", "NORMAL")).isEqualTo(3)
        assertThat(dao.getWinCount("fr", "NORMAL")).isEqualTo(3)
        assertThat(dao.getWinCount("de", "NORMAL")).isEqualTo(3)
    }

    @Test
    fun winRateCalculationIsCorrect() = runTest {
        repeat(7) { dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = true, attemptsUsed = 3)) }
        repeat(3) { dao.insert(StatsEntity(languageCode = "en", difficulty = "NORMAL", won = false, attemptsUsed = 6)) }

        val wins = dao.getWinCount("en", "NORMAL")
        val total = dao.getTotalGames("en", "NORMAL")
        val winRate = wins.toFloat() / total.toFloat()

        assertThat(wins).isEqualTo(7)
        assertThat(total).isEqualTo(10)
        assertThat(winRate).isWithin(0.01f).of(0.7f)
    }

    @Test
    fun insertedEntityHasCorrectFields() = runTest {
        dao.insert(StatsEntity(languageCode = "fr", difficulty = "EASY", won = true, attemptsUsed = 2))

        dao.getStats("fr", "EASY").test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            val entity = list[0]
            assertThat(entity.languageCode).isEqualTo("fr")
            assertThat(entity.difficulty).isEqualTo("EASY")
            assertThat(entity.won).isTrue()
            assertThat(entity.attemptsUsed).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
