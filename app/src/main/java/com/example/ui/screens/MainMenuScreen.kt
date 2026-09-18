package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.audio.BgmTrack
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
    var isJukeboxOpen by remember { mutableStateOf(false) }

    // Pulsing animation for the main action button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ForestNightDark, ForestDarkSurface, Color(0xFF060911))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HEADER BAR: Profile Gold & Quick Control Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gold Wallet Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF161F30))
                    .border(1.5.dp, PixelGold, RoundedCornerShape(10.dp))
                    .clickable { viewModel.navigateTo(AppScreen.STORE_MONETIZATION) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${profile.gold} Vàng",
                        color = PixelGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+",
                        color = Color(0xFF2EC4B6),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            // Quick Audio & Language Controls
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Jukebox BGM Selector
                IconButton(
                    onClick = { isJukeboxOpen = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x881E293B))
                        .border(1.dp, if (!uiState.bgmMuted) PixelGold else Color.Gray, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Jukebox BGM",
                        tint = if (uiState.bgmMuted) Color(0xFF94A3B8) else PixelGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Language
                IconButton(
                    onClick = { viewModel.toggleLanguage() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x881E293B))
                ) {
                    Text(text = uiState.currentLanguage.flag, fontSize = 16.sp)
                }

                // SFX Toggle
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x881E293B))
                ) {
                    Icon(
                        imageVector = if (uiState.soundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "SFX Toggle",
                        tint = if (uiState.soundMuted) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HERO TITLE & ART BANNER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(2.dp, Brush.horizontalGradient(listOf(NeonCyan, PixelPurple, PixelGold)), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1789704783959),
                    contentDescription = "Slime Survivor Hero Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x44000000),
                                    Color(0x88000000),
                                    Color(0xF5090D16)
                                )
                            )
                        )
                )

                // Top Floating Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC0F172A))
                        .border(1.dp, PixelMint, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "★ NANO BANANA 2.0 • ROGUELIKE ACTION ★",
                        color = PixelMint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Title Content at Bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "SLIME SURVIVOR",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Sống Sót 5 Phút • Thu Thập Ngọc • Trảm Đại Boss",
                        color = PixelGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Jukebox Mini Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF111827))
                .border(1.dp, Color(0xFF374151), RoundedCornerShape(10.dp))
                .clickable { isJukeboxOpen = true }
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎵", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nhạc: ${viewModel.soundFx.currentTrack.vietnameseTitle} (${viewModel.soundFx.currentTrack.bpm} BPM)",
                        color = if (uiState.bgmMuted) Color(0xFF9CA3AF) else NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Đổi Nhạc ▾",
                    color = PixelGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // QUICK SELECTION DUAL CARDS: Stage & Hero
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Selected Stage Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(AppScreen.STAGE_SELECT) }
                    .border(1.5.dp, uiState.selectedStage.primaryParticleColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2B))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ẢI THẾ GIỚI", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = "ĐỔI >", color = uiState.selectedStage.primaryParticleColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.selectedStage.vietnameseName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Boss: ${uiState.selectedStage.bossType.vietnameseName}",
                        color = PixelGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Selected Hero Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(AppScreen.HERO_SELECT) }
                    .border(1.5.dp, uiState.selectedHero.color, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2B))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "HIỆP SĨ SLIME", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = "ĐỔI >", color = uiState.selectedHero.color, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.selectedHero.vietnameseName,
                        color = uiState.selectedHero.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "HP: ${uiState.selectedHero.baseHp.toInt()} • ${uiState.selectedHero.starterSkill.vietnameseTitle}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                    .border(2.dp, PixelGold, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2234))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💾", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TRẬN ĐÁNH ĐANG DỞ DANG",
                                color = PixelGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
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
                        Text("Cấp: Lv.${saved.playerLevel} | 💀 ${saved.killCount} Hạ gục", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("⏱️ Còn lại: $timeStr", color = PixelMint, fontSize = 11.sp, fontWeight = FontWeight.Black)
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
                        Text("TIẾP TỤC TRẬN ĐÁNH (RESUME)", color = ForestNightDark, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // PRIMARY ACTION: START RUN BUTTON (Dynamic Pulse Glow)
        Button(
            onClick = { viewModel.startGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .scale(pulseScale)
                .testTag("start_run_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(14.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "🔥 XUẤT TRẬN - ${uiState.selectedStage.vietnameseName.uppercase()} 🔥",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LIFETIME STATS SUMMARY BAR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2D3B4E), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101726))
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
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color(0xFF334155)))
                StatItem(label = Localization.get("total_kills"), value = "${uiState.totalKills}", color = Color(0xFFFF8FA3))
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color(0xFF334155)))
                StatItem(label = "THÀNH TỰU", value = "${uiState.unlockedAchievementIds.size}/13", color = PixelGold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FEATURE MENU GRID (High Contrast & Attractive Layout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuTile(
                title = Localization.get("meta_shop"),
                subtitle = "Nâng cấp chỉ số vĩnh viễn",
                icon = "⚡",
                color = PixelGold,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.META_SHOP) }
            )
            MenuTile(
                title = "ĐẤU TRƯỜNG & SỬ KÝ",
                subtitle = "Bảng vàng & AI Ký sử",
                icon = "🏆",
                color = NeonCyan,
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
                title = Localization.get("store_monetization"),
                subtitle = "Gói Vàng & Ưu đãi",
                icon = "💎",
                color = GemGold,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.STORE_MONETIZATION) }
            )
            MenuTile(
                title = Localization.get("skin_forge"),
                subtitle = "Tạo ngoại trang bằng AI",
                icon = "🎨",
                color = Color(0xFFD8B4FE),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.SKIN_STUDIO) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuTile(
                title = "PLAYTEST & BÁO CÁO",
                subtitle = "Dữ liệu cân bằng game",
                icon = "🧪",
                color = PixelMint,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.PLAYTEST_HUB) }
            )
            MenuTile(
                title = "CÀI ĐẶT & TRỢ NĂNG",
                subtitle = "Âm nhạc, Mù màu & Pin",
                icon = "⚙️",
                color = Color(0xFFFBBF24),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuTile(
                title = Localization.get("bestiary"),
                subtitle = "Cẩm nang quái & Lời khuyên",
                icon = "📖",
                color = Color(0xFF94A3B8),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.SAGE_SANCTUARY) }
            )
            MenuTile(
                title = "BẢNG THÀNH TỰU",
                subtitle = "Mở khóa 13 danh hiệu",
                icon = "🎖️",
                color = Color(0xFFFF758F),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.ACHIEVEMENTS) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Privacy Policy link
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

    // Jukebox Soundtrack Selection Modal
    if (isJukeboxOpen) {
        AlertDialog(
            onDismissRequest = { isJukeboxOpen = false },
            containerColor = Color(0xFF141E2E),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎵", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Máy Phát Nhạc Retro (Jukebox)",
                        color = PixelGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Chọn bản nhạc 8-bit retro polyphonic để thưởng thức ngay:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    BgmTrack.values().forEach { track ->
                        val isPlaying = (viewModel.soundFx.currentTrack == track && !uiState.bgmMuted)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (uiState.bgmMuted) {
                                        viewModel.toggleBgmMute()
                                    }
                                    viewModel.switchBgmTrack(track)
                                    isJukeboxOpen = false
                                }
                                .border(
                                    1.2.dp,
                                    if (isPlaying) NeonCyan else Color(0xFF2D3B4E),
                                    RoundedCornerShape(8.dp)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPlaying) Color(0xFF1F2E45) else Color(0xFF0F1726)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = track.vietnameseTitle,
                                        color = if (isPlaying) NeonCyan else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${track.title} • ${track.bpm} BPM",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                                if (isPlaying) {
                                    Text(text = "▶ ĐANG PHÁT", color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                } else {
                                    Text(text = "PHÁT", color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { isJukeboxOpen = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Đóng", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun MenuTile(
    title: String,
    subtitle: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .clickable { onClick() }
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    color = color,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
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
