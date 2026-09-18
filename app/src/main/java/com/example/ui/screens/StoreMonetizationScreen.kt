package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
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
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreMonetizationScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KHO BÁU & GÓI HỖ TRỢ",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Free Rewarded Ad simulation
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NeonCyan, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E2E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(1.dp, NeonCyan, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Tv, contentDescription = null, tint = NeonCyan)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Xem Video Quảng Cáo",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Nhận ngay +100 Tiền Vàng miễn phí!",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.purchaseGoldPack(100, "Xem Quảng Cáo") },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "NHẬN 🎁", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Supporter Pack VIP
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, PixelGold, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E170A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = PixelGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GÓI HIỆP SĨ HOÀNG GIA (VIP PACK)",
                                color = PixelGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "• Mở khóa toàn bộ 4 Slime Heroes\n• Tặng kèm 2,500 Tiền Vàng\n• Tăng vĩnh viễn 25% tỷ lệ rơi ngọc kinh nghiệm",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.purchaseGoldPack(2500, "Gói Hiệp Sĩ Hoàng Gia") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PixelGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "MỞ KHÓA VIP ($2.99 / MÔ PHỎNG)",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Gold Bundles
            item {
                Text(
                    text = "GÓI TIỀN VÀNG TIỆM RỪNG",
                    color = PixelGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            item {
                GoldBundleRow(
                    icon = "🪙",
                    name = "Túi Vàng Thám Hiểm",
                    amount = 500,
                    price = "$0.99",
                    onBuy = { viewModel.purchaseGoldPack(500, "Túi Vàng Thám Hiểm") }
                )
            }

            item {
                GoldBundleRow(
                    icon = "💰",
                    name = "Rương Vàng Hiệp Sĩ",
                    amount = 1500,
                    price = "$1.99",
                    onBuy = { viewModel.purchaseGoldPack(1500, "Rương Vàng Hiệp Sĩ") }
                )
            }

            item {
                GoldBundleRow(
                    icon = "👑",
                    name = "Kho Báu Rồng Lửa",
                    amount = 5000,
                    price = "$4.99",
                    onBuy = { viewModel.purchaseGoldPack(5000, "Kho Báu Rồng Lửa") }
                )
            }
        }
    }
}

@Composable
private fun GoldBundleRow(
    icon: String,
    name: String,
    amount: Int,
    price: String,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 26.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "+$amount Vàng", color = PixelGold, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
            Button(
                onClick = onBuy,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, PixelGold)
            ) {
                Text(text = price, color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
