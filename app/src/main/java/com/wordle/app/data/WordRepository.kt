package com.wordle.app.data

import android.content.Context
import com.wordle.app.core.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wordCache = mutableMapOf<String, List<String>>()

    fun getRandomWord(language: Language): String {
        val words = loadWords(language)
        return words.random().uppercase()
    }

    fun isValidWord(word: String, language: Language): Boolean {
        return loadWords(language).any { it.equals(word, ignoreCase = true) }
    }

    fun getAllWords(language: Language): List<String> = loadWords(language)

    private fun loadWords(language: Language): List<String> {
        return wordCache.getOrPut(language.code) {
            val fileName = "words_${language.code}.txt"
            context.assets.open(fileName)
                .bufferedReader()
                .readLines()
                .filter { it.length == language.wordLength && it.all { c -> c.isLetter() } }
        }
    }
}
