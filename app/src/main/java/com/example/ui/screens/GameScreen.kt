package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.dialogs.GameOverDialog
import com.example.ui.dialogs.LevelUpDialog
import com.example.ui.dialogs.PauseDialog
import com.example.ui.dialogs.SageAdviceDialog
import com.example.ui.game.GameCanvas
import com.example.ui.game.GameHud
import com.example.ui.game.VirtualJoystick
import com.example.ui.theme.ForestNightDark

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.gameSettings

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestNightDark)
    ) {
        // 1. Game Canvas 2D Top-Down View
        GameCanvas(state = gameState)

        // 2. Touch Virtual Joystick for Smooth Controls (configurable position & size)
        VirtualJoystick(
            onMove = { dx, dy -> viewModel.setJoystickInput(dx, dy) },
            joystickPosition = settings.joystickPosition,
            joystickSize = settings.joystickSize
        )

        // 3. HUD Overlay (XP bar, 05:00 timer, HP bar, Boss bar, Gold counter)
        GameHud(
            state = gameState,
            isSoundMuted = uiState.soundMuted,
            onPauseClick = { viewModel.gameEngine.togglePause() },
            onMuteClick = { viewModel.toggleMute() },
            onSageAdviceClick = { viewModel.openSageAdvice() }
        )

        // 4. Level Up Upgrade Selection Dialog
        if (gameState.isLevelUpPending && gameState.pendingLevelUpCards.isNotEmpty()) {
            LevelUpDialog(
                cards = gameState.pendingLevelUpCards,
                onCardSelected = { card -> viewModel.selectSkillUpgrade(card) }
            )
        }

        // 5. Game Pause Dialog
        if (gameState.isGamePaused && !gameState.isGameOver && !gameState.isVictory) {
            PauseDialog(
                state = gameState,
                onResume = { viewModel.gameEngine.togglePause() },
                onSaveAndExit = { viewModel.saveCurrentRunAndExit() },
                onRestart = { viewModel.startGame() }
            )
        }

        // 6. Game Over / Victory Dialog
        if (gameState.isGameOver || gameState.isVictory) {
            GameOverDialog(
                state = gameState,
                onReviveClick = { viewModel.reviveCurrentRun() },
                onRestartClick = { viewModel.startGame() },
                onMenuClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
            )
        }

        // 7. AI Sage Strategy Dialog
        if (uiState.isSageAdviceOpen) {
            SageAdviceDialog(
                adviceText = uiState.sageAdviceText,
                isLoading = uiState.isSageLoading,
                onDismiss = { viewModel.closeSageAdvice() }
            )
        }
    }
}
