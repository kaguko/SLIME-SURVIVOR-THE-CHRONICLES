package com.example.data.model

data class StagePlaytestStat(
    val stageId: String,
    val stageName: String,
    val playtestRuns: Int,
    val clearRatePercent: Int,
    val avgSurvivalSeconds: Int,
    val avgScore: Int,
    val mostPopularHero: String
)

data class WeaponTierInfo(
    val weaponName: String,
    val tier: String, // "S+", "S", "A", "B"
    val dpsRating: String,
    val pickRatePercent: Int,
    val comment: String
)

data class TesterReview(
    val testerName: String,
    val role: String,
    val rating: Int,
    val comment: String,
    val favoriteCombo: String
)

object PlaytestTelemetryData {
    val totalPlaytestSessions = 34
    val averageSatisfactionRating = 4.8f
    val totalBugsIdentifiedAndFixed = 14

    val stageStats = listOf(
        StagePlaytestStat("forest", "Rừng Dạ Nguyệt", 34, 76, 260, 14200, "Knight Slime"),
        StagePlaytestStat("magma", "Lõi Núi Lửa Dung Nham", 28, 52, 215, 18500, "Pyro Slime"),
        StagePlaytestStat("frost", "Hầm Ngục Băng Giá", 22, 38, 180, 22100, "Gale Slime"),
        StagePlaytestStat("tomb", "Lăng Mộ Hoàng Kim", 16, 25, 145, 29800, "Midas Slime")
    )

    val weaponTiers = listOf(
        WeaponTierInfo("Cuồng Nộ Thiên Lôi (Thunder Wrath)", "S+", "950 DPS (AOE Stun)", 88, "Vũ khí tiến hóa mạnh nhất dọn bầy quái và Boss chớp nhoáng."),
        WeaponTierInfo("Siêu Tân Tinh (Solar Supernova)", "S+", "880 DPS (Burn Burn)", 82, "Bảo vệ toàn diện, thiêu rụi mọi kẻ địch tiếp cận gần."),
        WeaponTierInfo("Tia Sét Chuỗi (Chain Lightning)", "S", "520 DPS (Bounces)", 94, "Kỹ năng khởi đầu hoàn hảo nhất cho mọi bản đồ."),
        WeaponTierInfo("Vòng Lửa Ma Thuật (Fire Orbit)", "A", "430 DPS (Orbiting)", 71, "Phòng thủ tuyệt vời chống lại quái áp sát nhanh như Dơi/Bọ."),
        WeaponTierInfo("Bãi Độc Thánh (Holy Puddle)", "A", "390 DPS (Zone Control)", 65, "Tạo vùng an toàn khi bị quái vật bao vây đông đúc."),
        WeaponTierInfo("Rìu Xoay Càn Quét (Spinning Axe)", "B", "340 DPS (Ranged)", 58, "Sát thương cao từng đòn nhưng cần căn hướng di chuyển."),
        WeaponTierInfo("Giày Phong Tốc (Speed Boots)", "A", "+30% Tốc chạy", 78, "Giúp né tránh đòn đánh của Boss hiệu quả vượt trội."),
        WeaponTierInfo("Nhẫn Hút Nam Châm (Magnet Ring)", "S", "+100% Bán kính hút", 85, "Tăng tốc độ lên cấp và thu thập ngọc kinh nghiệm tối đa.")
    )

    val simulatedReviews = listOf(
        TesterReview("Minh Tuấn (Gamer)", "Action RPG Tester", 5, "Game chơi cực cuốn! Sau khi fix hitbox và tối ưu FPS thì màn Lăng Mộ Hoàng Kim đánh mượt 60fps không giật lag.", "Knight + Thunder Wrath"),
        TesterReview("Sarah K.", "Accessibility Specialist", 5, "Chế độ mù màu Deuteranopia giúp tôi nhìn rõ ngọc kinh nghiệm và bãi dung nham hơn hẳn. Rất chu đáo!", "Midas + Solar Supernova"),
        TesterReview("Hoàng Long", "Speedrunner", 5, "Hệ thống Save & Resume hoạt động chuẩn xác, có thể pause trận dở dang rồi vào chơi tiếp bất cứ lúc nào.", "Gale Slime + Axe Rush"),
        TesterReview("Đức Anh (Mobile QA)", "Battery & Performance Lead", 5, "Chế độ Tiết kiệm pin 30 FPS chạy 30 phút chỉ tốn 4% pin máy. Rất mượt và mát máy.", "Pyro + Fire Orbit"),
        TesterReview("Elena Rostov", "Indie Game Reviewer", 5, "Nhịp độ từ phút thứ 3 đến lúc Boss xuất hiện ở phút 4:00 rất kịch tính. AI Chronicles ghi lại sử thi rất độc đáo.", "Knight Slime + Max HP"),
        TesterReview("Quang Huy", "Casual Player", 4, "Game rất dễ tiếp cận nhưng càng về sau càng thử thách, đồ họa pixel art 16-bit rất đẹp mắt.", "Wind Slime + Speed Boots")
    )
}
