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

        // Screen shake offsets
        val shakeOffset = if (state.screenShake > 0.01f) {
            val s = state.screenShake
            Offset(Random.nextFloat() * s * 2f - s, Random.nextFloat() * s * 2f - s)
        } else Offset.Zero

        val camX = state.playerX - centerX + shakeOffset.x
        val camY = state.playerY - centerY + shakeOffset.y

        // 1. Draw Enchanted Night Forest Grid Floor
        drawForestBackground(camX, camY, screenW, screenH)

        // 2. Draw Toxic / Holy Puddles
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

        // 3. Draw Slime Water Trails
        state.trails.forEach { tr ->
            val tx = tr.x - camX
            val ty = tr.y - camY
            drawCircle(
                color = SlimeBlueGlow.copy(alpha = tr.alpha),
                radius = tr.radius,
                center = Offset(tx, ty)
            )
        }

        // 4. Draw XP Gems
        state.gems.forEach { gem ->
            val gx = gem.x - camX
            val gy = gem.y - camY
            if (gx in -50f..(screenW + 50f) && gy in -50f..(screenH + 50f)) {
                drawXpGem(gx, gy, gem.isRed, gem.isGold)
            }
        }

        // 5. Draw Enemies
        state.enemies.forEach { enemy ->
            val ex = enemy.x - camX
            val ey = enemy.y - camY
            if (ex in -120f..(screenW + 120f) && ey in -120f..(screenH + 120f)) {
                drawEnemy(enemy, ex, ey)
            }
        }

        // 6. Draw Knight Slime
        val px = state.playerX - camX
        val py = state.playerY - camY
        drawKnightSlime(state, px, py)

        // 7. Draw Fire Orbit
        if (state.fireOrbCount > 0) {
            val fireRadius = 65f + (state.skillLevels[SkillId.FIRE_ORBIT] ?: 1) * 10f
            for (f in 0 until state.fireOrbCount) {
                val orbAngle = state.fireOrbitAngle + (f * (2 * Math.PI.toFloat() / state.fireOrbCount))
                val ox = px + cos(orbAngle) * fireRadius
                val oy = py + sin(orbAngle) * fireRadius

                // Fire outer glow
                drawCircle(
                    color = NeonFireOrange.copy(alpha = 0.45f),
                    radius = 16f,
                    center = Offset(ox, oy)
                )
                // Fire core
                drawCircle(
                    color = NeonFireYellow,
                    radius = 9f,
                    center = Offset(ox, oy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(ox, oy)
                )
            }
        }

        // 8. Draw Spinning Axes
        state.spinningAxes.forEach { axe ->
            val ax = state.playerX + cos(axe.angle) * axe.distance - camX
            val ay = state.playerY + sin(axe.angle) * axe.distance - camY
            drawSpinningAxe(ax, ay, axe.angle)
        }

        // 9. Draw Chain Lightning Strikes
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

            // Neon Outer Bolt
            drawPath(
                path = path,
                color = NeonCyan.copy(alpha = l.progress),
                style = Stroke(width = 8f)
            )
            // Core White Bolt
            drawPath(
                path = path,
                color = Color.White.copy(alpha = l.progress),
                style = Stroke(width = 3f)
            )
        }

        // 10. Draw Particles
        state.particles.forEach { p ->
            val px = p.x - camX
            val py = p.y - camY
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size * alpha,
                center = Offset(px, py)
            )
        }

        // 11. Draw Floating Damage Numbers
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

