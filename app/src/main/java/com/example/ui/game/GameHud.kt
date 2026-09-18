package com.example.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameState
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun GameHud(
    state: GameState,
    isSoundMuted: Boolean,
    onPauseClick: () -> Unit,
    onMuteClick: () -> Unit,
    onSageAdviceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // 1. Top Experience Bar (Thanh ngang xanh dương sáng)
        val xpProgress = (state.currentXp.toFloat() / state.xpNeeded.toFloat()).coerceIn(0f, 1f)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, PixelGold, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LV ${state.playerLevel}",
                    color = PixelGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF0D1B2A))
                    .border(1.dp, XpBarBlue.copy(alpha = 0.6f), RoundedCornerShape(7.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(xpProgress)
                        .background(
                            Brush.horizontalGradient(
                                listOf(SlimeBlue, XpBarBlue, NeonCyan)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Score Badge
            Text(
                text = "${state.score} pts",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Middle Row: HP Bar with Slime Avatar on Left & 05:00 Timer in Center & Action buttons on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HP Bar + Slime Avatar
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mini Slime Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SlimeBlue)
                        .border(2.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛡️", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    val hpPct = (state.playerHp / state.playerMaxHp).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(HealthDarkRed)
                            .border(1.dp, HealthRed, RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(hpPct)
                                .background(HealthRed)
                        )
                    }
                    Text(
                        text = "${state.playerHp.toInt()} / ${state.playerMaxHp.toInt()}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Countdown Timer (05:00 -> 00:00)
            val minutes = (state.timeRemainingSeconds / 60).toInt()
            val seconds = (state.timeRemainingSeconds % 60).toInt()
            val timerFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC0B132B))
                    .border(1.5.dp, if (state.timeRemainingSeconds < 60f) HealthRed else NeonCyan, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timerFormatted,
                    color = if (state.timeRemainingSeconds < 60f) HealthRed else Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }

            // Action Buttons (Kills, AI Sage, Sound, Pause)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Kills icon
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x9910002B))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💀", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${state.killCount}",
                        color = Color(0xFFFF8FA3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // AI Sage Strategy Button
                IconButton(
                    onClick = onSageAdviceClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Text(text = "✨", fontSize = 16.sp)
                }

                // Sound Toggle Button
                IconButton(
                    onClick = onMuteClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Sound Mute",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Pause Button
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (state.isGamePaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. Boss Health Bar (When Boss Old Tree Ent is Active)
        if (state.bossActive && state.bossMaxHp > 0f) {
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌲 THẦN CÂY GIÀ (OLD TREE ENT) 🌲",
                    color = EntGlowGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val bossPct = (state.bossHp / state.bossMaxHp).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF2C1810))
                        .border(1.dp, EntGlowGreen, RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(bossPct)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF38B000), EntGlowGreen)))
                    )
                }
            }
        }
    }
}
