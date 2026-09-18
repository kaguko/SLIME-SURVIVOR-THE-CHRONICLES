package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    VIETNAMESE("vi", "Tiếng Việt", "🇻🇳"),
    ENGLISH("en", "English", "🇺🇸")
}

object Localization {
    private val _currentLanguage = MutableStateFlow(AppLanguage.VIETNAMESE)
    val currentLanguage = _currentLanguage.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun isVietnamese(): Boolean = _currentLanguage.value == AppLanguage.VIETNAMESE

    fun get(key: String): String {
        val isVi = isVietnamese()
        return when (key) {
            "app_title" -> if (isVi) "SLIME SURVIVOR" else "SLIME SURVIVOR"
            "app_subtitle" -> if (isVi) "BIÊN NIÊN SỬ RỪNG MA THUẬT" else "THE ENCHANTED CHRONICLES"
            "start_run" -> if (isVi) "VÀO TRẬN CHIẾN" else "START SURVIVAL RUN"
            "select_hero" -> if (isVi) "Chọn Hiệp Sĩ Slime" else "Select Slime Hero"
            "meta_shop" -> if (isVi) "Tiệm Cường Hóa Vĩnh Cửu" else "Permanent Talent Tree"
            "chronicles" -> if (isVi) "Biên Niên Sử & Bảng Vàng" else "Chronicles & Leaderboard"
            "bestiary" -> if (isVi) "Sổ Tay Quái & Kỹ Năng" else "Monster Bestiary & Guide"
            "store_monetization" -> if (isVi) "Kho Báu & Gói Hỗ Trợ" else "Treasury & Supporter Pack"
            "skin_forge" -> if (isVi) "Lò Rèn Ngoại Trang AI" else "AI Hero Skin Studio"
            "settings" -> if (isVi) "Cài Đặt & Chính Sách" else "Settings & Compliance"
            "best_time" -> if (isVi) "KỶ LỤC SỐNG SÓT" else "BEST SURVIVAL"
            "total_kills" -> if (isVi) "TỔNG QUÁI DIỆT" else "TOTAL KILLS"
            "gold" -> if (isVi) "Vàng" else "Gold"
            "revive" -> if (isVi) "Hồi Sinh Bằng Khiên Hào Quang" else "Revive with Radiant Shield"
            "revive_desc" -> if (isVi) "Hồi 60% HP & Bất tử 3 giây!" else "Restore 60% HP & 3s Invincible!"
            "game_over_title" -> if (isVi) "💀 BẠN ĐÃ HI SINH! 💀" else "💀 DEFEATED! 💀"
            "victory_title" -> if (isVi) "🏆 CHIẾN THẮNG HUY HOÀNG! 🏆" else "🏆 GLORIOUS VICTORY! 🏆"
            "play_again" -> if (isVi) "CHƠI LẠI TRẬN MỚI" else "PLAY AGAIN"
            "main_menu" -> if (isVi) "Về Trang Chủ" else "Back to Menu"
            "privacy_policy" -> if (isVi) "Chính Sách Quyền Riêng Tư" else "Privacy Policy & Age Rating"
            "ask_sage" -> if (isVi) "LỜI KHUYÊN THẦN RỪNG (AI SAGE)" else "FOREST SAGE TACTICAL ADVICE"
            "level_up" -> if (isVi) "⚡ THĂNG CẤP! (LEVEL UP) ⚡" else "⚡ LEVEL UP! ⚡"
            else -> key
        }
    }
}
