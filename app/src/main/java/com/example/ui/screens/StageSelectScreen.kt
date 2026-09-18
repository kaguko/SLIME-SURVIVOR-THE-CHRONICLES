package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.GameStage
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageSelectScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile
    val unlockedStages = profile.unlockedStages.split(",").map { it.trim() }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CHỌN ẢI THẾ GIỚI (STAGES)",
                        color = PixelGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, PixelGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💰 ${profile.gold}",
                            color = PixelGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestDarkSurface)
            )
        },
        containerColor = ForestNightDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Mỗi Ải Thế Giới sở hữu hệ thống quái vật, Đại Boss độc quyền và tỷ lệ thưởng Vàng/XP tăng dần:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            items(GameStage.values()) { stage ->
                val isUnlocked = unlockedStages.contains(stage.id)
                val isSelected = profile.selectedStageId == stage.id

                StageCard(
                    stage = stage,
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    onSelect = { viewModel.selectStage(stage) }
                )
            }
        }
    }
}

@Composable
private fun StageCard(
    stage: GameStage,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) PixelGold else if (isUnlocked) NeonCyan else Color(0xFF334155)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726))
    ) {
        Column {
            // Official Stage Art Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                Image(
                    painter = painterResource(id = stage.bannerRes),
                    contentDescription = stage.vietnameseName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xDD101726))
                            )
                        )
                )

                // Subtitle Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC000000))
                        .border(1.dp, stage.primaryParticleColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stage.subtitle,
                        color = stage.primaryParticleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                // Selection or Lock Tag
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PixelGold)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ĐANG CHỌN",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                } else if (!isUnlocked) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xEE1E293B))
                            .border(1.dp, PixelGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PixelGold, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${stage.unlockGoldCost} Vàng", color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            // Description and multipliers
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stage.vietnameseName,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = stage.description,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BonusBadge(label = "Thưởng Vàng", value = "+${((stage.goldMultiplier - 1f) * 100).toInt()}%", color = PixelGold)
                    BonusBadge(label = "Thưởng EXP", value = "+${((stage.xpMultiplier - 1f) * 100).toInt()}%", color = NeonCyan)
                    BonusBadge(label = "Đại Boss", value = stage.bossType.vietnameseName, color = HealthRed)
                }
            }
        }
    }
}

@Composable
private fun BonusBadge(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0B101D))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column {
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 8.sp)
            Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}
