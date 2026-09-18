package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun SageSanctuaryScreen(viewModel: GameViewModel) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SỔ TAY QUÁI VẬT & KHO KỸ NĂNG",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestDarkSurface)
            )
        },
        containerColor = ForestNightDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Section 1: Kẻ Địch (Quái Vật Rừng Sâu)
            SectionHeader(title = "👹 KẺ ĐỊCH (MONSTER BESTIARY)", color = Color(0xFFFF8FA3))

            Spacer(modifier = Modifier.height(10.dp))

            BestiaryCard(
                icon = "🍄",
                name = "Nấm Độc (Spore Fungus)",
                spawnTime = "Xuất hiện: 00:00",
                description = "Cây nấm màu tím với mũ nấm đốm hồng phát sáng nhè nhẹ. Bước đi lạch bạch chậm rãi nhưng đông đảo. Rơi Ngọc Xanh Lá (5 XP).",
                accentColor = SporePurple
            )

            Spacer(modifier = Modifier.height(10.dp))

            BestiaryCard(
                icon = "🦇",
                name = "Dơi Lửa (Flame Bat)",
                spawnTime = "Xuất hiện: 02:00",
                description = "Dơi pixel màu đỏ rực, cánh đập liên tục. Lao tới với tốc độ cực nhanh nhưng máu yếu. Rơi Ngọc Đỏ (25 XP).",
                accentColor = HealthRed
            )

            Spacer(modifier = Modifier.height(10.dp))

            BestiaryCard(
                icon = "🌲",
                name = "Thần Cây Già (Old Tree Ent) - BOSS",
                spawnTime = "Xuất hiện: 04:00",
                description = "Trùm khổng lồ to gấp 4 lần Slime, khối gỗ màu nâu xám với đôi mắt phát sáng xanh lục sâu thẳm. Mỗi bước đi làm rung chuyển màn hình!",
                accentColor = EntGlowGreen
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Kho Vũ Khí & Phước Lành
            SectionHeader(title = "⚔️ KHO VŨ KHÍ & PHƯỚC LÀNH", color = NeonCyan)

            Spacer(modifier = Modifier.height(10.dp))

            WeaponCard(
                icon = "⚡",
                name = "Tia Sét Định Vị (Chain Lightning)",
                description = "Tia chớp màu xanh neon rực sáng giật thẳng từ trên trời xuống con quái gần nhất, sinh bụi điện nổ xung quanh.",
                accentColor = NeonCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            WeaponCard(
                icon = "🔥",
                name = "Vòng Lửa Bảo Vệ (Fire Orbit)",
                description = "Hai quả cầu lửa màu cam đỏ xoay tròn liên tục quanh Slime với vệt đuôi mờ kéo dài, thiêu rụi kẻ địch đến gần.",
                accentColor = NeonFireOrange
            )

            Spacer(modifier = Modifier.height(10.dp))

            WeaponCard(
                icon = "🪓",
                name = "Rìu Xoay Càn Quét (Spinning Axe)",
                description = "Phóng rìu xoay theo hình xoắn ốc mở rộng, xuyên qua toàn bộ bầy quái vật trên đường bay.",
                accentColor = PixelGold
            )

            Spacer(modifier = Modifier.height(10.dp))

            WeaponCard(
                icon = "🧪",
                name = "Bãi Độc Thánh (Holy Puddle)",
                description = "Để lại những vũng axit ma thuật phát sáng phía sau bước chân của Slime, đốt cháy quái vật dẫm phải.",
                accentColor = SporePurple
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontWeight = FontWeight.Black,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun BestiaryCard(
    icon: String,
    name: String,
    spawnTime: String,
    description: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, accentColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = spawnTime, color = PixelGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = description, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun WeaponCard(
    icon: String,
    name: String,
    description: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, accentColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = description, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}
