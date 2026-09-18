package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.*
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*
import com.example.util.Localization
import java.util.Locale

@Composable
fun MainMenuScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ForestNightDark, ForestDarkSurface, Color(0xFF070B11))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Gold Balance, Language Switch, BGM & SFX Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.2.dp, PixelGold, RoundedCornerShape(8.dp))
                    .clickable { viewModel.navigateTo(AppScreen.STORE_MONETIZATION) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "💰 ${profile.gold} Vàng +",
                    color = PixelGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { viewModel.toggleLanguage() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x661E293B))
                ) {
                    Text(text = uiState.currentLanguage.flag, fontSize = 16.sp)
                }

                IconButton(
                    onClick = { viewModel.toggleBgmMute() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x661E293B))
                ) {
                    Icon(
                        imageVector = if (uiState.bgmMuted) Icons.Default.MusicOff else Icons.Default.MusicNote,
                        contentDescription = "BGM Toggle",
                        tint = if (uiState.bgmMuted) Color(0xFF94A3B8) else PixelGold
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x661E293B))
                ) {
                    Icon(
                        imageVector = if (uiState.soundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "SFX Toggle",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hero & Active Stage Art Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clickable { viewModel.navigateTo(AppScreen.STAGE_SELECT) }
                .border(2.dp, uiState.selectedStage.primaryParticleColor, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = uiState.selectedStage.bannerRes),
                    contentDescription = uiState.selectedStage.vietnameseName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xDD090D16))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.selectedStage.vietnameseName,
                        color = uiState.selectedStage.primaryParticleColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${uiState.selectedStage.subtitle} • Đại Boss: ${uiState.selectedStage.bossType.vietnameseName}",
                        color = PixelGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Stage & Hero Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stage Switch
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(AppScreen.STAGE_SELECT) }
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🗺️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "ẢI THẾ GIỚI", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = uiState.selectedStage.vietnameseName, color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Hero Switch
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(AppScreen.HERO_SELECT) }
                    .border(1.dp, uiState.selectedHero.color.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🛡️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "HIỆP SĨ SLIME", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = uiState.selectedHero.vietnameseName, color = uiState.selectedHero.color, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lifetime Stats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mins = uiState.bestSurvivalSeconds / 60
                val secs = uiState.bestSurvivalSeconds % 60
                val bestTimeStr = String.format(Locale.US, "%02d:%02d", mins, secs)

                StatItem(label = Localization.get("best_time"), value = bestTimeStr, color = NeonCyan)
                Divider(modifier = Modifier.height(26.dp).width(1.dp), color = Color(0xFF334155))
                StatItem(label = Localization.get("total_kills"), value = "${uiState.totalKills}", color = Color(0xFFFF8FA3))
                Divider(modifier = Modifier.height(26.dp).width(1.dp), color = Color(0xFF334155))
                StatItem(label = "THÀNH TỰU", value = "${uiState.unlockedAchievementIds.size}/13", color = PixelGold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // RESUME ACTIVE RUN BANNER (If saved run exists)
        uiState.activeSavedRun?.let { saved ->
            val savedHero = SlimeHero.values().find { it.id == saved.heroId } ?: SlimeHero.KNIGHT_SLIME
            val savedStage = GameStage.values().find { it.id == saved.stageId } ?: GameStage.ENCHANTED_FOREST
            val mins = saved.timeRemainingSeconds.toInt() / 60
            val secs = saved.timeRemainingSeconds.toInt() % 60
            val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, PixelGold, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💾 CÓ BẢN LƯU TRẬN ĐÁNH",
                            color = PixelGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        IconButton(
                            onClick = { viewModel.discardSavedRun() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Discard Save", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ải: ${savedStage.vietnameseName}", color = Color.White, fontSize = 11.sp)
                        Text("Hiệp sĩ: ${savedHero.vietnameseName}", color = savedHero.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cấp: Lv.${saved.playerLevel} | 💀 ${saved.killCount}", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("⏱️ Còn lại: $timeStr", color = PixelMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.resumeSavedRun() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("resume_run_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PixelGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ForestNightDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TIẾP TỤC TRẬN ĐÁNH (RESUME)", color = ForestNightDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // START RUN Primary Button
        Button(
            onClick = { viewModel.startGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_run_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${Localization.get("start_run")} (${uiState.selectedStage.vietnameseName})",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of Menu Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuTile(
                title = Localization.get("meta_shop"),
                icon = "⚡",
                color = PixelGold,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.META_SHOP) }
            )
            MenuTile(
                title = "THÀNH TỰU",
                icon = "🏆",
                color = NeonCyan,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.ACHIEVEMENTS) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuTile(
                title = Localization.get("store_monetization"),
                icon = "💎",
                color = GemGold,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.STORE_MONETIZATION) }
            )
            MenuTile(
                title = Localization.get("chronicles"),
                icon = "📜",
                color = Color(0xFFCBD5E1),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.LEADERBOARD) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuTile(
                title = Localization.get("skin_forge"),
                icon = "🎨",
                color = Color(0xFFE0AAFF),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.SKIN_STUDIO) }
            )
            MenuTile(
                title = Localization.get("bestiary"),
                icon = "📖",
                color = Color(0xFF94A3B8),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.SAGE_SANCTUARY) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuTile(
                title = "CÀI ĐẶT & TRỢ NĂNG",
                icon = "⚙️",
                color = PixelGold,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
            )
            MenuTile(
                title = "PLAYTEST & BÁO CÁO",
                icon = "🧪",
                color = PixelMint,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.PLAYTEST_HUB) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Chính Sách Quyền Riêng Tư & Quy Định Google Play",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            modifier = Modifier
                .clickable { viewModel.navigateTo(AppScreen.PRIVACY_POLICY) }
                .padding(vertical = 4.dp)
        )

        // Status Toast Message
        uiState.statusToastMessage?.let { msg ->
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xEE1E293B))
                    .border(1.dp, PixelGold, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(text = msg, color = PixelGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MenuTile(
    title: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(54.dp)
            .clickable { onClick() }
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}
