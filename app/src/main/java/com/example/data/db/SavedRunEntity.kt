package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_run")
data class SavedRunEntity(
    @PrimaryKey val id: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val heroId: String,
    val stageId: String,
    val playerHp: Float,
    val playerMaxHp: Float,
    val playerLevel: Int,
    val currentXp: Int,
    val xpNeeded: Int,
    val timeRemainingSeconds: Float,
    val totalTimeSurvived: Float,
    val killCount: Int,
    val goldCollected: Int,
    val score: Int,
    val skillLevelsJson: String,
    val canRevive: Boolean = true
)
