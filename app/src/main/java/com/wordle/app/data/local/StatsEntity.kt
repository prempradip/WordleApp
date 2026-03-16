package com.wordle.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stats")
data class StatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val languageCode: String,
    val difficulty: String,
    val won: Boolean,
    val attemptsUsed: Int,
    val timestamp: Long = System.currentTimeMillis()
)
