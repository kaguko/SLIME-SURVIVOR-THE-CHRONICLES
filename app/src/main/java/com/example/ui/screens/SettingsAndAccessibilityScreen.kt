package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndAccessibilityScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.gameSettings
    val scrollState = rememberScrollState()

    var selectedColorblind by remember(settings.colorblindMode) { mutableStateOf(settings.colorblindMode) }
    var selectedFontScale by remember(settings.fontScale) { mutableStateOf(settings.fontScale) }
    var selectedJoyPos by remember(settings.joystickPosition) { mutableStateOf(settings.joystickPosition) }
    var selectedJoySize by remember(settings.joystickSize) { mutableStateOf(settings.joystickSize) }
    var joySensitivity by remember(settings.joystickSensitivity) { mutableFloatStateOf(settings.joystickSensitivity) }
    var batterySaver by remember(settings.isBatterySaver) { mutableStateOf(settings.isBatterySaver) }
    var haptics by remember(settings.isHapticsEnabled) { mutableStateOf(settings.isHapticsEnabled) }

    fun save() {
        viewModel.updateSettings(
            GameSettings(
                colorblindMode = selectedColorblind,
                fontScale = selectedFontScale,
                joystickPosition = selectedJoyPos,
                joystickSize = selectedJoySize,
                joystickSensitivity = joySensitivity,
                isBatterySaver = batterySaver,
                isHapticsEnabled = haptics
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CÀI ĐẶT & TRỢ NĂNG",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = (18 * selectedFontScale.scale).sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PixelGold
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { save() },
                        colors = ButtonDefaults.buttonColors(containerColor = PixelGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_settings_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ForestNightDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LƯU", color = ForestNightDark, fontWeight = FontWeight.Bold)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. ACCESSIBILITY & COLORBLIND SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Màu Sắc & Chế Độ Mù Màu (Colorblind)",
                            fontWeight = FontWeight.Bold,
                            color = PixelGold,
                            fontSize = (15 * selectedFontScale.scale).sp
                        )
                    }

                    Text(
                        "Điều chỉnh bảng màu trong game để tối ưu khả năng nhận diện quái vật, ngọc kinh nghiệm và bãi độc phù hợp thị giác:",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = (12 * selectedFontScale.scale).sp
                    )

                    ColorblindMode.values().forEach { mode ->
                        val isSelected = selectedColorblind == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PixelMint.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) PixelMint else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedColorblind = mode; save() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedColorblind = mode; save() },
                                colors = RadioButtonDefaults.colors(selectedColor = PixelMint)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    mode.vietnameseName,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PixelMint else Color.White,
                                    fontSize = (13 * selectedFontScale.scale).sp
                                )
                                Text(
                                    mode.description,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = (11 * selectedFontScale.scale).sp
                                )
                            }
                        }
                    }

                    // Live Swatch Preview
                    Text("Xem trước bảng màu:", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val testColors = listOf(
                            Pair("Máu Slime", SlimeBlue),
                            Pair("Lửa Đỏ", FireRed),
                            Pair("Ngọc Xanh", PixelMint),
                            Pair("Vàng Hoàng Kim", PixelGold),
                            Pair("Độc Tím", SporePurple)
                        )
                        testColors.forEach { (label, col) ->
                            val adjusted = selectedColorblind.let {
                                GameSettings(colorblindMode = it).adjustColor(col)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(adjusted)
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // 2. FONT SCALING SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔤", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Kích Thước Chữ (Font Scaling)",
                            fontWeight = FontWeight.Bold,
                            color = PixelGold,
                            fontSize = (15 * selectedFontScale.scale).sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FontScale.values().forEach { scale ->
                            val isSel = selectedFontScale == scale
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) PixelGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        width = if (isSel) 1.5.dp else 0.5.dp,
                                        color = if (isSel) PixelGold else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedFontScale = scale; save() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    scale.vietnameseName,
                                    color = if (isSel) PixelGold else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. CONTROLS & JOYSTICK REMAPPING SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎮", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tùy Chỉnh Phím Điều Khiển (Joystick)",
                            fontWeight = FontWeight.Bold,
                            color = PixelGold,
                            fontSize = (15 * selectedFontScale.scale).sp
                        )
                    }

                    // Position (Left / Right handed)
                    Text("Vị trí Cần Gạt Joystick:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JoystickPosition.values().forEach { pos ->
                            val isSel = selectedJoyPos == pos
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SlimeBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        width = if (isSel) 1.5.dp else 0.5.dp,
                                        color = if (isSel) SlimeBlue else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedJoyPos = pos; save() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    pos.vietnameseName,
                                    color = if (isSel) SlimeBlue else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Size
                    Text("Kích thước Cần Gạt:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JoystickSize.values().forEach { sz ->
                            val isSel = selectedJoySize == sz
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SlimeBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        width = if (isSel) 1.5.dp else 0.5.dp,
                                        color = if (isSel) SlimeBlue else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedJoySize = sz; save() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    sz.vietnameseName,
                                    color = if (isSel) SlimeBlue else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Sensitivity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Độ nhạy Cần Gạt (Sensitivity):", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("${(joySensitivity * 100).toInt()}%", color = PixelGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Slider(
                        value = joySensitivity,
                        onValueChange = { joySensitivity = it },
                        onValueChangeFinished = { save() },
                        valueRange = 0.5f..2.0f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = PixelGold,
                            activeTrackColor = PixelGold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            // 4. PERFORMANCE & BATTERY SAVER SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Hiệu Năng & Tiết Kiệm Pin (Performance)",
                            fontWeight = FontWeight.Bold,
                            color = PixelGold,
                            fontSize = (15 * selectedFontScale.scale).sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Chế độ Tiết Kiệm Pin (Battery Saver)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Giới hạn 30 FPS, giảm hiệu ứng hạt phân tán để kéo dài thời lượng pin và giữ máy mát mẻ.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = batterySaver,
                            onCheckedChange = { batterySaver = it; save() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PixelMint,
                                checkedTrackColor = PixelMint.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Rung phản hồi khi nhận đòn (Haptics)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Rung nhẹ khi slime trúng đòn của quái vật.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = haptics,
                            onCheckedChange = { haptics = it; save() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PixelGold,
                                checkedTrackColor = PixelGold.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // 5. PLAYTEST HUB LINK
            Button(
                onClick = { viewModel.navigateTo(AppScreen.PLAYTEST_HUB) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("open_playtest_hub_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PixelPurple)
            ) {
                Text("🧪", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "XEM KẾT QUẢ PLAYTEST (28+ TESTERS)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = (13 * selectedFontScale.scale).sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
