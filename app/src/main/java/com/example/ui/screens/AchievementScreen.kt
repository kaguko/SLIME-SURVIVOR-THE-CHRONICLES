package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.AchievementId
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val unlockedIds = uiState.unlockedAchievementIds

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DANH HIỆU & THÀNH TỰU (ACHIEVEMENTS)",
                        color = PixelGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
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
                            text = "Đạt: ${unlockedIds.size}/${AchievementId.values().size}",
                            color = PixelGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(AchievementId.values()) { ach ->
                val isUnlocked = unlockedIds.contains(ach.name)
                AchievementCard(ach = ach, isUnlocked = isUnlocked)
            }
        }
    }
}

@Composable
private fun AchievementCard(
    ach: AchievementId,
    isUnlocked: Boolean
) {
    val borderColor = if (isUnlocked) PixelGold else Color(0xFF334155)
    val bgColor = if (isUnlocked) Color(0xFF161F30) else Color(0xFF0F1420)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isUnlocked) PixelGold.copy(alpha = 0.2f) else Color(0xFF1E293B))
                    .border(1.dp, if (isUnlocked) PixelGold else Color(0xFF475569), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = ach.icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ach.vietnameseTitle,
                    color = if (isUnlocked) PixelGold else Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = ach.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isUnlocked) {
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Đã đạt",
                        tint = PixelGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "+${ach.goldReward} 💰",
                        color = PixelGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${ach.goldReward} Vàng",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
