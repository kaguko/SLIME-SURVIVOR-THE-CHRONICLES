package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(val x: Float, val y: Float) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    
    fun length(): Float = sqrt(x * x + y * y)
    
    fun distanceTo(other: Vector2D): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }
    
    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.0001f) Vector2D(x / len, y / len) else Vector2D(0f, 0f)
    }
}

enum class EnemyType(
    val displayName: String,
    val maxHp: Float,
    val baseSpeed: Float,
    val touchDamage: Float,
    val xpReward: Int,
    val sizeRadius: Float,
    val isRedGem: Boolean = false,
    val isBoss: Boolean = false
) {
    SPORE_FUNGUS("Nấm Độc (Spore Fungus)", 30f, 50f, 6f, 5, 18f),
    FLAME_BAT("Dơi Lửa (Flame Bat)", 18f, 135f, 10f, 25, 14f, isRedGem = true),
    FOREST_GOLEM("Thú Rừng Golem", 150f, 40f, 16f, 40, 26f, isRedGem = true),
    OLD_TREE_ENT_BOSS("Thần Cây Già (Old Tree Ent)", 1600f, 28f, 25f, 250, 52f, isRedGem = true, isBoss = true)
}

data class Enemy(
    val id: Long,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var hp: Float,
    val maxHp: Float,
    var hurtTimer: Float = 0f,
    var walkPhase: Float = 0f,
    var flapPhase: Float = 0f,
    var isDead: Boolean = false
)

data class TrailPoint(
    val x: Float,
    val y: Float,
    var alpha: Float = 0.5f,
    val radius: Float = 14f
)

data class XpGem(
    val id: Long,
    var x: Float,
    var y: Float,
    val value: Int,
    val isRed: Boolean,
    val isGold: Boolean = false,
    var isPulled: Boolean = false,
    var pullSpeed: Float = 0f
)

data class DamageNumber(
    val id: Long,
    var x: Float,
    var y: Float,
    val damage: Int,
    val color: Color,
    var alpha: Float = 1f,
    var life: Float = 0.7f,
    val isCrit: Boolean = false
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var life: Float,
    val maxLife: Float
)

data class LightningEffect(
    val id: Long,
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    var progress: Float = 1f,
    val branches: List<Pair<Float, Float>> = emptyList()
)

data class SpinningAxe(
    var angle: Float,
    var distance: Float,
    var active: Boolean = true
)

data class GroundPuddle(
    val id: Long,
    val x: Float,
    val y: Float,
    val radius: Float,
    var durationRemaining: Float,
    val maxDuration: Float = 4f
)

enum class SkillId {
    CHAIN_LIGHTNING,
    FIRE_ORBIT,
    SPINNING_AXE,
    HOLY_PUDDLE,
    SPEED_BOOTS,
    MAGNET_RING,
    MAX_HP_BOOST,
    REGENERATION
}

data class SkillCardOption(
    val skillId: SkillId,
    val currentLevel: Int,
    val targetLevel: Int,
    val title: String,
    val vietnameseTitle: String,
    val description: String,
    val iconName: String,
    val isNew: Boolean
)
