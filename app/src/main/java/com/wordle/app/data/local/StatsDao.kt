package com.wordle.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {
    @Insert
    suspend fun insert(stats: StatsEntity)

    @Query("SELECT * FROM stats WHERE languageCode = :lang AND difficulty = :diff ORDER BY timestamp DESC")
    fun getStats(lang: String, diff: String): Flow<List<StatsEntity>>

    @Query("SELECT COUNT(*) FROM stats WHERE languageCode = :lang AND difficulty = :diff AND won = 1")
    suspend fun getWinCount(lang: String, diff: String): Int

    @Query("SELECT COUNT(*) FROM stats WHERE languageCode = :lang AND difficulty = :diff")
    suspend fun getTotalGames(lang: String, diff: String): Int

    @Query("DELETE FROM stats")
    suspend fun clearAll()
}
