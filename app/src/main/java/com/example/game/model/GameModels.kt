package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class EnemyType(
    val baseHp: Float,
    val baseSpeed: Float,
    val damage: Float,
    val xpValue: Int,
    val sizeRadius: Float,
    val isBoss: Boolean = false,
    val isElite: Boolean = false,
    val vietnameseName: String = ""
) {
    // Forest Mobs
    SPORE_FUNGUS(baseHp = 22f, baseSpeed = 55f, damage = 8f, xpValue = 1, sizeRadius = 14f, vietnameseName = "Nấm Độc Dạ Quang"),
    FLAME_BAT(baseHp = 14f, baseSpeed = 95f, damage = 6f, xpValue = 2, sizeRadius = 11f, vietnameseName = "Dơi Lửa Tốc Biến"),
    FOREST_GOLEM(baseHp = 140f, baseSpeed = 38f, damage = 18f, xpValue = 6, sizeRadius = 24f, isElite = true, vietnameseName = "Golem Rừng Xanh"),
    OLD_TREE_ENT_BOSS(baseHp = 1500f, baseSpeed = 42f, damage = 25f, xpValue = 35, sizeRadius = 45f, isBoss = true, vietnameseName = "Cây Cổ Thụ Ngàn Năm"),

    // Magma Mobs
    MAGMA_CRAB(baseHp = 35f, baseSpeed = 65f, damage = 12f, xpValue = 3, sizeRadius = 16f, vietnameseName = "Cua Dung Nham"),
    LAVA_SKULL(baseHp = 20f, baseSpeed = 110f, damage = 10f, xpValue = 3, sizeRadius = 13f, vietnameseName = "Đầu Lâu Quỷ Lửa"),
    INFERNAL_DRAGON_BOSS(baseHp = 2200f, baseSpeed = 48f, damage = 35f, xpValue = 50, sizeRadius = 50f, isBoss = true, vietnameseName = "Rồng Lửa Địa Ngục"),

    // Frost Mobs
    FROST_IMP(baseHp = 28f, baseSpeed = 85f, damage = 11f, xpValue = 3, sizeRadius = 13f, vietnameseName = "Tiểu Quỷ Băng Giá"),
    ICE_GOLEM(baseHp = 220f, baseSpeed = 35f, damage = 22f, xpValue = 8, sizeRadius = 26f, isElite = true, vietnameseName = "Người Tuyết Khổng Lồ"),
    FROST_LICH_BOSS(baseHp = 2800f, baseSpeed = 45f, damage = 40f, xpValue = 65, sizeRadius = 48f, isBoss = true, vietnameseName = "Phù Thủy Băng Vĩnh Cửu"),

    // Golden Tomb Mobs
    TOMB_MUMMY(baseHp = 45f, baseSpeed = 50f, damage = 15f, xpValue = 4, sizeRadius = 16f, vietnameseName = "Xác Ướp Hoàng Kim"),
    ANUBIS_SCARAB(baseHp = 18f, baseSpeed = 125f, damage = 12f, xpValue = 3, sizeRadius = 12f, vietnameseName = "Bọ Cánh Cứng Sa Mạc"),
    VOID_SPECTER(baseHp = 60f, baseSpeed = 75f, damage = 18f, xpValue = 5, sizeRadius = 18f, isElite = true, vietnameseName = "Bóng Ma Hư Không"),
    PHARAOH_KING_BOSS(baseHp = 3600f, baseSpeed = 52f, damage = 45f, xpValue = 100, sizeRadius = 52f, isBoss = true, vietnameseName = "Đại Vương Pharaoh Bất Tử")
}

data class Enemy(
    val id: Long,
    var x: Float,
    var y: Float,
    var hp: Float,
    var maxHp: Float,
    val type: EnemyType,
    var hurtTimer: Float = 0f,
    var walkPhase: Float = 0f,
    var flapPhase: Float = 0f
)

data class XpGem(
    val id: Long,
    var x: Float,
    var y: Float,
    val xpValue: Int,
    val isRed: Boolean = false,
    val isGold: Boolean = false
)

data class GoldCoin(
    val id: Long,
    var x: Float,
    var y: Float,
    val amount: Int
)

data class TreasureChest(
    val id: Long,
    var x: Float,
    var y: Float,
    val goldReward: Int
)

data class ToxicPuddle(
    val x: Float,
    val y: Float,
    val radius: Float = 55f,
    val damage: Float = 8f,
    var durationRemaining: Float = 3.5f,
    val maxDuration: Float = 3.5f
)

data class SlimeWaterTrail(
    val x: Float,
    val y: Float,
    val radius: Float = 14f,
    var alpha: Float = 0.5f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val color: Color
)

data class DamageNumber(
    val x: Float,
    var y: Float,
    val damage: Int,
    val color: Color = Color.White,
    val isCrit: Boolean = false,
    var life: Float = 0.6f
) {
    val alpha: Float get() = (life / 0.6f).coerceIn(0f, 1f)
}

data class LightningStrike(
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    val branches: List<Pair<Float, Float>> = emptyList(),
    var progress: Float = 1f
)

