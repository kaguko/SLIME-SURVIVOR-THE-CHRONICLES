package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush as GBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinStudioScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var promptInput by remember { mutableStateOf("Hiệp sĩ Slime mặc áo giáp rồng phát sáng neon với thanh kiếm ánh sáng") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedSkinName by remember { mutableStateOf<String?>("Slime Long Vương Thần Giáp (Dragon King Slime)") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LÒ RÈN NGOẠI TRANG AI (SKIN STUDIO)",
                        color = PixelGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(2.dp, NeonCyan, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "👑🛡️✨", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = generatedSkinName ?: "Ngoại trang tùy chỉnh",
                            color = NeonCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Hào quang: Rồng Lửa Tinh Vân • Hiệu ứng bước chân: Sao băng",
                            color = PixelGold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Text(
                text = "Mô tả ngoại trang bạn muốn tạo bằng AI:",
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )

            Button(
                onClick = {
                    isGenerating = true
                    // Simulate generation process
                    generatedSkinName = "Slime Thần Thoại: $promptInput"
                    isGenerating = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGenerating) "Đang Rèn Ngoại Trang..." else "RÈN NGOẠI TRANG BẰNG AI",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Gợi ý mẫu có sẵn:",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = { promptInput = "Slime Cyberpunk Neon tia chớp tương lai" },
                    label = { Text("⚡ Cyberpunk Neon", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = { promptInput = "Slime Băng Tuyết Bắc Cực đội vương miện băng giá" },
                    label = { Text("❄️ Băng Tuyết", fontSize = 11.sp) }
                )
            }
        }
    }
}
