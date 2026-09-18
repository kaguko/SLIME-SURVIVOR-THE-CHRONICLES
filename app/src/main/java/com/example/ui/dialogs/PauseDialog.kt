package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.engine.GameState
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun PauseDialog(
    state: GameState,
    onResume: () -> Unit,
    onSaveAndExit: () -> Unit,
    onRestart: () -> Unit
) {
    val mins = state.timeRemainingSeconds.toInt() / 60
    val secs = state.timeRemainingSeconds.toInt() % 60
    val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)

    Dialog(onDismissRequest = onResume) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, PixelGold, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "⏸️ TẠM DỪNG TRẬN ĐÁNH",
                    color = PixelGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                // Current Run Stats
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ải đang đấu:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(state.selectedStage.vietnameseName, color = state.selectedStage.primaryParticleColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Hiệp sĩ Slime:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(state.selectedHero.vietnameseName, color = state.selectedHero.color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Thời gian còn lại:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text(timeStr, color = PixelMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Quái vật đã diệt:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("💀 ${state.killCount}", color = FireRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Vàng thu thập:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("💰 +${state.goldCollectedInRun}", color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // RESUME
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("resume_pause_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ForestNightDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TIẾP TỤC CHIẾN ĐẤU", color = ForestNightDark, fontWeight = FontWeight.Bold)
                }

                // SAVE & EXIT
                Button(
                    onClick = onSaveAndExit,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("save_and_exit_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = ForestNightDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("LƯU & THOÁT RA MENU", color = ForestNightDark, fontWeight = FontWeight.Bold)
                }

                // RESTART
                OutlinedButton(
                    onClick = onRestart,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("restart_run_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CHƠI LẠI TỪ ĐẦU", color = Color.White)
                }
            }
        }
    }
}
