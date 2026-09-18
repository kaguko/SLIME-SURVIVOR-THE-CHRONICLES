package com.example.ui.game

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.game.engine.GameState
import com.example.game.model.*
import com.example.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

@Composable
fun GameCanvas(
    state: GameState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val screenW = size.width
        val screenH = size.height
        val centerX = screenW / 2f
        val centerY = screenH / 2f

        val shakeOffset = if (state.screenShake > 0.01f) {
            val s = state.screenShake
            Offset(Random.nextFloat() * s * 2f - s, Random.nextFloat() * s * 2f - s)
        } else Offset.Zero

        val camX = state.playerX - centerX + shakeOffset.x
        val camY = state.playerY - centerY + shakeOffset.y

        // 1. Draw Stage Floor (Dynamic Tile Theme)
        drawStageFloorBackground(state.selectedStage, camX, camY, screenW, screenH)

        // 2. Toxic / Holy Puddles
        state.puddles.forEach { pud ->
            val px = pud.x - camX
            val py = pud.y - camY
            val alpha = (pud.durationRemaining / pud.maxDuration).coerceIn(0.2f, 0.7f)
            drawCircle(
                color = SporePurple.copy(alpha = alpha * 0.4f),
                radius = pud.radius,
                center = Offset(px, py)
            )
            drawCircle(
                color = NeonCyan.copy(alpha = alpha * 0.8f),
                radius = pud.radius * 0.7f,
                center = Offset(px, py),
                style = Stroke(width = 3f)
            )
        }

        // 3. Slime Water Trails
        state.trails.forEach { tr ->
            val tx = tr.x - camX
            val ty = tr.y - camY
            drawCircle(
                color = state.selectedHero.color.copy(alpha = tr.alpha * 0.6f),
                radius = tr.radius,
                center = Offset(tx, ty)
            )
        }

        // 4. XP Gems
        state.gems.forEach { gem ->
            val gx = gem.x - camX
            val gy = gem.y - camY
            if (gx in -50f..(screenW + 50f) && gy in -50f..(screenH + 50f)) {
                drawXpGem(gx, gy, gem.isRed, gem.isGold)
            }
        }

        // 5. Gold Coins & Chests
        state.goldCoins.forEach { coin ->
            val cx = coin.x - camX
            val cy = coin.y - camY
            if (cx in -50f..(screenW + 50f) && cy in -50f..(screenH + 50f)) {
                drawCircle(color = PixelGold.copy(alpha = 0.35f), radius = 14f, center = Offset(cx, cy))
                drawCircle(color = PixelGold, radius = 8f, center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = 3f, center = Offset(cx - 1f, cy - 2f))
            }
        }

        state.chests.forEach { chest ->
            val chx = chest.x - camX
            val chy = chest.y - camY
            if (chx in -60f..(screenW + 60f) && chy in -60f..(screenH + 60f)) {
                drawTreasureChest(chx, chy)
            }
        }

        // 6. Enemies
        state.enemies.forEach { enemy ->
            val ex = enemy.x - camX
            val ey = enemy.y - camY
            if (ex in -160f..(screenW + 160f) && ey in -160f..(screenH + 160f)) {
                drawStageEnemy(enemy, ex, ey)
            }
        }

        // 7. Player Hero
        val px = state.playerX - camX
        val py = state.playerY - camY
        drawPlayerHero(state, px, py)

        // 8. Radiant Invincibility Shield
        if (state.invincibleTimer > 0f) {
            val shieldPulse = (sin(state.totalTimeSurvived * 12f) * 4f).toFloat()
            drawCircle(
                color = PixelGold.copy(alpha = 0.3f),
                radius = 48f + shieldPulse,
                center = Offset(px, py)
            )
            drawCircle(
                color = NeonCyan,
                radius = 48f + shieldPulse,
                center = Offset(px, py),
                style = Stroke(width = 3.5f)
            )
        }

        // 9. Fire Orbit / Solar Supernova
        if (state.fireOrbCount > 0) {
            val isSuper = (state.skillLevels[SkillId.SOLAR_SUPERNOVA] ?: 0) > 0
            val fireRadius = 65f + (state.skillLevels[SkillId.FIRE_ORBIT] ?: 1) * 10f
            for (f in 0 until state.fireOrbCount) {
                val orbAngle = state.fireOrbitAngle + (f * (2 * Math.PI.toFloat() / state.fireOrbCount))
                val ox = px + cos(orbAngle) * fireRadius
                val oy = py + sin(orbAngle) * fireRadius

                val orbSize = if (isSuper) 22f else 16f
                drawCircle(color = NeonFireOrange.copy(alpha = 0.45f), radius = orbSize, center = Offset(ox, oy))
                drawCircle(color = NeonFireYellow, radius = orbSize * 0.6f, center = Offset(ox, oy))
                drawCircle(color = Color.White, radius = orbSize * 0.3f, center = Offset(ox, oy))
            }
        }

        // 10. Spinning Axes
        state.spinningAxes.forEach { axe ->
            val ax = state.playerX + cos(axe.angle) * axe.distance - camX
            val ay = state.playerY + sin(axe.angle) * axe.distance - camY
            drawSpinningAxe(ax, ay, axe.angle)
        }

        // 11. Chain Lightning Strikes
        state.lightnings.forEach { l ->
            val sx = l.startX - camX
            val sy = l.startY - camY
            val tx = l.targetX - camX
            val ty = l.targetY - camY

            val path = Path().apply {
                moveTo(sx, sy)
                l.branches.forEach { (bx, by) ->
                    lineTo(bx - camX, by - camY)
                }
                lineTo(tx, ty)
            }

            drawPath(path = path, color = NeonCyan.copy(alpha = l.progress), style = Stroke(width = 8f))
            drawPath(path = path, color = Color.White.copy(alpha = l.progress), style = Stroke(width = 3.5f))
        }

        // 12. Particles
        state.particles.forEach { p ->
            val ppx = p.x - camX
            val ppy = p.y - camY
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size * alpha,
                center = Offset(ppx, ppy)
            )
        }

        // 13. Floating Damage Numbers
        state.damageNumbers.forEach { dmg ->
            val dx = dmg.x - camX
            val dy = dmg.y - camY
            drawContext.canvas.nativeCanvas.apply {
                val paint = Paint().apply {
                    color = dmg.color.toArgb()
                    this.alpha = (dmg.alpha * 255).toInt().coerceIn(0, 255)
                    textSize = if (dmg.isCrit) 36f else 26f
                    isFakeBoldText = true
                    setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                }
                drawText("${dmg.damage}", dx, dy, paint)
            }
        }
    }
}

