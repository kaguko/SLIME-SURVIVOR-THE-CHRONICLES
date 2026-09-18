package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.db.*
import com.example.data.model.*
import com.example.data.repository.GameRepository
import com.example.game.audio.SoundFxSynth
import com.example.game.engine.GameEngine
import com.example.game.engine.GameState
import com.example.game.model.*
import com.example.util.AppLanguage
import com.example.util.Localization
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN_MENU,
    PLAYING,
    STAGE_SELECT,
    HERO_SELECT,
    META_SHOP,
    STORE_MONETIZATION,
    LEADERBOARD,
    ACHIEVEMENTS,
    SAGE_SANCTUARY,
    SKIN_STUDIO,
    PRIVACY_POLICY,
    SETTINGS,
    PLAYTEST_HUB
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.MAIN_MENU,
    val isSageAdviceOpen: Boolean = false,
    val sageAdviceText: String = "",
    val isSageLoading: Boolean = false,
    val lastSavedStory: String? = null,
    val bestSurvivalSeconds: Int = 0,
    val totalKills: Int = 0,
    val totalRuns: Int = 0,
    val soundMuted: Boolean = false,
    val bgmMuted: Boolean = false,
    val selectedHero: SlimeHero = SlimeHero.KNIGHT_SLIME,
    val selectedStage: GameStage = GameStage.ENCHANTED_FOREST,
    val userProfile: UserProfileEntity = UserProfileEntity(),
    val unlockedAchievementIds: Set<String> = emptySet(),
    val currentLanguage: AppLanguage = AppLanguage.VIETNAMESE,
    val gameSettings: GameSettings = GameSettings(),
    val activeSavedRun: SavedRunEntity? = null,
    val playtestFeedbackList: List<PlaytestFeedbackEntity> = emptyList(),
    val statusToastMessage: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val soundFx = SoundFxSynth()
    val gameEngine = GameEngine(soundFx)

    private val db = AppDatabase.getInstance(application)
    private val aiService = GeminiAiService()
    val repository = GameRepository(db, aiService)

    val gameState: StateFlow<GameState> = gameEngine.state

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val runHistory: StateFlow<List<RunRecordEntity>> = repository.allRuns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topRuns: StateFlow<List<RunRecordEntity>> = repository.topScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var gameLoopJob: Job? = null
    private var isRunSaved = false

    init {
        loadStats()
        observeProfile()
        observeAchievements()
        observeSavedRun()
        observePlaytestFeedback()
        soundFx.startBgm(com.example.game.audio.BgmTrack.MENU)
    }

    private fun observeProfile() {
        viewModelScope.launch {
            repository.userProfile.filterNotNull().collect { profile ->
                val hero = SlimeHero.values().find { it.id == profile.selectedHeroId } ?: SlimeHero.KNIGHT_SLIME
                val stage = GameStage.values().find { it.id == profile.selectedStageId } ?: GameStage.ENCHANTED_FOREST
                _uiState.update {
                    it.copy(
                        userProfile = profile,
                        selectedHero = hero,
                        selectedStage = stage
                    )
                }
            }
        }
    }

    private fun observeAchievements() {
        viewModelScope.launch {
            repository.unlockedAchievements.collect { list ->
                val ids = list.map { it.achievementId }.toSet()
                _uiState.update { it.copy(unlockedAchievementIds = ids) }
            }
        }
    }

    private fun observeSavedRun() {
        viewModelScope.launch {
            repository.savedRunFlow.collect { saved ->
                _uiState.update { it.copy(activeSavedRun = saved) }
            }
        }
    }

    private fun observePlaytestFeedback() {
        viewModelScope.launch {
            repository.playtestFeedbackList.collect { list ->
                _uiState.update { it.copy(playtestFeedbackList = list) }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getOrCreateProfile()
            val best = repository.getBestSurvivalTime()
            val kills = repository.getTotalKills()
            val runs = repository.getTotalRuns()
            _uiState.update {
                it.copy(
                    bestSurvivalSeconds = best,
                    totalKills = kills,
                    totalRuns = runs
                )
            }
        }
    }

    fun updateSettings(newSettings: GameSettings) {
        gameEngine.isBatterySaver = newSettings.isBatterySaver
        _uiState.update { it.copy(gameSettings = newSettings) }
        showToast("Đã lưu thiết lập trợ năng & điều khiển!")
    }

    fun startGame() {
        isRunSaved = false
        val profile = _uiState.value.userProfile
        val hero = _uiState.value.selectedHero
        val stage = _uiState.value.selectedStage

        gameEngine.metaHpBonus = profile.ironBodyRank * 15f
        gameEngine.metaSpeedMultiplier = 1f + profile.swiftStepsRank * 0.06f
        gameEngine.metaDmgMultiplier = 1f + profile.mightyStrikeRank * 0.08f
        gameEngine.metaGoldMultiplier = 1f + profile.greedRuneRank * 0.20f
        gameEngine.metaMagnetMultiplier = 1f + profile.magnetPullRank * 0.25f
        gameEngine.metaFreeRevive = profile.phoenixFeatherRank > 0
        gameEngine.isBatterySaver = _uiState.value.gameSettings.isBatterySaver

        gameEngine.resetGame(hero, stage)
        _uiState.update { it.copy(currentScreen = AppScreen.PLAYING, lastSavedStory = null) }
        startGameLoop()
    }

    fun resumeSavedRun() {
        val saved = _uiState.value.activeSavedRun ?: return
        val hero = SlimeHero.values().find { it.id == saved.heroId } ?: SlimeHero.KNIGHT_SLIME
        val stage = GameStage.values().find { it.id == saved.stageId } ?: GameStage.ENCHANTED_FOREST

        // Parse skills
        val skills = mutableMapOf<SkillId, Int>()
        saved.skillLevelsJson.split(";").filter { it.isNotBlank() }.forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val skill = SkillId.values().find { it.name == parts[0] }
                val level = parts[1].toIntOrNull() ?: 1
                if (skill != null) skills[skill] = level
            }
        }
        if (skills.isEmpty()) {
            skills[hero.starterSkill] = 1
        }

        isRunSaved = false
        val profile = _uiState.value.userProfile
        gameEngine.metaHpBonus = profile.ironBodyRank * 15f
        gameEngine.metaSpeedMultiplier = 1f + profile.swiftStepsRank * 0.06f
        gameEngine.metaDmgMultiplier = 1f + profile.mightyStrikeRank * 0.08f
        gameEngine.metaGoldMultiplier = 1f + profile.greedRuneRank * 0.20f
        gameEngine.metaMagnetMultiplier = 1f + profile.magnetPullRank * 0.25f
        gameEngine.metaFreeRevive = profile.phoenixFeatherRank > 0
        gameEngine.isBatterySaver = _uiState.value.gameSettings.isBatterySaver

        gameEngine.restoreSavedState(
            hero = hero,
            stage = stage,
            hp = saved.playerHp,
            maxHp = saved.playerMaxHp,
            level = saved.playerLevel,
            xp = saved.currentXp,
            xpNeeded = saved.xpNeeded,
            timeRemainingSec = saved.timeRemainingSeconds,
            timeSurvivedSec = saved.totalTimeSurvived,
            kills = saved.killCount,
            gold = saved.goldCollected,
            score = saved.score,
            skills = skills,
            canRevive = saved.canRevive
        )

        _uiState.update { it.copy(currentScreen = AppScreen.PLAYING, selectedHero = hero, selectedStage = stage) }
        startGameLoop()
        showToast("Đã khôi phục trận đánh đã lưu!")
    }

    fun saveCurrentRunAndExit() {
        val st = gameEngine.state.value
        if (st.isGameOver || st.isVictory) {
            navigateTo(AppScreen.MAIN_MENU)
            return
        }

        soundFx.playButtonClick()
        gameLoopJob?.cancel()

        val skillsJson = st.skillLevels.entries.joinToString(";") { "${it.key.name}:${it.value}" }
        val savedEntity = SavedRunEntity(
            heroId = st.selectedHero.id,
            stageId = st.selectedStage.id,
            playerHp = st.playerHp,
            playerMaxHp = st.playerMaxHp,
            playerLevel = st.playerLevel,
            currentXp = st.currentXp,
            xpNeeded = st.xpNeeded,
            timeRemainingSeconds = st.timeRemainingSeconds,
            totalTimeSurvived = st.totalTimeSurvived,
            killCount = st.killCount,
            goldCollected = st.goldCollectedInRun,
            score = st.score,
            skillLevelsJson = skillsJson,
            canRevive = st.canRevive
        )

        viewModelScope.launch {
            repository.saveActiveRun(savedEntity)
            soundFx.stopBgm()
            _uiState.update { it.copy(currentScreen = AppScreen.MAIN_MENU) }
            showToast("Đã lưu tiến độ trận đánh thành công!")
        }
    }

    fun discardSavedRun() {
        soundFx.playButtonClick()
        viewModelScope.launch {
            repository.clearSavedActiveRun()
            showToast("Đã xóa bản lưu trận đánh.")
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            val isBatteryMode = _uiState.value.gameSettings.isBatterySaver
            val frameDelayMs = if (isBatteryMode) 33L else 16L

            while (isActive) {
                val currentTime = System.nanoTime()
                val dt = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime

                gameEngine.update(dt)

                val curState = gameEngine.state.value
                if ((curState.isGameOver || curState.isVictory) && !isRunSaved) {
                    isRunSaved = true
                    handleRunFinished(curState)
                    // Clear active saved run on game over or victory
                    repository.clearSavedActiveRun()
                }

                delay(frameDelayMs)
            }
        }
    }

    private fun handleRunFinished(state: GameState) {
        viewModelScope.launch {
            val runId = repository.saveRun(
                survivalSeconds = state.totalTimeSurvived.toInt(),
                kills = state.killCount,
                levelReached = state.playerLevel,
                isVictory = state.isVictory,
                skills = state.skillLevels,
                score = state.score,
                goldEarned = state.goldCollectedInRun,
                stageId = state.selectedStage.id
            )
            loadStats()
        }
    }

    fun reviveCurrentRun() {
        soundFx.playButtonClick()
        gameEngine.revivePlayer()
        viewModelScope.launch {
            repository.tryUnlockAchievement(AchievementId.RADIANT_REBIRTH)
        }
    }

    fun selectSkillUpgrade(card: SkillCardOption) {
        soundFx.playButtonClick()
        gameEngine.applySkillUpgrade(card)
    }

    fun setJoystickInput(dx: Float, dy: Float) {
        val sens = _uiState.value.gameSettings.joystickSensitivity
        gameEngine.setMovementInput(dx * sens, dy * sens)
    }

    fun navigateTo(screen: AppScreen) {
        soundFx.playButtonClick()
        if (screen == AppScreen.MAIN_MENU && !_uiState.value.bgmMuted) {
            soundFx.setBgmTrack(com.example.game.audio.BgmTrack.MENU)
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun switchBgmTrack(track: com.example.game.audio.BgmTrack) {
        soundFx.playButtonClick()
        soundFx.setBgmTrack(track)
        showToast("🎵 Nhạc nền: ${track.vietnameseTitle} (${track.bpm} BPM)")
    }

    fun selectStage(stage: GameStage) {
        soundFx.playButtonClick()
        viewModelScope.launch {
            val profile = _uiState.value.userProfile
            val isUnlocked = profile.unlockedStages.split(",").contains(stage.id)
            if (isUnlocked) {
                repository.selectStage(stage.id)
                _uiState.update { it.copy(selectedStage = stage) }
                showToast("Đã chọn: ${stage.vietnameseName}")
            } else {
                val success = repository.unlockStage(stage)
                if (success) {
                    soundFx.playLevelUp()
                    _uiState.update { it.copy(selectedStage = stage) }
                    showToast("Đã mở khóa ${stage.vietnameseName} thành công!")
                } else {
                    showToast("Không đủ Tiền Vàng để mở khóa Ải!")
                }
            }
        }
    }

    fun selectHero(hero: SlimeHero) {
        soundFx.playButtonClick()
        viewModelScope.launch {
            val profile = _uiState.value.userProfile
            val isUnlocked = profile.unlockedHeroes.split(",").contains(hero.id)
            if (isUnlocked) {
                repository.selectHero(hero.id)
                _uiState.update { it.copy(selectedHero = hero) }
            } else {
                val success = repository.unlockHero(hero)
                if (success) {
                    soundFx.playLevelUp()
                    _uiState.update { it.copy(selectedHero = hero) }
                    showToast("Đã mở khóa ${hero.vietnameseName} thành công!")
                } else {
                    showToast("Không đủ Tiền Vàng để mở khóa!")
                }
            }
        }
    }

    fun purchaseMetaUpgrade(type: MetaUpgradeType) {
        soundFx.playButtonClick()
        viewModelScope.launch {
            val success = repository.purchaseUpgrade(type)
            if (success) {
                soundFx.playLevelUp()
                showToast("Đã nâng cấp ${type.vietnameseTitle}!")
            } else {
                showToast("Không đủ Tiền Vàng!")
            }
        }
    }

    fun purchaseGoldPack(amount: Int, packName: String) {
        soundFx.playGoldPickup()
        viewModelScope.launch {
            repository.addGoldBonus(amount)
            showToast("Đã nhận $amount Vàng từ $packName!")
        }
    }

    fun submitFeedback(testerName: String, category: String, rating: Int, comments: String) {
        soundFx.playButtonClick()
        viewModelScope.launch {
            repository.submitPlaytestFeedback(testerName, category, rating, comments)
            showToast("Đã gửi phản hồi playtest thành công! Cảm ơn bạn!")
        }
    }

    fun openSageAdvice() {
        soundFx.playButtonClick()
        val st = gameEngine.state.value
        _uiState.update { it.copy(isSageAdviceOpen = true, isSageLoading = true) }
        viewModelScope.launch {
            val advice = repository.getSageAdvice(
                currentLevel = st.playerLevel,
                timeSec = st.totalTimeSurvived.toInt(),
                skills = st.skillLevels
            )
            _uiState.update { it.copy(isSageAdviceOpen = true, sageAdviceText = advice, isSageLoading = false) }
        }
    }

    fun closeSageAdvice() {
        soundFx.playButtonClick()
        _uiState.update { it.copy(isSageAdviceOpen = false) }
    }

    fun toggleLanguage() {
        soundFx.playButtonClick()
        val nextLang = if (Localization.isVietnamese()) AppLanguage.ENGLISH else AppLanguage.VIETNAMESE
        Localization.setLanguage(nextLang)
        _uiState.update { it.copy(currentLanguage = nextLang) }
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.soundMuted
        soundFx.isMuted = newMuted
        _uiState.update { it.copy(soundMuted = newMuted) }
    }

    fun toggleBgmMute() {
        val newBgmMuted = !_uiState.value.bgmMuted
        soundFx.isBgmMuted = newBgmMuted
        if (newBgmMuted) soundFx.stopBgm() else soundFx.startBgm()
        _uiState.update { it.copy(bgmMuted = newBgmMuted) }
    }

    private fun showToast(msg: String) {
        _uiState.update { it.copy(statusToastMessage = msg) }
        viewModelScope.launch {
            delay(2500)
            _uiState.update { it.copy(statusToastMessage = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundFx.stopBgm()
        gameLoopJob?.cancel()
    }
}
