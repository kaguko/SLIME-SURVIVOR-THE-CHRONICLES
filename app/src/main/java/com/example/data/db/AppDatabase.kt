package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RunRecordEntity::class,
        UserProfileEntity::class,
        AchievementEntity::class,
        SavedRunEntity::class,
        PlaytestFeedbackEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runRecordDao(): RunRecordDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun achievementDao(): AchievementDao
    abstract fun savedRunDao(): SavedRunDao
    abstract fun playtestFeedbackDao(): PlaytestFeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "slime_survivor.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
