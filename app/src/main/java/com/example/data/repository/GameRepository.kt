package com.example.data.repository

import com.example.data.ai.GeminiAiService
import com.example.data.db.AppDatabase
import com.example.data.db.RunRecordEntity
import com.example.game.model.SkillId
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val database: AppDatabase,
    private val aiService: GeminiAiService
) {
    val allRuns: Flow<List<RunRecordEntity>> = database.runRecordDao().getAllRuns()
    val topScores: Flow<List<RunRecordEntity>> = database.runRecordDao().getTopScores()

    suspend fun saveRun(
        survivalSeconds: Int,
        kills: Int,
        levelReached: Int,
        isVictory: Boolean,
        skills: Map<SkillId, Int>,
        score: Int
    ): Long {
        val skillsString = skills.entries.joinToString(", ") { "${it.key.name}: Lv.${it.value}" }
        val story = aiService.generateChronicleStory(survivalSeconds, levelReached, kills, isVictory, skills)
        
        val record = RunRecordEntity(
            survivalSeconds = survivalSeconds,
            kills = kills,
            levelReached = levelReached,
            isVictory = isVictory,
            selectedSkills = skillsString,
            score = score,
            chronicleStory = story
        )
        return database.runRecordDao().insertRun(record)
    }

    suspend fun getSageAdvice(currentLevel: Int, timeSec: Int, skills: Map<SkillId, Int>): String {
        return aiService.getSageStrategyAdvice(currentLevel, timeSec, skills)
    }

    suspend fun getBestSurvivalTime(): Int = database.runRecordDao().getBestSurvivalTime() ?: 0
    suspend fun getTotalKills(): Int = database.runRecordDao().getTotalKills() ?: 0
    suspend fun getTotalRuns(): Int = database.runRecordDao().getTotalRunsCount()
}
