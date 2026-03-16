package com.wordle.app.data

import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepository @Inject constructor() {

    /** Encodes a word into a shareable challenge token (simple Base64, not security-sensitive). */
    fun buildChallengeLink(word: String): String {
        val token = Base64.encodeToString(word.uppercase().toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
        return "https://wordle.app/challenge?w=$token"
    }

    /** Decodes a challenge token back to a word. Returns null if invalid. */
    fun decodeChallengeToken(token: String): String? {
        return try {
            val bytes = Base64.decode(token, Base64.URL_SAFE or Base64.NO_WRAP)
            String(bytes).uppercase().takeIf { it.all { c -> c.isLetter() } && it.length in 4..8 }
        } catch (e: Exception) { null }
    }

    fun buildChallengeShareText(word: String): String {
        val link = buildChallengeLink(word)
        return "I challenge you to guess my word in Wordle! 🟩🟨⬛\n\nCan you beat me?\n$link"
    }
}
