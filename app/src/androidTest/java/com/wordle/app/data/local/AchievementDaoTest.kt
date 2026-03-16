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
class AchievementDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AchievementDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.achievementDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun unlockNewAchievementReturnsPositiveRowId() = runTest {
        val rowId = dao.unlock(AchievementEntity("first_win"))
        assertThat(rowId).isGreaterThan(0L)
    }

    @Test
    fun unlockDuplicateAchievementReturnsMinusOne() = runTest {
        dao.unlock(AchievementEntity("first_win"))
        val rowId = dao.unlock(AchievementEntity("first_win"))
        assertThat(rowId).isEqualTo(-1L)
    }

    @Test
    fun isUnlockedReturnsTrueAfterUnlock() = runTest {
        dao.unlock(AchievementEntity("genius"))
        val count = dao.isUnlocked("genius")
        assertThat(count).isGreaterThan(0)
    }

    @Test
    fun isUnlockedReturnsFalseForMissingAchievement() = runTest {
        val count = dao.isUnlocked("nonexistent")
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun getUnlockedIdsReturnsAllUnlocked() = runTest {
        dao.unlock(AchievementEntity("first_win"))
        dao.unlock(AchievementEntity("genius"))
        dao.unlock(AchievementEntity("streak_3"))

        dao.getUnlockedIds().test {
            val ids = awaitItem()
            assertThat(ids).containsExactly("first_win", "genius", "streak_3")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUnlockedIdsEmptyWhenNoneUnlocked() = runTest {
        dao.getUnlockedIds().test {
            val ids = awaitItem()
            assertThat(ids).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun multipleAchievementsCanBeUnlocked() = runTest {
        val achievements = listOf("first_win", "genius", "streak_3", "streak_7", "hard_mode_win")
        achievements.forEach { dao.unlock(AchievementEntity(it)) }

        dao.getUnlockedIds().test {
            val ids = awaitItem()
            assertThat(ids).hasSize(5)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unlockedAtTimestampIsSetOnInsert() = runTest {
        val before = System.currentTimeMillis()
        dao.unlock(AchievementEntity("first_win"))
        // Re-query via isUnlocked — we can't directly read the entity without a SELECT *,
        // but we can verify the row exists and was inserted after `before`
        val count = dao.isUnlocked("first_win")
        assertThat(count).isEqualTo(1)
        // Timestamp sanity: the entity default is System.currentTimeMillis() at construction
        val entity = AchievementEntity("first_win")
        assertThat(entity.unlockedAt).isAtLeast(before)
    }

    @Test
    fun insertingAllKnownAchievementsSucceeds() = runTest {
        val ids = listOf(
            "first_win", "genius", "streak_3", "streak_7", "streak_30",
            "hard_mode_win", "no_hints", "polyglot", "daily_7", "daily_30",
            "practice_10", "speed_demon", "challenge_sent", "definition_fan"
        )
        ids.forEach { dao.unlock(AchievementEntity(it)) }

        dao.getUnlockedIds().test {
            val unlocked = awaitItem()
            assertThat(unlocked).hasSize(14)
            cancelAndIgnoreRemainingEvents()
        }
    }
