package com.wordle.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StatsEntity::class, AchievementEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statsDao(): StatsDao
    abstract fun achievementDao(): AchievementDao
}
