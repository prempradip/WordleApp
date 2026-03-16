package com.wordle.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(achievement: AchievementEntity): Long  // returns -1 if already exists

    @Query("SELECT id FROM achievements")
    fun getUnlockedIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM achievements WHERE id = :id")
    suspend fun isUnlocked(id: String): Int
}
