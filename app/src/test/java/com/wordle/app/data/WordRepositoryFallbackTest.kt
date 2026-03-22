package com.wordle.app.data

import android.content.Context
import android.content.res.AssetManager
import com.google.common.truth.Truth.assertThat
import com.wordle.app.core.Language
import com.wordle.app.core.WordLength
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * Smoke test for WordRepository fallback path:
 * when a length-specific asset file is absent, the base file is filtered by length.
 */
class WordRepositoryFallbackTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var repository: WordRepository

    // Base word list contains words of mixed lengths
    private val baseWords = listOf(
        "FOUR",       // 4 letters
        "APPLE",      // 5 letters
        "BRAVE",      // 5 letters
        "ORANGE",     // 6 letters
        "FREEDOM",    // 7 letters
        "ABCDEFG",    // 7 letters
    ).joinToString("\n")

    @Before
    fun setup() {
        assetManager = mockk()
        context = mockk()
        every { context.assets } returns assetManager
        repository = WordRepository(context)
    }

    @Test
    fun `falls back to base file and filters by length when specific file absent`() {
        // Specific file (words_en_5.txt) throws → fallback to words_en.txt
        every { assetManager.open("words_en_5.txt") } throws Exception("not found")
        every { assetManager.open("words_en.txt") } returns ByteArrayInputStream(baseWords.toByteArray())

        val words = repository.loadWordSet(Language.ENGLISH, WordLength.FIVE)

        assertThat(words).containsExactly("APPLE", "BRAVE")
    }

    @Test
    fun `returns empty set when both specific and base files are absent`() {
        every { assetManager.open(any()) } throws Exception("not found")

        val words = repository.loadWordSet(Language.ENGLISH, WordLength.SIX)

        assertThat(words).isEmpty()
    }

    @Test
    fun `specific file takes priority over base file`() {
        val specificWords = "CRANE\nGRACE\nSLATE\n"
        every { assetManager.open("words_en_5.txt") } returns ByteArrayInputStream(specificWords.toByteArray())

        val words = repository.loadWordSet(Language.ENGLISH, WordLength.FIVE)

        assertThat(words).containsExactly("CRANE", "GRACE", "SLATE")
    }

    @Test
    fun `all returned words have exactly the requested length`() {
        every { assetManager.open("words_en_4.txt") } throws Exception("not found")
        every { assetManager.open("words_en.txt") } returns ByteArrayInputStream(baseWords.toByteArray())

        val words = repository.loadWordSet(Language.ENGLISH, WordLength.FOUR)

        assertThat(words).isNotEmpty()
        words.forEach { word ->
            assertThat(word.length).isEqualTo(WordLength.FOUR.value)
        }
    }

    @Test
    fun `cache key is per language and length — different lengths return different sets`() {
        every { assetManager.open("words_en_5.txt") } throws Exception("not found")
        every { assetManager.open("words_en_6.txt") } throws Exception("not found")
        every { assetManager.open("words_en.txt") } returns ByteArrayInputStream(baseWords.toByteArray())

        val fiveLetterWords = repository.loadWordSet(Language.ENGLISH, WordLength.FIVE)
        // Re-open for the second call (cache miss for different key)
        every { assetManager.open("words_en.txt") } returns ByteArrayInputStream(baseWords.toByteArray())
        val sixLetterWords = repository.loadWordSet(Language.ENGLISH, WordLength.SIX)

        assertThat(fiveLetterWords).containsExactly("APPLE", "BRAVE")
        assertThat(sixLetterWords).containsExactly("ORANGE")
    }
}
