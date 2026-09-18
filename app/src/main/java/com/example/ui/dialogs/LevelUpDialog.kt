package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
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
import com.example.game.model.SkillCardOption
import com.example.game.model.SkillId
import com.example.ui.theme.*

@Composable
fun LevelUpDialog(
    cards: List<SkillCardOption>,
    onCardSelected: (SkillCardOption) -> Unit
) {
    Dialog(
        onDismissRequest = { /* Must choose an upgrade */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E140F), Color(0xFF0F0B08))
                    )
                )
                .border(2.5.dp, PixelGold, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚡ THĂNG CẤP! (LEVEL UP) ⚡",
                color = PixelGold,
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Chọn 1 trong các phước lành để cường hóa Slime Hiệp Sĩ:",
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            cards.forEachIndexed { index, card ->
                SkillCardItem(
                    card = card,
                    onClick = { onCardSelected(card) },
                    modifier = Modifier.testTag("skill_card_$index")
                )
                if (index < cards.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SkillCardItem(
    card: SkillCardOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (card.isEvolution) PixelGold else if (card.isNew) NeonCyan else CardWoodBorder

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWoodBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF15100B))
                    .border(1.dp, if (card.isEvolution) PixelGold else NeonCyan, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                val iconEmoji = when (card.skillId) {
                    SkillId.CHAIN_LIGHTNING -> "⚡"
                    SkillId.FIRE_ORBIT -> "🔥"
                    SkillId.SPINNING_AXE -> "🪓"
                    SkillId.HOLY_PUDDLE -> "🧪"
                    SkillId.SPEED_BOOTS -> "👟"
                    SkillId.MAGNET_RING -> "🧲"
                    SkillId.MAX_HP_BOOST -> "🛡️"
                    SkillId.REGENERATION -> "💖"
                    SkillId.THUNDER_WRATH -> "🌩️"
                    SkillId.SOLAR_SUPERNOVA -> "☀️"
                }
                Text(text = iconEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.vietnameseTitle,
                        color = if (card.isEvolution) PixelGold else PixelGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    val badgeText = if (card.isEvolution) "TIẾN HÓA" else if (card.isNew) "MỚI" else "Lv.${card.targetLevel}"
                    val badgeColor = if (card.isEvolution) PixelGold else if (card.isNew) NeonCyan else GemGold
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = card.description,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
