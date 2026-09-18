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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaytestTelemetryData
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaytestHubScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Telemetry Stats, 1: Weapon Tier List, 2: Feedback Form & Reviews

    var testerName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Cân Bằng Game (Balance)") }
    var rating by remember { mutableIntStateOf(5) }
    var comments by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TRUNG TÂM PLAYTEST & PHẢN HỒI",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier.testTag("playtest_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PixelGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = ForestNightDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = PixelGold
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📊 Dữ Liệu 34 Lượt", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("⚔️ Tier List Vũ Khí", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("📝 Gửi Đánh Giá", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Telemetry & Stage Statistics
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("TỔNG QUAN PLAYTEST TOÀN CẦU", color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${PlaytestTelemetryData.totalPlaytestSessions}", color = PixelMint, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                            Text("Lượt Test", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("⭐ ${PlaytestTelemetryData.averageSatisfactionRating}/5.0", color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                            Text("Hài Lòng", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${PlaytestTelemetryData.totalBugsIdentifiedAndFixed}", color = FireRed, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                            Text("Bug Đã Fix", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("TỈ LỆ THẮNG & THỜI GIAN SỐNG THEO ẢI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        items(PlaytestTelemetryData.stageStats) { stat ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(stat.stageName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Text("Tỉ lệ qua màn: ${stat.clearRatePercent}%", color = if (stat.clearRatePercent > 50) PixelMint else PixelGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    LinearProgressIndicator(
                                        progress = { stat.clearRatePercent / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (stat.clearRatePercent > 50) PixelMint else PixelGold,
                                        trackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("⏱️ Sống sót TB: ${stat.avgSurvivalSeconds}s", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                        Text("🌟 Anh hùng chuộng: ${stat.mostPopularHero}", color = SlimeBlue, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Weapon Tier List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("BẢNG XẾP HẠNG VŨ KHÍ (DPS & CÂN BẰNG METAGAME)", color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        items(PlaytestTelemetryData.weaponTiers) { wp ->
                            val tierColor = when (wp.tier) {
                                "S+" -> FireRed
                                "S" -> PixelGold
                                "A" -> PixelMint
                                else -> SlimeBlue
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(tierColor.copy(alpha = 0.2f))
                                            .border(1.dp, tierColor, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(wp.tier, color = tierColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(wp.weaponName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${wp.dpsRating} • Tỉ lệ chọn: ${wp.pickRatePercent}%", color = PixelGold, fontSize = 11.sp)
                                        Text(wp.comment, color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Submit feedback and review list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("GỬI Ý KIẾN PLAYTEST TRỰC TIẾP", color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                    OutlinedTextField(
                                        value = testerName,
                                        onValueChange = { testerName = it },
                                        label = { Text("Tên người chơi / Nickname") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("playtest_name_input"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PixelGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                        )
                                    )

                                    // Category selector
                                    val categories = listOf("Cân Bằng Game (Balance)", "Tối Ưu Pin & FPS", "Báo Lỗi (Bug)", "Góp Ý Tính Năng")
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        categories.take(2).forEach { cat ->
                                            val isSel = selectedCategory == cat
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSel) PixelMint.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                                    .border(1.dp, if (isSel) PixelMint else Color.Transparent, RoundedCornerShape(6.dp))
                                                    .clickable { selectedCategory = cat }
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(cat, color = if (isSel) PixelMint else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Rating
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Đánh giá: ", color = Color.White, fontSize = 12.sp)
                                        (1..5).forEach { star ->
                                            Text(
                                                text = if (star <= rating) "⭐" else "☆",
                                                fontSize = 20.sp,
                                                modifier = Modifier
                                                    .clickable { rating = star }
                                                    .padding(2.dp)
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = comments,
                                        onValueChange = { comments = it },
                                        label = { Text("Nhận xét về độ mượt, độ khó, quái vật...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                            .testTag("playtest_comment_input"),
                                        maxLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PixelGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            if (comments.isNotBlank()) {
                                                viewModel.submitFeedback(testerName, selectedCategory, rating, comments)
                                                comments = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("submit_feedback_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = PixelMint)
                                    ) {
                                        Text("GỬI PHẢN HỒI CHO DEVELOPER", color = ForestNightDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Text("NHẬN XÉT TỪ ĐỘI NGŨ PLAYTESTER (28+ BÁO CÁO)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        items(PlaytestTelemetryData.simulatedReviews) { rev ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(rev.testerName, color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("⭐".repeat(rev.rating), fontSize = 12.sp)
                                    }
                                    Text("Chức danh: ${rev.role}", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                    Text("\"${rev.comment}\"", color = Color.White, fontSize = 11.sp)
                                    Text("🎯 Combo yêu thích: ${rev.favoriteCombo}", color = PixelMint, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
