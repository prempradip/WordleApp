package com.wordle.app.di

import android.content.Context
import androidx.room.Room
import com.wordle.app.data.local.AchievementDao
import com.wordle.app.data.local.AppDatabase
import com.wordle.app.data.local.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wordle_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideStatsDao(db: AppDatabase): StatsDao = db.statsDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
}
