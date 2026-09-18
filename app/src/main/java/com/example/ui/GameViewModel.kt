package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.db.AppDatabase
import com.example.data.db.RunRecordEntity
import com.example.data.repository.GameRepository
import com.example.game.audio.SoundFxSynth
import com.example.game.engine.GameEngine
import com.example.game.engine.GameState
import com.example.game.model.SkillCardOption
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN_MENU,
    PLAYING,
    LEADERBOARD,
    SAGE_SANCTUARY
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
    val soundMuted: Boolean = false
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
    }

    private fun loadStats() {
        viewModelScope.launch {
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

    fun startGame() {
        isRunSaved = false
        gameEngine.resetGame()
        _uiState.update { it.copy(currentScreen = AppScreen.PLAYING, lastSavedStory = null) }
        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val currentTime = System.nanoTime()
                val dt = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime

                gameEngine.update(dt)

                // Check if run finished
                val curState = gameEngine.state.value
                if ((curState.isGameOver || curState.isVictory) && !isRunSaved) {
                    isRunSaved = true
                    handleRunFinished(curState)
                }

                delay(16) // Target ~60 FPS
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
                score = state.score
            )
            loadStats()
        }
    }

    fun selectSkillUpgrade(card: SkillCardOption) {
        gameEngine.applySkillUpgrade(card)
    }

    fun setJoystickInput(dx: Float, dy: Float) {
        gameEngine.setMovementInput(dx, dy)
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun openSageAdvice() {
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
        _uiState.update { it.copy(isSageAdviceOpen = false) }
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.soundMuted
        soundFx.isMuted = newMuted
        _uiState.update { it.copy(soundMuted = newMuted) }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
