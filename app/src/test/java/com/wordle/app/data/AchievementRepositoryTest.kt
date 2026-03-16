package com.wordle.app.data

import com.google.common.truth.Truth.assertThat
import com.wordle.app.data.local.AchievementDao
import com.wordle.app.data.local.AchievementEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AchievementRepositoryTest {

    private lateinit var dao: AchievementDao
    private lateinit var repo: AchievementRepository

    @Before
    fun setup() {
        dao = mockk()
        repo = AchievementRepository(dao)
    }

    @Test
    fun `tryUnlock returns achievement when newly unlocked`() = runTest {
        coEvery { dao.unlock(any()) } returns 1L

        val result = repo.tryUnlock("first_win")

        assertThat(result).isNotNull()
        assertThat(result!!.id).isEqualTo("first_win")
        assertThat(result.title).isEqualTo("First Blood")
    }

    @Test
    fun `tryUnlock returns null when already unlocked`() = runTest {
        coEvery { dao.unlock(any()) } returns -1L

        val result = repo.tryUnlock("first_win")

        assertThat(result).isNull()
    }

    @Test
    fun `tryUnlock returns null for unknown achievement id`() = runTest {
        coEvery { dao.unlock(any()) } returns 1L

        val result = repo.tryUnlock("nonexistent_achievement")

        assertThat(result).isNull()
    }

    @Test
    fun `tryUnlock calls dao with correct entity`() = runTest {
        coEvery { dao.unlock(any()) } returns 1L

        repo.tryUnlock("genius")

        coVerify { dao.unlock(AchievementEntity("genius")) }
    }

    @Test
    fun `isUnlocked returns true when dao count is positive`() = runTest {
        coEvery { dao.isUnlocked("streak_3") } returns 1

        val result = repo.isUnlocked("streak_3")

        assertThat(result).isTrue()
    }

    @Test
    fun `isUnlocked returns false when dao count is zero`() = runTest {
        coEvery { dao.isUnlocked("streak_3") } returns 0

        val result = repo.isUnlocked("streak_3")

        assertThat(result).isFalse()
    }

    @Test
    fun `ALL list contains 14 achievements`() {
        assertThat(AchievementRepository.ALL).hasSize(14)
    }

    @Test
    fun `ALL achievement ids are unique`() {
        val ids = AchievementRepository.ALL.map { it.id }
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `ALL achievements have non-empty titles and descriptions`() {
        AchievementRepository.ALL.forEach { achievement ->
            assertThat(achievement.title).isNotEmpty()
            assertThat(achievement.description).isNotEmpty()
            assertThat(achievement.icon).isNotEmpty()
        }
    }
}
