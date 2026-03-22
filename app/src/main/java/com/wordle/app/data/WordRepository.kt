package com.wordle.app.data

import android.content.Context
import com.wordle.app.core.Language
import com.wordle.app.core.WordLength
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wordCache = mutableMapOf<String, Set<String>>()

    fun getRandomWord(language: Language, wordLength: WordLength): String {
        val words = loadWordSet(language, wordLength)
        return words.random().uppercase()
    }

    fun isValidWord(word: String, language: Language, wordLength: WordLength): Boolean {
        return loadWordSet(language, wordLength).contains(word.uppercase())
    }

    fun getAllWords(language: Language, wordLength: WordLength): List<String> =
        loadWordSet(language, wordLength).toList()

    fun loadWordSet(language: Language, wordLength: WordLength): Set<String> {
        val cacheKey = "${language.code}_${wordLength.value}"
        return wordCache.getOrPut(cacheKey) {
            val specificFile = "words_${language.code}_${wordLength.value}.txt"
            val baseFile = "words_${language.code}.txt"

            // Try length-specific file first
            val lines = tryReadAsset(specificFile)
                ?: tryReadAsset(baseFile)
                ?: return@getOrPut emptySet()

            lines
                .filter { it.length == wordLength.value && it.all { c -> c.isLetter() } }
                .map { it.uppercase() }
                .toHashSet()
        }
    }

    private fun tryReadAsset(fileName: String): List<String>? {
        return try {
            context.assets.open(fileName).bufferedReader().readLines()
        } catch (e: Exception) {
            null
        }
    }
}
