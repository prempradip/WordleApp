package com.wordle.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class WordDefinition(
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String?
)

@Singleton
class DefinitionRepository @Inject constructor() {

    private val cache = mutableMapOf<String, Result<WordDefinition>>()

    suspend fun getDefinition(word: String): Result<WordDefinition> {
        cache[word.lowercase()]?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.dictionaryapi.dev/api/v2/entries/en/${word.lowercase()}"
                val json = URL(url).readText()
                val arr = JSONArray(json)
                val entry = arr.getJSONObject(0)
                val phonetic = entry.optString("phonetic", "")
                val meanings = entry.getJSONArray("meanings")
                val meaning = meanings.getJSONObject(0)
                val pos = meaning.optString("partOfSpeech", "")
                val defs = meaning.getJSONArray("definitions")
                val defObj = defs.getJSONObject(0)
                val def = defObj.optString("definition", "No definition found")
                val example = defObj.optString("example", "").takeIf { it.isNotBlank() }
                val result = Result.success(WordDefinition(word, phonetic, pos, def, example))
                cache[word.lowercase()] = result
                result
            } catch (e: Exception) {
                val result = Result.failure<WordDefinition>(e)
                result
            }
        }
    }
}
