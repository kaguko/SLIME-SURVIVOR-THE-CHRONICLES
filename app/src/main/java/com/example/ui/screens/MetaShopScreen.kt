package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.game.model.MetaUpgradeType
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetaShopScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TIỆM CƯỜNG HÓA VĨNH CỬU",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Dùng Tiền Vàng thu thập được trong các trận chiến để nâng cấp chỉ số vĩnh viễn cho Slime:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            items(MetaUpgradeType.values()) { upgrade ->
                val currentRank = when (upgrade) {
                    MetaUpgradeType.IRON_BODY -> profile.ironBodyRank
                    MetaUpgradeType.SWIFT_STEPS -> profile.swiftStepsRank
                    MetaUpgradeType.MIGHTY_STRIKE -> profile.mightyStrikeRank
                    MetaUpgradeType.GREED_RUNE -> profile.greedRuneRank
                    MetaUpgradeType.MAGNET_PULL -> profile.magnetPullRank
                    MetaUpgradeType.PHOENIX_FEATHER -> profile.phoenixFeatherRank
                }

                val isMax = currentRank >= upgrade.maxRank
                val cost = (upgrade.baseCost * Math.pow(upgrade.costMultiplier.toDouble(), currentRank.toDouble())).toInt()
                val canAfford = profile.gold >= cost && !isMax

                UpgradeRowCard(
                    upgrade = upgrade,
                    currentRank = currentRank,
                    cost = cost,
                    isMax = isMax,
                    canAfford = canAfford,
                    onBuy = { viewModel.purchaseMetaUpgrade(upgrade) }
                )
            }
        }
    }
}

@Composable
private fun UpgradeRowCard(
    upgrade: MetaUpgradeType,
    currentRank: Int,
    cost: Int,
    isMax: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (canAfford) PixelGold.copy(alpha = 0.5f) else Color(0xFF334155), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726))
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
                    .background(Color(0xFF0F172A))
                    .border(1.dp, PixelGold, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = upgrade.icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = upgrade.vietnameseTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = upgrade.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Rank pips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..upgrade.maxRank) {
                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i <= currentRank) PixelGold else Color(0xFF334155))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isMax) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF334155))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(text = "TỐI ĐA", color = PixelGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onBuy,
                    enabled = canAfford,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelGold,
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFF1E293B),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$cost 💰",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
