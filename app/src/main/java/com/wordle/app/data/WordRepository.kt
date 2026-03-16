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
    private val wordCache = mutableMapOf<String, Set<String>>()

    fun getRandomWord(language: Language): String {
        val words = loadWords(language)
        return words.random().uppercase()
    }

    fun isValidWord(word: String, language: Language): Boolean {
        return loadWordSet(language).contains(word.uppercase())
    }

    fun getAllWords(language: Language): List<String> = loadWords(language)

    private fun loadWords(language: Language): List<String> {
        return loadWordSet(language).toList()
    }

    private fun loadWordSet(language: Language): Set<String> {
        return wordCache.getOrPut(language.code) {
            try {
                val fileName = "words_${language.code}.txt"
                context.assets.open(fileName)
                    .bufferedReader()
                    .readLines()
                    .filter { it.length == language.wordLength && it.all { c -> c.isLetter() } }
                    .map { it.uppercase() }
                    .toHashSet()
            } catch (e: Exception) {
                emptySet()
            }
        }
    }
}