data class SpinningAxeEntity(
    var angle: Float,
    var distance: Float,
    val speed: Float = 5.5f,
    val damage: Float
)

data class BossRootHazard(
    val x: Float,
    val y: Float,
    val radius: Float = 40f,
    var warningTimer: Float = 1.0f,
    var activeTimer: Float = 0.6f,
    val damage: Float = 25f
)

enum class SkillId {
    CHAIN_LIGHTNING,
    FIRE_ORBIT,
    SPINNING_AXE,
    HOLY_PUDDLE,
    SPEED_BOOTS,
    MAGNET_RING,
    MAX_HP_BOOST,
    REGENERATION,
    THUNDER_WRATH,
    SOLAR_SUPERNOVA
}

data class SkillCardOption(
    val skillId: SkillId,
    val currentLevel: Int,
    val targetLevel: Int,
    val title: String,
    val vietnameseTitle: String,
    val description: String,
    val iconName: String,
    val isNew: Boolean = false,
    val isEvolution: Boolean = false
)

enum class SlimeHero(
    val id: String,
    val heroName: String,
    val vietnameseName: String,
    val title: String,
    val description: String,
    val baseHp: Float,
    val baseSpeed: Float,
    val starterSkill: SkillId,
    val color: Color,
    val unlockGoldCost: Int
) {
    KNIGHT_SLIME(
        id = "knight",
        heroName = "Knight Slime",
        vietnameseName = "Hiệp Sĩ Thạch",
        title = "Đội Trưởng Giáp Dày",
        description = "Máu trâu, khởi đầu với kỹ năng Tia Sét Chuỗi. Thích hợp cho người mới bắt đầu.",
        baseHp = 120f,
        baseSpeed = 160f,
        starterSkill = SkillId.CHAIN_LIGHTNING,
        color = Color(0xFF48CAE4),
        unlockGoldCost = 0
    ),
    FIRE_SLIME(
        id = "pyro",
        heroName = "Pyro Slime",
        vietnameseName = "Thạch Lửa Đỏ",
        title = "Chúa Tể Lửa Đỏ",
        description = "Gây thêm 30% sát thương bốc cháy, bắt đầu với Vòng Lửa Bảo Vệ.",
        baseHp = 90f,
        baseSpeed = 175f,
        starterSkill = SkillId.FIRE_ORBIT,
        color = Color(0xFFFF6B6B),
        unlockGoldCost = 250
    ),
    WIND_SLIME(
        id = "gale",
        heroName = "Gale Slime",
        vietnameseName = "Thạch Phong Thần",
        title = "Bóng Ma Gió Lốc",
        description = "Tốc độ di chuyển cực nhanh, bắt đầu với Rìu Xoay Càn Quét.",
        baseHp = 80f,
        baseSpeed = 210f,
        starterSkill = SkillId.SPINNING_AXE,
        color = Color(0xFF64DFDF),
        unlockGoldCost = 500
    ),
    GOLDEN_SLIME(
        id = "midas",
        heroName = "Midas Slime",
        vietnameseName = "Thạch Hoàng Kim",
        title = "Thần Tài Phú Quý",
        description = "Nhặt thêm 50% tiền vàng trong mỗi trận chiến, bắt đầu với Bãi Độc Thánh.",
        baseHp = 100f,
        baseSpeed = 165f,
        starterSkill = SkillId.HOLY_PUDDLE,
        color = Color(0xFFFFD166),
        unlockGoldCost = 1000
    )
}

enum class MetaUpgradeType(
    val id: String,
    val title: String,
    val vietnameseTitle: String,
    val description: String,
    val icon: String,
    val maxRank: Int,
    val baseCost: Int,
    val costMultiplier: Float
) {
    IRON_BODY("iron_body", "Iron Slime Body", "Thể Phách Thạch Kim", "+15 Max HP mỗi cấp vĩnh viễn", "🛡️", 5, 50, 1.8f),
    SWIFT_STEPS("swift_steps", "Swift Steps", "Bước Chân Phiêu Lãng", "+6% Tốc độ chạy mỗi cấp vĩnh viễn", "👟", 5, 50, 1.8f),
    MIGHTY_STRIKE("mighty_strike", "Mighty Strike", "Uy Lực Thần Lực", "+8% Sát thương toàn bộ vũ khí", "⚔️", 5, 75, 2.0f),
    GREED_RUNE("greed_rune", "Greed Rune", "Cổ Tự Hoàng Kim", "+20% Tiền vàng nhận được từ trận đánh", "🪙", 5, 60, 1.9f),
    MAGNET_PULL("magnet_pull", "Magnetic Core", "Lực Hút Nam Châm", "+25% Bán kính hút ngọc kinh nghiệm", "🧲", 5, 40, 1.7f),
    PHOENIX_FEATHER("phoenix_feather", "Phoenix Feather", "Lông Vũ Phượng Hoàng", "Tự động hồi sinh 1 lần miễn phí khi hi sinh", "🔥", 1, 300, 1.0f)
}
