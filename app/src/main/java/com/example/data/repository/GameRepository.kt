package com.example.data.repository

import com.example.data.ai.GeminiAiService
import com.example.data.db.*
import com.example.game.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class GameRepository(
    private val database: AppDatabase,
    private val aiService: GeminiAiService
) {
    val allRuns: Flow<List<RunRecordEntity>> = database.runRecordDao().getAllRuns()
    val topScores: Flow<List<RunRecordEntity>> = database.runRecordDao().getTopScores()
    val userProfile: Flow<UserProfileEntity?> = database.userProfileDao().getUserProfile()
    val unlockedAchievements: Flow<List<AchievementEntity>> = database.achievementDao().getAllUnlockedAchievements()
    val savedRunFlow: Flow<SavedRunEntity?> = database.savedRunDao().getSavedRunFlow()
    val playtestFeedbackList: Flow<List<PlaytestFeedbackEntity>> = database.playtestFeedbackDao().getAllFeedback()

    suspend fun saveActiveRun(run: SavedRunEntity) {
        database.savedRunDao().saveRun(run)
    }

    suspend fun getSavedActiveRun(): SavedRunEntity? {
        return database.savedRunDao().getSavedRun()
    }

    suspend fun clearSavedActiveRun() {
        database.savedRunDao().clearSavedRun()
    }

    suspend fun submitPlaytestFeedback(
        testerName: String,
        category: String,
        rating: Int,
        comments: String
    ): Long {
        val feedback = PlaytestFeedbackEntity(
            testerName = testerName.ifBlank { "Anh Hùng Slime" },
            category = category,
            rating = rating,
            comments = comments,
            aiInsight = "Cảm ơn đóng góp của bạn! Đội ngũ phát triển đã ghi nhận dữ liệu cân bằng cho phiên bản tiếp theo."
        )
        return database.playtestFeedbackDao().insertFeedback(feedback)
    }

    suspend fun getOrCreateProfile(): UserProfileEntity {
        val existing = database.userProfileDao().getUserProfileSync()
        if (existing != null) return existing
        val defaultProfile = UserProfileEntity()
        database.userProfileDao().saveUserProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun saveRun(
        survivalSeconds: Int,
        kills: Int,
        levelReached: Int,
        isVictory: Boolean,
        skills: Map<SkillId, Int>,
        score: Int,
        goldEarned: Int,
        stageId: String
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
            chronicleStory = story,
            stageId = stageId
        )
        
        database.userProfileDao().addGold(goldEarned)
        val runId = database.runRecordDao().insertRun(record)

        // Evaluate and unlock achievements
        checkRunAchievements(survivalSeconds, kills, isVictory, skills, stageId)

        return runId
    }

    private suspend fun checkRunAchievements(
        survivalSec: Int,
        kills: Int,
        isVictory: Boolean,
        skills: Map<SkillId, Int>,
        stageId: String
    ) {
        val totalKills = (database.runRecordDao().getTotalKills() ?: 0)
        val profile = getOrCreateProfile()

        if (totalKills >= 50) tryUnlockAchievement(AchievementId.FIRST_BLOOD)
        if (totalKills >= 500) tryUnlockAchievement(AchievementId.MONSTER_SLAYER)
        if (totalKills >= 2000) tryUnlockAchievement(AchievementId.GENOCIDE)

        if (survivalSec >= 120) tryUnlockAchievement(AchievementId.SURVIVOR_NOVICE)
        if (survivalSec >= 300 || isVictory) tryUnlockAchievement(AchievementId.SURVIVAL_MASTER)

        if (isVictory) {
            when (stageId) {
                GameStage.ENCHANTED_FOREST.id -> tryUnlockAchievement(AchievementId.TREE_ENT_SLAYER)
                GameStage.MAGMA_CORE.id -> tryUnlockAchievement(AchievementId.DRAGON_SLAYER)
                GameStage.GLACIAL_FROST.id -> tryUnlockAchievement(AchievementId.LICH_SLAYER)
                GameStage.GOLDEN_TOMB.id -> tryUnlockAchievement(AchievementId.PHARAOH_SLAYER)
            }
        }

        if (skills.containsKey(SkillId.THUNDER_WRATH) || skills.containsKey(SkillId.SOLAR_SUPERNOVA)) {
            tryUnlockAchievement(AchievementId.EVOLUTION_MASTER)
        }

        if (profile.gold >= 1000) {
            tryUnlockAchievement(AchievementId.TREASURY_HOARDER)
        }

        val unlockedHeroesCount = profile.unlockedHeroes.split(",").size
        if (unlockedHeroesCount >= 4) {
            tryUnlockAchievement(AchievementId.HERO_COLLECTOR)
        }
    }

    suspend fun tryUnlockAchievement(achievement: AchievementId): Boolean {
        val existing = database.achievementDao().getAllUnlockedAchievementsSync()
        if (existing.any { it.achievementId == achievement.name }) return false

        database.achievementDao().unlockAchievement(AchievementEntity(achievement.name))
        database.userProfileDao().addGold(achievement.goldReward)
        return true
    }

    suspend fun unlockStage(stage: GameStage): Boolean {
        val profile = getOrCreateProfile()
        if (profile.gold < stage.unlockGoldCost) return false
        val currentUnlocked = profile.unlockedStages.split(",").map { it.trim() }.toMutableSet()
        currentUnlocked.add(stage.id)

        val updated = profile.copy(
            gold = profile.gold - stage.unlockGoldCost,
            unlockedStages = currentUnlocked.joinToString(","),
            selectedStageId = stage.id
        )
        database.userProfileDao().saveUserProfile(updated)
        return true
    }

    suspend fun selectStage(stageId: String) {
        val profile = getOrCreateProfile()
        database.userProfileDao().saveUserProfile(profile.copy(selectedStageId = stageId))
    }

    suspend fun purchaseUpgrade(upgradeType: MetaUpgradeType): Boolean {
        val profile = getOrCreateProfile()
        val currentRank = when (upgradeType) {
            MetaUpgradeType.IRON_BODY -> profile.ironBodyRank
            MetaUpgradeType.SWIFT_STEPS -> profile.swiftStepsRank
            MetaUpgradeType.MIGHTY_STRIKE -> profile.mightyStrikeRank
            MetaUpgradeType.GREED_RUNE -> profile.greedRuneRank
            MetaUpgradeType.MAGNET_PULL -> profile.magnetPullRank
            MetaUpgradeType.PHOENIX_FEATHER -> profile.phoenixFeatherRank
        }

        if (currentRank >= upgradeType.maxRank) return false
        val cost = (upgradeType.baseCost * Math.pow(upgradeType.costMultiplier.toDouble(), currentRank.toDouble())).toInt()
        if (profile.gold < cost) return false

        val updated = when (upgradeType) {
            MetaUpgradeType.IRON_BODY -> profile.copy(gold = profile.gold - cost, ironBodyRank = currentRank + 1)
            MetaUpgradeType.SWIFT_STEPS -> profile.copy(gold = profile.gold - cost, swiftStepsRank = currentRank + 1)
            MetaUpgradeType.MIGHTY_STRIKE -> profile.copy(gold = profile.gold - cost, mightyStrikeRank = currentRank + 1)
            MetaUpgradeType.GREED_RUNE -> profile.copy(gold = profile.gold - cost, greedRuneRank = currentRank + 1)
            MetaUpgradeType.MAGNET_PULL -> profile.copy(gold = profile.gold - cost, magnetPullRank = currentRank + 1)
            MetaUpgradeType.PHOENIX_FEATHER -> profile.copy(gold = profile.gold - cost, phoenixFeatherRank = currentRank + 1)
        }
        database.userProfileDao().saveUserProfile(updated)
        return true
    }

    suspend fun unlockHero(hero: SlimeHero): Boolean {
        val profile = getOrCreateProfile()
        if (profile.gold < hero.unlockGoldCost) return false
        val currentUnlocked = profile.unlockedHeroes.split(",").map { it.trim() }.toMutableSet()
        currentUnlocked.add(hero.id)

        val updated = profile.copy(
            gold = profile.gold - hero.unlockGoldCost,
            unlockedHeroes = currentUnlocked.joinToString(","),
            selectedHeroId = hero.id
        )
        database.userProfileDao().saveUserProfile(updated)
        return true
    }

    suspend fun selectHero(heroId: String) {
        val profile = getOrCreateProfile()
        database.userProfileDao().saveUserProfile(profile.copy(selectedHeroId = heroId))
    }

    suspend fun addGoldBonus(amount: Int) {
        database.userProfileDao().addGold(amount)
    }

    suspend fun getSageAdvice(currentLevel: Int, timeSec: Int, skills: Map<SkillId, Int>): String {
        return aiService.getSageStrategyAdvice(currentLevel, timeSec, skills)
    }

    suspend fun getBestSurvivalTime(): Int = database.runRecordDao().getBestSurvivalTime() ?: 0
    suspend fun getTotalKills(): Int = database.runRecordDao().getTotalKills() ?: 0
    suspend fun getTotalRuns(): Int = database.runRecordDao().getTotalRunsCount()
}
