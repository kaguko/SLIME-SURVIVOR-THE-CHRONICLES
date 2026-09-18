package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RunRecordDao {
    @Query("SELECT * FROM run_records ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<RunRecordEntity>>

    @Query("SELECT * FROM run_records ORDER BY score DESC LIMIT 10")
    fun getTopScores(): Flow<List<RunRecordEntity>>

    @Query("SELECT * FROM run_records WHERE stageId = :stageId ORDER BY score DESC LIMIT 10")
    fun getTopScoresByStage(stageId: String): Flow<List<RunRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(record: RunRecordEntity): Long

    @Query("SELECT MAX(survivalSeconds) FROM run_records")
    suspend fun getBestSurvivalTime(): Int?

    @Query("SELECT SUM(kills) FROM run_records")
    suspend fun getTotalKills(): Int?

    @Query("SELECT COUNT(*) FROM run_records")
    suspend fun getTotalRunsCount(): Int

    @Query("UPDATE run_records SET chronicleStory = :story WHERE id = :runId")
    suspend fun updateChronicleStory(runId: Long, story: String)
}
