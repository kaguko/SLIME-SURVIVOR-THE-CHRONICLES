package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.engine.GameState
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun GameOverDialog(
    state: GameState,
    onRestartClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Must choose action */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        val isVic = state.isVictory
        val title = if (isVic) "🏆 CHIẾN THẮNG HUY HOÀNG! 🏆" else "💀 BẠN ĐÃ HI SINH! 💀"
        val titleColor = if (isVic) PixelGold else HealthRed
        val borderColor = if (isVic) NeonCyan else HealthRed

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF181829), Color(0xFF090D16))
                    )
                )
                .border(2.dp, borderColor, RoundedCornerShape(20.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = titleColor,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mins = (state.totalTimeSurvived / 60).toInt()
                val secs = (state.totalTimeSurvived % 60).toInt()
                val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)

                StatBox(
                    label = "Thời gian",
                    value = timeStr,
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Diệt Quái",
                    value = "${state.killCount}",
                    color = Color(0xFFFF8FA3),
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Cấp Độ",
                    value = "Lv.${state.playerLevel}",
                    color = PixelGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF131A29))
                    .border(1.dp, PixelGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tổng Điểm: ${state.score} PTS",
                    color = PixelGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Button(
                onClick = onRestartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("restart_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVic) NeonCyan else HealthRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CHƠI LẠI TRẬN MỚI",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("main_menu_button"),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Về Trang Chủ & Sử Thi",
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF101624))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
