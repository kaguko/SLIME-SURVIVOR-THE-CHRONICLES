package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.SlimeHero
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroSelectScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile = uiState.userProfile
    val unlockedSet = userProfile.unlockedHeroes.split(",").map { it.trim() }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CHỌN HIỆP SĨ SLIME (HEROES)",
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
                            text = "💰 ${userProfile.gold}",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(SlimeHero.values()) { hero ->
                val isUnlocked = unlockedSet.contains(hero.id)
                val isSelected = userProfile.selectedHeroId == hero.id

                HeroCard(
                    hero = hero,
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    onSelect = { viewModel.selectHero(hero) }
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    hero: SlimeHero,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderCol = if (isSelected) PixelGold else if (isUnlocked) hero.color else Color(0xFF334155)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderCol, RoundedCornerShape(14.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(hero.color.copy(alpha = 0.25f))
                            .border(1.5.dp, hero.color, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (hero) {
                                SlimeHero.KNIGHT_SLIME -> "🛡️"
                                SlimeHero.FIRE_SLIME -> "🔥"
                                SlimeHero.WIND_SLIME -> "⚡"
                                SlimeHero.GOLDEN_SLIME -> "👑"
                            },
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = hero.vietnameseName,
                            color = hero.color,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = hero.title,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF334155))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = PixelGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${hero.unlockGoldCost} Vàng",
                            color = PixelGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = hero.description,
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(label = "HP Khởi đầu", value = "${hero.baseHp.toInt()}", color = HealthRed)
                StatBadge(label = "Tốc độ", value = "${hero.baseSpeed.toInt()}", color = NeonCyan)
                StatBadge(label = "Vũ khí gốc", value = hero.starterSkill.name, color = PixelGold)
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0B101D))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column {
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 9.sp)
            Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}
