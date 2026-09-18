package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val achievementId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllUnlockedAchievementsSync(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockAchievement(entity: AchievementEntity): Long

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getUnlockedCount(): Int
}
