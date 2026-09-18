package com.example.game.model

enum class AchievementId(
    val title: String,
    val vietnameseTitle: String,
    val description: String,
    val goldReward: Int,
    val icon: String
) {
    FIRST_BLOOD("First Blood", "Máu Đầu Tiên", "Tiêu diệt 50 quái vật đầu tiên", 50, "🩸"),
    MONSTER_SLAYER("Monster Slayer", "Thợ Săn Quái Thú", "Tiêu diệt 500 quái vật", 150, "⚔️"),
    GENOCIDE("Genocide", "Kẻ Hủy Diệt", "Tiêu diệt 2,000 quái vật", 500, "💀"),
    SURVIVOR_NOVICE("Survivor Novice", "Tập Sự Sinh Tồn", "Sống sót qua 2:00 phút", 50, "⏱️"),
    SURVIVAL_MASTER("Survival Master", "Bậc Thầy Sinh Tồn", "Sống sót đủ 5:00 phút và Chiến Thắng!", 300, "🏆"),
    TREE_ENT_SLAYER("Ent Nemesis", "Đốn Gục Cổ Thụ", "Đánh bại Đại Boss Cây Cổ Thụ ở Ải 1", 200, "🌲"),
    DRAGON_SLAYER("Dragon Bane", "Diệt Rồng Địa Ngục", "Đánh bại Rồng Lửa ở Ải Núi Lửa", 350, "🐉"),
    LICH_SLAYER("Lich Slayer", "Hóa Giải Băng Giá", "Đánh bại Phù Thủy Băng ở Ải Băng Tuyết", 450, "❄️"),
    PHARAOH_SLAYER("Pharaoh Nemesis", "Lăng Mộ Vua", "Đánh bại Vua Pharaoh ở Ải Lăng Mộ", 600, "👑"),
    EVOLUTION_MASTER("Evolution Master", "Tiến Hóa Tối Thượng", "Tiến hóa thành công 1 vũ khí thần thoại", 250, "⚡"),
    TREASURY_HOARDER("Gold Hoarder", "Phú Hộ Rừng Xanh", "Tích lũy tổng cộng 1,000 Tiền Vàng", 200, "💰"),
    HERO_COLLECTOR("Hero Collector", "Hội Hiệp Sĩ", "Mở khóa toàn bộ 4 Slime Heroes", 400, "🛡️"),
    RADIANT_REBIRTH("Radiant Rebirth", "Bất Tử Tái Sinh", "Hồi sinh thành công với Khiên Hào Quang", 100, "✨")
}

data class AchievementUiModel(
    val achievement: AchievementId,
    val isUnlocked: Boolean,
    val unlockedTimestamp: Long? = null
)