private fun DrawScope.drawStageFloorBackground(
    stage: GameStage,
    camX: Float,
    camY: Float,
    w: Float,
    h: Float
) {
    drawRect(color = stage.floorDarkColor, size = Size(w, h))

    val tileSize = 96f
    val startCol = floor(camX / tileSize).toInt()
    val endCol = ceil((camX + w) / tileSize).toInt()
    val startRow = floor(camY / tileSize).toInt()
    val endRow = ceil((camY + h) / tileSize).toInt()

    for (col in startCol..endCol) {
        for (row in startRow..endRow) {
            val tileX = col * tileSize - camX
            val tileY = row * tileSize - camY

            val hash = abs((col * 73856093) xor (row * 19349663))
            val tileColor = if (hash % 5 == 0) stage.floorAccentColor else stage.floorTileColor

            drawRect(
                color = tileColor,
                topLeft = Offset(tileX + 2f, tileY + 2f),
                size = Size(tileSize - 4f, tileSize - 4f)
            )

            if (hash % 11 == 0) {
                drawCircle(
                    color = stage.primaryParticleColor.copy(alpha = 0.35f),
                    radius = 8f,
                    center = Offset(tileX + tileSize * 0.5f, tileY + tileSize * 0.5f)
                )
            }
        }
    }
}

