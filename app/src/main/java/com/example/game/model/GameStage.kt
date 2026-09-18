package com.example.game.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.*

enum class GameStage(
    val id: String,
    val vietnameseName: String,
    val englishName: String,
    val subtitle: String,
    val description: String,
    val floorDarkColor: Color,
    val floorTileColor: Color,
    val floorAccentColor: Color,
    val primaryParticleColor: Color,
    val unlockGoldCost: Int,
    val bossType: EnemyType,
    val goldMultiplier: Float,
    val xpMultiplier: Float,
    @DrawableRes val bannerRes: Int
) {
    ENCHANTED_FOREST(
        id = "forest",
        vietnameseName = "Rừng Dạ Nguyệt",
        englishName = "Enchanted Night Forest",
        subtitle = "Ải 1: Cội Nguồn Sinh Mệnh",
        description = "Khu rừng ma thuật tĩnh mịch ban đêm với nấm độc và dơi lửa bay lượn.",
        floorDarkColor = Color(0xFF0D1B14),
        floorTileColor = Color(0xFF142C20),
        floorAccentColor = Color(0xFF1C3D2D),
        primaryParticleColor = EntGlowGreen,
        unlockGoldCost = 0,
        bossType = EnemyType.OLD_TREE_ENT_BOSS,
        goldMultiplier = 1.0f,
        xpMultiplier = 1.0f,
        bannerRes = R.drawable.img_game_hero_1789657413633
    ),
    MAGMA_CORE(
        id = "magma",
        vietnameseName = "Lõi Núi Lửa Dung Nham",
        englishName = "Magma Core Volcano",
        subtitle = "Ải 2: Biển Lửa Rực Cháy",
        description = "Hang dung nham sôi sục với Cua Lửa và Đầu Lâu Quỷ bốc cháy dữ dội.",
        floorDarkColor = Color(0xFF1C0A05),
        floorTileColor = Color(0xFF2E110A),
        floorAccentColor = Color(0xFF4A180E),
        primaryParticleColor = NeonFireOrange,
        unlockGoldCost = 300,
        bossType = EnemyType.INFERNAL_DRAGON_BOSS,
        goldMultiplier = 1.35f,
        xpMultiplier = 1.25f,
        bannerRes = R.drawable.img_stage_magma_1789702166677
    ),
    GLACIAL_FROST(
        id = "frost",
        vietnameseName = "Hầm Ngục Băng Giá",
        englishName = "Glacial Frost Crypt",
        subtitle = "Ải 3: Miền Đất Băng Tuyết",
        description = "Lâu đài băng vĩnh cửu bao phủ bởi Tiểu Quỷ Băng và Người Tuyết Khổng Lồ.",
        floorDarkColor = Color(0xFF091624),
        floorTileColor = Color(0xFF10273F),
        floorAccentColor = Color(0xFF183B5E),
        primaryParticleColor = NeonCyan,
        unlockGoldCost = 800,
        bossType = EnemyType.FROST_LICH_BOSS,
        goldMultiplier = 1.7f,
        xpMultiplier = 1.5f,
        bannerRes = R.drawable.img_stage_frost_1789702183656
    ),
    GOLDEN_TOMB(
        id = "tomb",
        vietnameseName = "Lăng Mộ Hoàng Kim",
        englishName = "Golden Pharaoh Tomb",
        subtitle = "Ải 4: Kho Báu Pharaoh Cổ Đại",
        description = "Lăng mộ ngập tràn vàng bạc với Xác Ướp Cổ và Bọ Cánh Cứng Sa Mạc.",
        floorDarkColor = Color(0xFF1E1705),
        floorTileColor = Color(0xFF332709),
        floorAccentColor = Color(0xFF4C3B0E),
        primaryParticleColor = PixelGold,
        unlockGoldCost = 1500,
        bossType = EnemyType.PHARAOH_KING_BOSS,
        goldMultiplier = 2.5f,
        xpMultiplier = 2.0f,
        bannerRes = R.drawable.img_stage_tomb_1789702198314
    )
}
