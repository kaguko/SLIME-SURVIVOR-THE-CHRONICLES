package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_records")
data class RunRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val survivalSeconds: Int,
    val kills: Int,
    val levelReached: Int,
    val isVictory: Boolean,
    val selectedSkills: String, // comma separated or descriptions
    val score: Int,
    val chronicleStory: String? = null
)