private fun DrawScope.drawStageEnemy(enemy: Enemy, ex: Float, ey: Float) {
    val hurt = enemy.hurtTimer > 0f

    when (enemy.type) {
        EnemyType.SPORE_FUNGUS -> {
            val waddle = sin(enemy.walkPhase) * 4f
            val capColor = if (hurt) HealthRed else Color(0xFF7B2CBF)
            drawRoundRect(
                color = Color(0xFFE0AAFF),
                topLeft = Offset(ex - 8f + waddle, ey - 6f),
                size = Size(16f, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            drawArc(
                color = capColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(ex - 22f + waddle, ey - 26f),
                size = Size(44f, 32f)
            )
        }

        EnemyType.FLAME_BAT -> {
            val flap = sin(enemy.flapPhase) * 14f
            val batColor = if (hurt) Color.White else HealthRed
            val leftWing = Path().apply {
                moveTo(ex, ey)
                lineTo(ex - 24f, ey - 12f + flap)
                lineTo(ex - 10f, ey + 6f)
                close()
            }
            val rightWing = Path().apply {
                moveTo(ex, ey)
                lineTo(ex + 24f, ey - 12f + flap)
                lineTo(ex + 10f, ey + 6f)
                close()
            }
            drawPath(leftWing, color = batColor)
            drawPath(rightWing, color = batColor)
            drawOval(color = Color(0xFF590D22), topLeft = Offset(ex - 8f, ey - 8f), size = Size(16f, 18f))
        }

        EnemyType.MAGMA_CRAB -> {
            val waddle = sin(enemy.walkPhase) * 3f
            val crabColor = if (hurt) Color.White else NeonFireOrange
            drawOval(color = crabColor, topLeft = Offset(ex - 18f + waddle, ey - 12f), size = Size(36f, 24f))
            drawCircle(color = NeonFireYellow, radius = 4f, center = Offset(ex - 8f + waddle, ey - 6f))
            drawCircle(color = NeonFireYellow, radius = 4f, center = Offset(ex + 8f + waddle, ey - 6f))
        }

        EnemyType.LAVA_SKULL -> {
            val floatOff = sin(enemy.walkPhase) * 6f
            val skullColor = if (hurt) Color.White else Color(0xFFFFD166)
            drawCircle(color = skullColor, radius = 14f, center = Offset(ex, ey + floatOff))
            drawCircle(color = HealthRed, radius = 4f, center = Offset(ex - 5f, ey - 2f + floatOff))
            drawCircle(color = HealthRed, radius = 4f, center = Offset(ex + 5f, ey - 2f + floatOff))
        }

        EnemyType.FROST_IMP -> {
            val flap = sin(enemy.flapPhase) * 10f
            val impColor = if (hurt) Color.White else NeonCyan
            drawCircle(color = impColor, radius = 12f, center = Offset(ex, ey))
            drawCircle(color = Color(0xFF03045E), radius = 3f, center = Offset(ex - 4f, ey - 2f))
            drawCircle(color = Color(0xFF03045E), radius = 3f, center = Offset(ex + 4f, ey - 2f))
        }

        EnemyType.ICE_GOLEM -> {
            val golemColor = if (hurt) Color.White else Color(0xFF90E0EF)
            drawRoundRect(
                color = golemColor,
                topLeft = Offset(ex - 26f, ey - 34f),
                size = Size(52f, 58f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )
            drawRect(color = Color(0xFF0077B6), topLeft = Offset(ex - 14f, ey - 18f), size = Size(28f, 6f))
        }

        EnemyType.TOMB_MUMMY -> {
            val waddle = sin(enemy.walkPhase) * 4f
            val mummyColor = if (hurt) Color.White else Color(0xFFD4A373)
            drawRoundRect(
                color = mummyColor,
                topLeft = Offset(ex - 16f + waddle, ey - 22f),
                size = Size(32f, 44f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            drawRect(color = PixelGold, topLeft = Offset(ex - 12f + waddle, ey - 10f), size = Size(24f, 4f))
        }

        EnemyType.ANUBIS_SCARAB -> {
            val scarabColor = if (hurt) Color.White else PixelGold
            drawOval(color = scarabColor, topLeft = Offset(ex - 14f, ey - 10f), size = Size(28f, 20f))
            drawCircle(color = Color(0xFF7209B7), radius = 4f, center = Offset(ex, ey))
        }

        EnemyType.VOID_SPECTER -> {
            val floatOff = sin(enemy.walkPhase) * 5f
            val specterColor = if (hurt) Color.White else SporePurple
            drawOval(color = specterColor, topLeft = Offset(ex - 18f, ey - 24f + floatOff), size = Size(36f, 48f))
            drawCircle(color = NeonCyan, radius = 5f, center = Offset(ex - 6f, ey - 10f + floatOff))
            drawCircle(color = NeonCyan, radius = 5f, center = Offset(ex + 6f, ey - 10f + floatOff))
        }

        EnemyType.FOREST_GOLEM -> {
            val golemColor = if (hurt) Color.White else EntWoodBrown
            drawRoundRect(
                color = golemColor,
                topLeft = Offset(ex - 24f, ey - 32f),
                size = Size(48f, 54f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
        }

        // BOSS 1: Old Tree Ent
        EnemyType.OLD_TREE_ENT_BOSS -> {
            val entColor = if (hurt) Color.White else EntWoodBrown
            val waddle = sin(enemy.walkPhase) * 6f
            drawRoundRect(
                color = entColor,
                topLeft = Offset(ex - 52f + waddle, ey - 75f),
                size = Size(104f, 130f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            )
            drawCircle(color = EntGlowGreen, radius = 10f, center = Offset(ex - 20f + waddle, ey - 25f))
            drawCircle(color = EntGlowGreen, radius = 10f, center = Offset(ex + 20f + waddle, ey - 25f))
        }

        // BOSS 2: Infernal Dragon
        EnemyType.INFERNAL_DRAGON_BOSS -> {
            val dragColor = if (hurt) Color.White else Color(0xFF9E2A2B)
            val flap = sin(enemy.flapPhase) * 20f
            // Wings
            val lw = Path().apply {
                moveTo(ex, ey - 20f)
                lineTo(ex - 65f, ey - 55f + flap)
                lineTo(ex - 35f, ey + 20f)
                close()
            }
            val rw = Path().apply {
                moveTo(ex, ey - 20f)
                lineTo(ex + 65f, ey - 55f + flap)
                lineTo(ex + 35f, ey + 20f)
                close()
            }
            drawPath(lw, color = NeonFireOrange)
            drawPath(rw, color = NeonFireOrange)
            // Body & Head
            drawOval(color = dragColor, topLeft = Offset(ex - 35f, ey - 50f), size = Size(70f, 90f))
            drawCircle(color = NeonFireYellow, radius = 9f, center = Offset(ex - 14f, ey - 30f))
            drawCircle(color = NeonFireYellow, radius = 9f, center = Offset(ex + 14f, ey - 30f))
        }

        // BOSS 3: Frost Lich
        EnemyType.FROST_LICH_BOSS -> {
            val lichColor = if (hurt) Color.White else Color(0xFF03045E)
            val floatOff = sin(enemy.walkPhase) * 8f
            drawRoundRect(
                color = lichColor,
                topLeft = Offset(ex - 36f, ey - 60f + floatOff),
                size = Size(72f, 110f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            )
            drawCircle(color = Color.White, radius = 22f, center = Offset(ex, ey - 45f + floatOff))
            drawCircle(color = NeonCyan, radius = 8f, center = Offset(ex - 8f, ey - 45f + floatOff))
            drawCircle(color = NeonCyan, radius = 8f, center = Offset(ex + 8f, ey - 45f + floatOff))
        }

        // BOSS 4: Pharaoh King
        EnemyType.PHARAOH_KING_BOSS -> {
            val pharaohColor = if (hurt) Color.White else PixelGold
            val waddle = sin(enemy.walkPhase) * 6f
            drawRoundRect(
                color = Color(0xFF582F0E),
                topLeft = Offset(ex - 42f + waddle, ey - 65f),
                size = Size(84f, 120f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
            )
            // Headdress
            val nemes = Path().apply {
                moveTo(ex - 48f + waddle, ey - 70f)
                lineTo(ex + 48f + waddle, ey - 70f)
                lineTo(ex + 36f + waddle, ey - 20f)
                lineTo(ex - 36f + waddle, ey - 20f)
                close()
            }
            drawPath(nemes, color = pharaohColor)
            drawCircle(color = HealthRed, radius = 8f, center = Offset(ex - 15f + waddle, ey - 35f))
            drawCircle(color = HealthRed, radius = 8f, center = Offset(ex + 15f + waddle, ey - 35f))
        }
    }

    if (enemy.hp < enemy.maxHp && !enemy.type.isBoss) {
        val barW = enemy.type.sizeRadius * 1.8f
        val barH = 5f
        val hpPct = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
        val barX = ex - barW / 2f
        val barY = ey - enemy.type.sizeRadius - 12f

        drawRect(color = Color.Black, topLeft = Offset(barX, barY), size = Size(barW, barH))
        drawRect(color = HealthRed, topLeft = Offset(barX, barY), size = Size(barW * hpPct, barH))
    }
}

private fun DrawScope.drawPlayerHero(state: GameState, px: Float, py: Float) {
    val isMoving = state.isMoving
    val walkTime = state.playerWalkTime

    val squashX = if (isMoving) 1f + sin(walkTime) * 0.18f else 1f + sin(walkTime) * 0.06f
    val squashY = if (isMoving) 1f - sin(walkTime) * 0.18f else 1f - sin(walkTime) * 0.06f

    val baseW = 38f * squashX
    val baseH = 32f * squashY
    val hurtTint = state.playerHurtTimer > 0f

    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(px - baseW * 0.9f, py + baseH * 0.6f),
        size = Size(baseW * 1.8f, 14f)
    )

    val heroColor = state.selectedHero.color
    drawOval(
        color = if (hurtTint) HealthRed.copy(alpha = 0.5f) else heroColor.copy(alpha = 0.3f),
        topLeft = Offset(px - baseW - 6f, py - baseH - 6f),
        size = Size((baseW + 6f) * 2f, (baseH + 6f) * 2f)
    )

    val bodyColor = if (hurtTint) HealthRed else heroColor
    drawOval(
        color = bodyColor,
        topLeft = Offset(px - baseW, py - baseH),
        size = Size(baseW * 2f, baseH * 2f)
    )

    drawOval(
        color = Color.White.copy(alpha = 0.3f),
        topLeft = Offset(px - baseW * 0.7f, py - baseH * 0.8f),
        size = Size(baseW * 1.4f, baseH * 1.3f)
    )

    val eyeOffsetX = state.moveDirection.first * 4f
    val eyeOffsetY = state.moveDirection.second * 3f
    val leftEyeX = px - 9f + eyeOffsetX
    val rightEyeX = px + 9f + eyeOffsetX
    val eyeY = py - 2f + eyeOffsetY

    drawOval(color = Color(0xFF0B132B), topLeft = Offset(leftEyeX - 4f, eyeY - 6f), size = Size(8f, 12f))
    drawOval(color = Color(0xFF0B132B), topLeft = Offset(rightEyeX - 4f, eyeY - 6f), size = Size(8f, 12f))
    drawCircle(color = Color.White, radius = 2f, center = Offset(leftEyeX - 1f, eyeY - 3f))
    drawCircle(color = Color.White, radius = 2f, center = Offset(rightEyeX - 1f, eyeY - 3f))
}

private fun DrawScope.drawXpGem(gx: Float, gy: Float, isRed: Boolean, isGold: Boolean) {
    val gemColor = if (isGold) GemGold else if (isRed) GemRed else GemGreen
    val size = if (isGold) 12f else if (isRed) 9f else 7f

    val diamond = Path().apply {
        moveTo(gx, gy - size)
        lineTo(gx + size, gy)
        lineTo(gx, gy + size)
        lineTo(gx - size, gy)
        close()
    }

    drawCircle(color = gemColor.copy(alpha = 0.35f), radius = size * 1.8f, center = Offset(gx, gy))
    drawPath(path = diamond, color = gemColor)
    drawCircle(color = Color.White, radius = size * 0.35f, center = Offset(gx - 1f, gy - 2f))
}

private fun DrawScope.drawTreasureChest(chx: Float, chy: Float) {
    drawCircle(color = PixelGold.copy(alpha = 0.4f), radius = 28f, center = Offset(chx, chy))
    drawRoundRect(
        color = Color(0xFF6B4423),
        topLeft = Offset(chx - 18f, chy - 10f),
        size = Size(36f, 24f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRect(color = PixelGold, topLeft = Offset(chx - 4f, chy - 10f), size = Size(8f, 24f))
    drawCircle(color = Color.White, radius = 3f, center = Offset(chx, chy + 2f))
}

private fun DrawScope.drawSpinningAxe(ax: Float, ay: Float, angle: Float) {
    rotate(degrees = Math.toDegrees(angle.toDouble()).toFloat() * 2.5f, pivot = Offset(ax, ay)) {
        drawRect(
            color = EntWoodBrown,
            topLeft = Offset(ax - 3f, ay - 20f),
            size = Size(6f, 40f)
        )
        val leftBlade = Path().apply {
            moveTo(ax - 2f, ay - 18f)
            lineTo(ax - 18f, ay - 26f)
            lineTo(ax - 16f, ay - 2f)
            close()
        }
        val rightBlade = Path().apply {
            moveTo(ax + 2f, ay - 18f)
            lineTo(ax + 18f, ay - 26f)
            lineTo(ax + 16f, ay - 2f)
            close()
        }
        drawPath(leftBlade, color = PixelGold)
        drawPath(rightBlade, color = PixelGold)
        drawCircle(color = NeonCyan, radius = 4f, center = Offset(ax, ay - 14f))
    }
}