private fun DrawScope.drawForestBackground(camX: Float, camY: Float, w: Float, h: Float) {
    // Solid deep dark forest base
    drawRect(color = ForestNightDark, size = Size(w, h))

    val tileSize = 96f
    val startCol = floor(camX / tileSize).toInt()
    val endCol = ceil((camX + w) / tileSize).toInt()
    val startRow = floor(camY / tileSize).toInt()
    val endRow = ceil((camY + h) / tileSize).toInt()

    for (col in startCol..endCol) {
        for (row in startRow..endRow) {
            val tileX = col * tileSize - camX
            val tileY = row * tileSize - camY

            // Pseudo random tile pattern based on coordinates
            val hash = abs((col * 73856093) xor (row * 19349663))
            val tileColor = if (hash % 5 == 0) ForestTileAccent else ForestTileGreen

            drawRect(
                color = tileColor,
                topLeft = Offset(tileX + 2f, tileY + 2f),
                size = Size(tileSize - 4f, tileSize - 4f)
            )

            // Scattered glowing flora or dark stone
            if (hash % 11 == 0) {
                drawCircle(
                    color = Color(0xFF1E3A2B),
                    radius = 12f,
                    center = Offset(tileX + tileSize * 0.5f, tileY + tileSize * 0.5f)
                )
            } else if (hash % 17 == 0) {
                // Neon glowing mushroom dot
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.4f),
                    radius = 4f,
                    center = Offset(tileX + tileSize * 0.35f, tileY + tileSize * 0.65f)
                )
            }
        }
    }
}

