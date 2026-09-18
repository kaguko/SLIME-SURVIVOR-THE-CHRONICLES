package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playtest_feedback")
data class PlaytestFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val testerName: String,
    val category: String, // "Gameplay Balance", "Performance & Battery", "Controls & Feel", "Bug Report", "Feature Idea"
    val rating: Int, // 1..5 stars
    val comments: String,
    val aiInsight: String? = null
)

@Dao
interface PlaytestFeedbackDao {
    @Query("SELECT * FROM playtest_feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<PlaytestFeedbackEntity>>

    @Insert
    suspend fun insertFeedback(feedback: PlaytestFeedbackEntity): Long

    @Query("SELECT COUNT(*) FROM playtest_feedback")
    suspend fun getFeedbackCount(): Int
}
