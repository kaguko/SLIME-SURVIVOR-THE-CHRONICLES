package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val gold: Int = 0,
    val selectedHeroId: String = "knight",
    val unlockedHeroes: String = "knight",
    val selectedStageId: String = "forest",
    val unlockedStages: String = "forest",
    // Permanent Meta Upgrades
    val ironBodyRank: Int = 0,
    val swiftStepsRank: Int = 0,
    val mightyStrikeRank: Int = 0,
    val greedRuneRank: Int = 0,
    val magnetPullRank: Int = 0,
    val phoenixFeatherRank: Int = 0
)