private fun DrawScope.drawKnightSlime(state: GameState, px: Float, py: Float) {
    val isMoving = state.isMoving
    val walkTime = state.playerWalkTime

    // Squash & Stretch Animation
    val squashX = if (isMoving) 1f + sin(walkTime) * 0.18f else 1f + sin(walkTime) * 0.06f
    val squashY = if (isMoving) 1f - sin(walkTime) * 0.18f else 1f - sin(walkTime) * 0.06f

    val baseW = 38f * squashX
    val baseH = 32f * squashY
    val hurtTint = state.playerHurtTimer > 0f

    // 1. Slime Shadow
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(px - baseW * 0.9f, py + baseH * 0.6f),
        size = Size(baseW * 1.8f, 14f)
    )

    // 2. Slime Glow Aura
    drawOval(
        color = if (hurtTint) HealthRed.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.3f),
        topLeft = Offset(px - baseW - 6f, py - baseH - 6f),
        size = Size((baseW + 6f) * 2f, (baseH + 6f) * 2f)
    )

    // 3. Slime Body (Blue / Red on hurt)
    val bodyColor = if (hurtTint) HealthRed else SlimeBlue
    val topColor = if (hurtTint) Color(0xFFFF8FA3) else SlimeBlueGlow

    drawOval(
        color = bodyColor,
        topLeft = Offset(px - baseW, py - baseH),
        size = Size(baseW * 2f, baseH * 2f)
    )

    drawOval(
        color = topColor,
        topLeft = Offset(px - baseW * 0.7f, py - baseH * 0.8f),
        size = Size(baseW * 1.4f, baseH * 1.3f)
    )

    // 4. Slime Gloss Highlight
    drawOval(
        color = Color.White.copy(alpha = 0.7f),
        topLeft = Offset(px - baseW * 0.5f, py - baseH * 0.65f),
        size = Size(baseW * 0.4f, baseH * 0.35f)
    )

    // 5. Cute Round Eyes
    val eyeOffsetX = state.moveDirection.x * 4f
    val eyeOffsetY = state.moveDirection.y * 3f

    val leftEyeX = px - 9f + eyeOffsetX
    val rightEyeX = px + 9f + eyeOffsetX
    val eyeY = py - 2f + eyeOffsetY

    drawOval(
        color = Color(0xFF0B132B),
        topLeft = Offset(leftEyeX - 4f, eyeY - 6f),
        size = Size(8f, 12f)
    )
    drawOval(
        color = Color(0xFF0B132B),
        topLeft = Offset(rightEyeX - 4f, eyeY - 6f),
        size = Size(8f, 12f)
    )
    // Eye light sparks
    drawCircle(
        color = Color.White,
        radius = 2f,
        center = Offset(leftEyeX - 1f, eyeY - 3f)
    )
    drawCircle(
        color = Color.White,
        radius = 2f,
        center = Offset(rightEyeX - 1f, eyeY - 3f)
    )

    // 6. Knight Helmet (Tilted on top)
    val helmX = px - 16f
    val helmY = py - baseH - 12f + (if (isMoving) sin(walkTime) * 3f else 0f)

    // Steel Helmet Dome
    drawRoundRect(
        color = Color(0xFF6C757D),
        topLeft = Offset(helmX, helmY),
        size = Size(32f, 18f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    // Helmet Visor line
    drawRect(
        color = Color(0xFF212529),
        topLeft = Offset(helmX + 4f, helmY + 8f),
        size = Size(24f, 4f)
    )
    // Red Plume Feather
    val plumePath = Path().apply {
        moveTo(px - 2f, helmY)
        lineTo(px + 12f, helmY - 14f)
        lineTo(px + 4f, helmY - 2f)
        close()
    }
    drawPath(path = plumePath, color = HealthRed)
}

private fun DrawScope.drawEnemy(enemy: Enemy, ex: Float, ey: Float) {
    val hurt = enemy.hurtTimer > 0f

    when (enemy.type) {
        EnemyType.SPORE_FUNGUS -> {
            val waddle = sin(enemy.walkPhase) * 4f
            val baseColor = if (hurt) Color.White else SporePurple
            val capColor = if (hurt) HealthRed else Color(0xFF7B2CBF)

            // Stem
            drawRoundRect(
                color = Color(0xFFE0AAFF),
                topLeft = Offset(ex - 8f + waddle, ey - 6f),
                size = Size(16f, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            // Mushroom Cap
            drawArc(
                color = capColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(ex - 22f + waddle, ey - 26f),
                size = Size(44f, 32f)
            )
            // Glowing Pink Spore Spots
            drawCircle(color = SporePink, radius = 3.5f, center = Offset(ex - 10f + waddle, ey - 18f))
            drawCircle(color = SporePink, radius = 4f, center = Offset(ex + 6f + waddle, ey - 20f))
            drawCircle(color = SporePink, radius = 3f, center = Offset(ex + 12f + waddle, ey - 14f))

            // Eyes
            drawCircle(color = Color(0xFF10002B), radius = 2f, center = Offset(ex - 4f + waddle, ey + 2f))
            drawCircle(color = Color(0xFF10002B), radius = 2f, center = Offset(ex + 4f + waddle, ey + 2f))
        }

        EnemyType.FLAME_BAT -> {
            val flap = sin(enemy.flapPhase) * 14f
            val batColor = if (hurt) Color.White else HealthRed

            // Left Wing
            val leftWing = Path().apply {
                moveTo(ex, ey)
                lineTo(ex - 24f, ey - 12f + flap)
                lineTo(ex - 10f, ey + 6f)
                close()
            }
            // Right Wing
            val rightWing = Path().apply {
                moveTo(ex, ey)
                lineTo(ex + 24f, ey - 12f + flap)
                lineTo(ex + 10f, ey + 6f)
                close()
            }
            drawPath(leftWing, color = batColor)
            drawPath(rightWing, color = batColor)

            // Body
            drawOval(
                color = Color(0xFF590D22),
                topLeft = Offset(ex - 8f, ey - 8f),
                size = Size(16f, 18f)
            )
            // Fiery glowing eyes
            drawCircle(color = NeonFireYellow, radius = 2.5f, center = Offset(ex - 3f, ey - 2f))
            drawCircle(color = NeonFireYellow, radius = 2.5f, center = Offset(ex + 3f, ey - 2f))
        }

        EnemyType.FOREST_GOLEM -> {
            val golemColor = if (hurt) Color.White else EntWoodBrown
            // Body
            drawRoundRect(
                color = golemColor,
                topLeft = Offset(ex - 24f, ey - 32f),
                size = Size(48f, 54f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
            // Stone shoulder plates
            drawCircle(color = Color(0xFF3D2619), radius = 14f, center = Offset(ex - 22f, ey - 18f))
            drawCircle(color = Color(0xFF3D2619), radius = 14f, center = Offset(ex + 22f, ey - 18f))
            // Glowing green eye slit
            drawRect(
                color = EntGlowGreen,
                topLeft = Offset(ex - 12f, ey - 16f),
                size = Size(24f, 6f)
            )
        }

        EnemyType.OLD_TREE_ENT_BOSS -> {
            // Giant 4x Boss
            val entColor = if (hurt) Color.White else EntWoodBrown
            val waddle = sin(enemy.walkPhase) * 6f

            // Giant Trunk Body
            drawRoundRect(
                color = entColor,
                topLeft = Offset(ex - 52f + waddle, ey - 75f),
                size = Size(104f, 130f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            )
            // Bark texture lines
            drawLine(
                color = Color(0xFF3A2417),
                start = Offset(ex - 20f + waddle, ey - 50f),
                end = Offset(ex - 15f + waddle, ey + 30f),
                strokeWidth = 6f
            )
            drawLine(
                color = Color(0xFF3A2417),
                start = Offset(ex + 18f + waddle, ey - 45f),
                end = Offset(ex + 22f + waddle, ey + 25f),
                strokeWidth = 6f
            )

            // Leaf Crown
            drawCircle(color = ForestTileGreen, radius = 35f, center = Offset(ex - 35f + waddle, ey - 70f))
            drawCircle(color = ForestTileGreen, radius = 42f, center = Offset(ex + waddle, ey - 85f))
            drawCircle(color = ForestTileGreen, radius = 35f, center = Offset(ex + 35f + waddle, ey - 70f))

            // Deep Glowing Emerald Eyes
            drawCircle(color = EntGlowGreen, radius = 9f, center = Offset(ex - 20f + waddle, ey - 25f))
            drawCircle(color = EntGlowGreen, radius = 9f, center = Offset(ex + 20f + waddle, ey - 25f))
            drawCircle(color = Color.White, radius = 4f, center = Offset(ex - 18f + waddle, ey - 26f))
            drawCircle(color = Color.White, radius = 4f, center = Offset(ex + 22f + waddle, ey - 26f))

            // Roots Base
            drawRoundRect(
                color = Color(0xFF2C1810),
                topLeft = Offset(ex - 60f + waddle, ey + 35f),
                size = Size(120f, 28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )
        }
    }

    // Enemy mini health bar if damaged and not boss
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

private fun DrawScope.drawXpGem(gx: Float, gy: Float, isRed: Boolean, isGold: Boolean) {
    val gemColor = if (isGold) GemGold else if (isRed) GemRed else GemGreen
    val size = if (isGold) 12f else if (isRed) 9f else 7f

    // Glowing diamond path
    val diamond = Path().apply {
        moveTo(gx, gy - size)
        lineTo(gx + size, gy)
        lineTo(gx, gy + size)
        lineTo(gx - size, gy)
        close()
    }

    // Outer glow
    drawCircle(color = gemColor.copy(alpha = 0.35f), radius = size * 1.8f, center = Offset(gx, gy))
    drawPath(path = diamond, color = gemColor)
    // Diamond Inner Shimmer
    drawCircle(color = Color.White, radius = size * 0.35f, center = Offset(gx - 1f, gy - 2f))
}

private fun DrawScope.drawSpinningAxe(ax: Float, ay: Float, angle: Float) {
    rotate(degrees = Math.toDegrees(angle.toDouble()).toFloat() * 2.5f, pivot = Offset(ax, ay)) {
        // Wooden handle
        drawRect(
            color = EntWoodBrown,
            topLeft = Offset(ax - 3f, ay - 20f),
            size = Size(6f, 40f)
        )
        // Golden double axe blades
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
