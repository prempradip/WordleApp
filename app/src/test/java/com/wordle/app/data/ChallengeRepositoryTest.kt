package com.wordle.app.data

import android.util.Base64
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test

class ChallengeRepositoryTest {

    private lateinit var repo: ChallengeRepository

    @Before
    fun setup() {
        // Mock Android's Base64 to use Java's implementation in unit tests
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(firstArg<ByteArray>())
        }
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getUrlDecoder().decode(firstArg<String>())
        }
        repo = ChallengeRepository()
    }

    @Test
    fun `buildChallengeLink contains base url and query param`() {
        val link = repo.buildChallengeLink("PLANT")
        assertThat(link).startsWith("https://wordle.app/challenge?w=")
    }

    @Test
    fun `encode then decode round-trip returns original word uppercase`() {
        val original = "plant"
        val link = repo.buildChallengeLink(original)
        val token = link.substringAfter("?w=")
        val decoded = repo.decodeChallengeToken(token)
        assertThat(decoded).isEqualTo("PLANT")
    }

    @Test
    fun `decode returns null for garbage input`() {
        val result = repo.decodeChallengeToken("!!!not_valid_base64!!!")
        assertThat(result).isNull()
    }

    @Test
    fun `decode returns null for token that decodes to non-letters`() {
        // Encode a string with digits
        val token = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("WORD1".toByteArray())
        val result = repo.decodeChallengeToken(token)
        assertThat(result).isNull()
    }

    @Test
    fun `decode returns null for word shorter than 4 chars`() {
        val token = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("HI".toByteArray())
        val result = repo.decodeChallengeToken(token)
        assertThat(result).isNull()
    }

    @Test
    fun `decode returns null for word longer than 8 chars`() {
        val token = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("ABCDEFGHI".toByteArray())
        val result = repo.decodeChallengeToken(token)
        assertThat(result).isNull()
    }

    @Test
    fun `buildChallengeShareText contains the link and emoji`() {
        val text = repo.buildChallengeShareText("PLANT")
        assertThat(text).contains("https://wordle.app/challenge?w=")
        assertThat(text).contains("🟩")
    }

    @Test
    fun `different words produce different tokens`() {
        val link1 = repo.buildChallengeLink("PLANT")
        val link2 = repo.buildChallengeLink("CRANE")
        assertThat(link1).isNotEqualTo(link2)
    }

    @Test
    fun `decode accepts exactly 4-char word`() {
        val token = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("WORD".toByteArray())
        val result = repo.decodeChallengeToken(token)
        assertThat(result).isEqualTo("WORD")
    }

    @Test
    fun `decode accepts exactly 8-char word`() {
        val token = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("ABCDEFGH".toByteArray())
        val result = repo.decodeChallengeToken(token)
        assertThat(result).isEqualTo("ABCDEFGH")
    }

    @Test
    fun `buildChallengeLink uppercases the word before encoding`() {
        val linkLower = repo.buildChallengeLink("plant")
        val linkUpper = repo.buildChallengeLink("PLANT")
        assertThat(linkLower).isEqualTo(linkUpper)
    }
}
