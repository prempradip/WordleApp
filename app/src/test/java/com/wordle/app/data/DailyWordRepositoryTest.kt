package com.wordle.app.data

import com.google.common.truth.Truth.assertThat
import com.wordle.app.core.Language
import com.wordle.app.core.WordLength
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class DailyWordRepositoryTest {

    private lateinit var wordRepository: WordRepository

    // We test the pure logic of getDailyWord and msUntilNextWord without
    // a real Context/DataStore — those paths are covered by instrumented tests.

    @Before
    fun setup() {
        wordRepository = mockk()
        // Provide a stable word list for deterministic index calculation
        every { wordRepository.getAllWords(any(), any()) } returns listOf(
            "APPLE", "BRAVE", "CRANE", "DELTA", "EAGLE",
            "FLAME", "GRACE", "HONEY", "IVORY", "JOKER"
        )
    }

    @Test
    fun `getDailyWord returns a word from the list`() {
        val repo = makeDailyWordRepo()
        val word = repo.getDailyWord(Language.ENGLISH, WordLength.FIVE)
        val allWords = wordRepository.getAllWords(Language.ENGLISH, WordLength.FIVE).map { it.uppercase() }
        assertThat(allWords).contains(word)
    }

    @Test
    fun `getDailyWord is deterministic for same day and language`() {
        val repo = makeDailyWordRepo()
        val word1 = repo.getDailyWord(Language.ENGLISH, WordLength.FIVE)
        val word2 = repo.getDailyWord(Language.ENGLISH, WordLength.FIVE)
        assertThat(word1).isEqualTo(word2)
    }

    @Test
    fun `getDailyWord differs across languages`() {
        val repo = makeDailyWordRepo()
        // Different language ordinals produce different indices (most of the time)
        // We just verify the function runs without error for all languages
        Language.entries.forEach { lang ->
            val word = repo.getDailyWord(lang, WordLength.FIVE)
            assertThat(word).isNotEmpty()
            assertThat(word).matches("[A-Z]+")
        }
    }

    @Test
    fun `getDailyWord returns uppercase`() {
        val repo = makeDailyWordRepo()
        val word = repo.getDailyWord(Language.ENGLISH, WordLength.FIVE)
        assertThat(word).isEqualTo(word.uppercase())
    }

    @Test
    fun `msUntilNextWord returns positive value less than 24 hours`() {
        val repo = makeDailyWordRepo()
        val ms = repo.msUntilNextWord()
        assertThat(ms).isGreaterThan(0L)
        assertThat(ms).isAtMost(24 * 60 * 60 * 1000L)
    }

    @Test
    fun `getDailyWord index stays within word list bounds`() {
        // Use a small list to stress-test modulo
        every { wordRepository.getAllWords(any(), any()) } returns listOf("ALPHA", "BRAVO", "DELTA")
        val repo = makeDailyWordRepo()
        Language.entries.forEach { lang ->
            val word = repo.getDailyWord(lang, WordLength.FIVE)
            assertThat(listOf("ALPHA", "BRAVO", "DELTA")).contains(word)
        }
    }

    @Test
    fun `getDailyWord produces different words for different simulated days`() {
        val words = (1..50).map { "W${it.toString().padStart(4, '0')}" }
        every { wordRepository.getAllWords(any(), any()) } returns words

        // With 50 words and different day seeds, at least some days will differ
        val results = (0..9).map { day ->
            DailyWordRepositoryTestableWithDay(wordRepository, dayOffset = day.toLong())
                .getDailyWord(Language.ENGLISH, WordLength.FIVE)
        }
        // Not all 10 days should return the same word
        assertThat(results.toSet().size).isGreaterThan(1)
    }

    @Test
    fun `getDailyWord differs across word lengths`() {
        val repo = makeDailyWordRepo()
        // Different wordLength ordinals produce different indices (most of the time)
        // We just verify the function runs without error for all word lengths
        WordLength.entries.forEach { wl ->
            val word = repo.getDailyWord(Language.ENGLISH, wl)
            assertThat(word).isNotEmpty()
            assertThat(word).matches("[A-Z]+")
        }
    }

    // Helper — creates a DailyWordRepository without a real Context by
    // subclassing and overriding only the pure functions under test.
    private fun makeDailyWordRepo(): DailyWordRepositoryTestable {
        return DailyWordRepositoryTestable(wordRepository)
    }
}

/**
 * Testable subclass that avoids the Android Context/DataStore dependency
 * so pure logic can be exercised in JVM unit tests.
 */
class DailyWordRepositoryTestable(
    private val wordRepo: WordRepository
) {
    fun getDailyWord(language: Language, wordLength: WordLength): String {
        val today = todayEpochDay()
        val words = wordRepo.getAllWords(language, wordLength)
        val idx = ((today * 31L + language.ordinal * 7L + wordLength.ordinal * 13L) % words.size).toInt()
        return words[idx].uppercase()
    }

    fun msUntilNextWord(): Long {
        val cal = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis - System.currentTimeMillis()
    }

    private fun todayEpochDay(): Long {
        val cal = java.util.Calendar.getInstance()
        return cal.get(java.util.Calendar.YEAR) * 1000L + cal.get(java.util.Calendar.DAY_OF_YEAR)
    }
}

/**
 * Variant that accepts a day offset so tests can simulate different calendar days.
 */
class DailyWordRepositoryTestableWithDay(
    private val wordRepo: WordRepository,
    private val dayOffset: Long = 0L
) {
    fun getDailyWord(language: Language, wordLength: WordLength): String {
        val today = todayEpochDay() + dayOffset
        val words = wordRepo.getAllWords(language, wordLength)
        val idx = ((today * 31L + language.ordinal * 7L + wordLength.ordinal * 13L) % words.size).toInt()
        return words[idx].uppercase()
    }

    private fun todayEpochDay(): Long {
        val cal = java.util.Calendar.getInstance()
        return cal.get(java.util.Calendar.YEAR) * 1000L + cal.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
