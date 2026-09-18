package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ColorblindMode(val displayName: String, val vietnameseName: String, val description: String) {
    NORMAL("Normal", "Mặc định", "Màu sắc tự nhiên tiêu chuẩn"),
    PROTANOPIA("Protanopia", "Mù màu Đỏ (Protanopia)", "Tăng tương phát & chuyển tông vàng/xanh lam"),
    DEUTERANOPIA("Deuteranopia", "Mù màu Xanh Lục (Deuteranopia)", "Tối ưu hóa phân biệt Xanh lục - Đỏ"),
    TRITANOPIA("Tritanopia", "Mù màu Xanh Lam (Tritanopia)", "Tăng cường tương phản màu Xanh ngọc & Vàng"),
    HIGH_CONTRAST("High Contrast", "Tương Phản Cao", "Viền sáng đậm nét, nền tối sâu tối đa")
}

enum class FontScale(val scale: Float, val displayName: String, val vietnameseName: String) {
    STANDARD(1.0f, "Standard (100%)", "Tiêu chuẩn (100%)"),
    LARGE(1.15f, "Large (115%)", "Lớn (115%)"),
    EXTRA_LARGE(1.30f, "Extra Large (130%)", "Rất lớn (130%)")
}

enum class JoystickPosition(val displayName: String, val vietnameseName: String) {
    LEFT("Left-Handed (Left Bottom)", "Tay Trái (Góc Trái)"),
    RIGHT("Right-Handed (Right Bottom)", "Tay Phải (Góc Phải)")
}

enum class JoystickSize(val diameterDp: Int, val displayName: String, val vietnameseName: String) {
    SMALL(100, "Compact (100dp)", "Nhỏ gọn (100dp)"),
    MEDIUM(125, "Standard (125dp)", "Tiêu chuẩn (125dp)"),
    LARGE(155, "Spacious (155dp)", "Lớn rộng rãi (155dp)")
}

data class GameSettings(
    val colorblindMode: ColorblindMode = ColorblindMode.NORMAL,
    val fontScale: FontScale = FontScale.STANDARD,
    val joystickPosition: JoystickPosition = JoystickPosition.LEFT,
    val joystickSize: JoystickSize = JoystickSize.MEDIUM,
    val joystickSensitivity: Float = 1.0f, // 0.5f .. 2.0f
    val isBatterySaver: Boolean = false,   // 30 FPS cap, fewer particles
    val isHapticsEnabled: Boolean = true,
    val isAutoAimVisuals: Boolean = true
) {
    fun adjustColor(original: Color): Color {
        return when (colorblindMode) {
            ColorblindMode.NORMAL -> original
            ColorblindMode.PROTANOPIA -> {
                // Shift reds towards golden amber
                if (original.red > 0.6f && original.green < 0.5f) {
                    Color(0xFFFFB703) // Gold amber
                } else if (original.green > 0.6f && original.red < 0.5f) {
                    Color(0xFF219EBC) // Blue cyan
                } else original
            }
            ColorblindMode.DEUTERANOPIA -> {
                // Shift greens and reds for distinct perception
                if (original.green > 0.6f && original.blue < 0.5f) {
                    Color(0xFF00B4D8)
                } else if (original.red > 0.6f && original.green < 0.5f) {
                    Color(0xFFFFB703)
                } else original
            }
            ColorblindMode.TRITANOPIA -> {
                // Shift blues towards distinct purples and cyans
                if (original.blue > 0.6f && original.red < 0.5f) {
                    Color(0xFF9D4EDD) // Vibrant Purple
                } else original
            }
            ColorblindMode.HIGH_CONTRAST -> {
                // Boost lum / saturation
                if (original.red > 0.5f || original.green > 0.5f || original.blue > 0.5f) {
                    Color(0xFF00F0FF) // Ultra neon
                } else {
                    Color(0xFFFFFFFF)
                }
            }
        }
    }
}
