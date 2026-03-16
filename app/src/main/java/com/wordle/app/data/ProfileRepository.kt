package com.wordle.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.profileDataStore by preferencesDataStore(name = "profile")

enum class AppTheme(val displayName: String) {
    CLASSIC("Classic"),
    AMOLED("Amoled Black"),
    SOLARIZED("Solarized"),
    PASTEL("Pastel"),
    OCEAN("Ocean")
}

@Singleton
class ProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_USERNAME   = stringPreferencesKey("username")
        val KEY_AVATAR_IDX = intPreferencesKey("avatar_idx")
        val KEY_APP_THEME  = stringPreferencesKey("app_theme")
        val KEY_FONT_SIZE  = intPreferencesKey("font_size")   // 0=small,1=normal,2=large
        val KEY_TILE_SIZE  = intPreferencesKey("tile_size")   // 0=compact,1=normal,2=large
        val KEY_SOUND_ON   = androidx.datastore.preferences.core.booleanPreferencesKey("sound_on")
        val KEY_HAPTIC_LEVEL = intPreferencesKey("haptic_level") // 0=off,1=light,2=strong
        val KEY_DEF_VIEWS  = intPreferencesKey("definition_views")
    }

    val username: Flow<String>   = context.profileDataStore.data.map { it[KEY_USERNAME] ?: "Wordler" }
    val avatarIdx: Flow<Int>     = context.profileDataStore.data.map { it[KEY_AVATAR_IDX] ?: 0 }
    val appTheme: Flow<AppTheme> = context.profileDataStore.data.map {
        AppTheme.entries.find { t -> t.name == it[KEY_APP_THEME] } ?: AppTheme.CLASSIC
    }
    val fontSize: Flow<Int>      = context.profileDataStore.data.map { it[KEY_FONT_SIZE] ?: 1 }
    val tileSize: Flow<Int>      = context.profileDataStore.data.map { it[KEY_TILE_SIZE] ?: 1 }
    val soundOn: Flow<Boolean>   = context.profileDataStore.data.map { it[KEY_SOUND_ON] ?: true }
    val hapticLevel: Flow<Int>   = context.profileDataStore.data.map { it[KEY_HAPTIC_LEVEL] ?: 1 }
    val definitionViews: Flow<Int> = context.profileDataStore.data.map { it[KEY_DEF_VIEWS] ?: 0 }

    suspend fun setUsername(name: String) = context.profileDataStore.edit { it[KEY_USERNAME] = name }
    suspend fun setAvatarIdx(idx: Int)    = context.profileDataStore.edit { it[KEY_AVATAR_IDX] = idx }
    suspend fun setAppTheme(theme: AppTheme) = context.profileDataStore.edit { it[KEY_APP_THEME] = theme.name }
    suspend fun setFontSize(size: Int)    = context.profileDataStore.edit { it[KEY_FONT_SIZE] = size }
    suspend fun setTileSize(size: Int)    = context.profileDataStore.edit { it[KEY_TILE_SIZE] = size }
    suspend fun setSoundOn(on: Boolean)   = context.profileDataStore.edit { it[KEY_SOUND_ON] = on }
    suspend fun setHapticLevel(level: Int) = context.profileDataStore.edit { it[KEY_HAPTIC_LEVEL] = level }
    suspend fun incrementDefinitionViews() = context.profileDataStore.edit {
        it[KEY_DEF_VIEWS] = (it[KEY_DEF_VIEWS] ?: 0) + 1
    }
}
