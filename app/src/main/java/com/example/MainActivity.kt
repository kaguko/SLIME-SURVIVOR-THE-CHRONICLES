package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.screens.*
import com.example.ui.theme.ForestNightDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ForestNightDark
                ) {
                    SlimeSurvivorApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SlimeSurvivorApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Crossfade(targetState = uiState.currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            AppScreen.MAIN_MENU -> MainMenuScreen(viewModel = viewModel)
            AppScreen.PLAYING -> GameScreen(viewModel = viewModel)
            AppScreen.STAGE_SELECT -> StageSelectScreen(viewModel = viewModel)
            AppScreen.HERO_SELECT -> HeroSelectScreen(viewModel = viewModel)
            AppScreen.META_SHOP -> MetaShopScreen(viewModel = viewModel)
            AppScreen.STORE_MONETIZATION -> StoreMonetizationScreen(viewModel = viewModel)
            AppScreen.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel)
            AppScreen.ACHIEVEMENTS -> AchievementScreen(viewModel = viewModel)
            AppScreen.SAGE_SANCTUARY -> SageSanctuaryScreen(viewModel = viewModel)
            AppScreen.SKIN_STUDIO -> SkinStudioScreen(viewModel = viewModel)
            AppScreen.PRIVACY_POLICY -> PrivacyPolicyScreen(viewModel = viewModel)
            AppScreen.SETTINGS -> SettingsAndAccessibilityScreen(viewModel = viewModel)
            AppScreen.PLAYTEST_HUB -> PlaytestHubScreen(viewModel = viewModel)
        }
    }
}
