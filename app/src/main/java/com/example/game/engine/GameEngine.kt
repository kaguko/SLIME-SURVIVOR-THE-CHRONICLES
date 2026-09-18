package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.audio.SoundFxSynth
import com.example.game.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.*
import kotlin.random.Random

data class GameState(
    // Player status
    val playerX: Float = 0f,
    val playerY: Float = 0f,
    val playerHp: Float = 100f,
    val playerMaxHp: Float = 100f,
    val playerSpeed: Float = 160f,
    val playerLevel: Int = 1,
    val currentXp: Int = 0,
    val xpNeeded: Int = 20,
    val playerHurtTimer: Float = 0f,
    val playerWalkTime: Float = 0f,
    val isMoving: Boolean = false,
    val moveDirection: Vector2D = Vector2D(0f, 0f),

    // Timer & Game Status
    val timeRemainingSeconds: Float = 300f, // 5:00
    val totalTimeSurvived: Float = 0f,
    val isGamePaused: Boolean = false,
    val isLevelUpPending: Boolean = false,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val killCount: Int = 0,
    val score: Int = 0,
    val screenShake: Float = 0f,

    // Entities
    val enemies: List<Enemy> = emptyList(),
    val gems: List<XpGem> = emptyList(),
    val particles: List<Particle> = emptyList(),
    val damageNumbers: List<DamageNumber> = emptyList(),
    val trails: List<TrailPoint> = emptyList(),
    val lightnings: List<LightningEffect> = emptyList(),
    val puddles: List<GroundPuddle> = emptyList(),

    // Weapon States
    val fireOrbitAngle: Float = 0f,
    val fireOrbCount: Int = 0,
    val spinningAxes: List<SpinningAxe> = emptyList(),

    // Skills Equipped Map (SkillId -> Level)
    val skillLevels: Map<SkillId, Int> = mapOf(
        SkillId.CHAIN_LIGHTNING to 1
    ),
    val pendingLevelUpCards: List<SkillCardOption> = emptyList(),

    // Boss state
    val bossActive: Boolean = false,
    val bossHp: Float = 0f,
    val bossMaxHp: Float = 0f
)

class GameEngine(
    val soundFx: SoundFxSynth
) {
    private val idGen = AtomicLong(1000)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    // Internal timers
    private var enemySpawnTimer = 0f
    private var lightningCooldownTimer = 0f
    private var axeCooldownTimer = 0f
    private var puddleDropTimer = 0f
    private var regenTimer = 0f
    private var trailTimer = 0f
    private var bossSpawned = false

    fun resetGame() {
        bossSpawned = false
        enemySpawnTimer = 0f
        lightningCooldownTimer = 0f
        axeCooldownTimer = 0f
        puddleDropTimer = 0f
        regenTimer = 0f
        trailTimer = 0f
        _state.value = GameState()
    }

    fun setMovementInput(dx: Float, dy: Float) {
        val current = _state.value
        if (current.isGameOver || current.isVictory || current.isLevelUpPending) return

        val inputVec = Vector2D(dx, dy)
        val len = inputVec.length()
        val normalized = if (len > 0.05f) {
            val clampedLen = min(1f, len)
            Vector2D(inputVec.x / len * clampedLen, inputVec.y / len * clampedLen)
        } else {
            Vector2D(0f, 0f)
        }

        _state.value = current.copy(
            moveDirection = normalized,
            isMoving = normalized.length() > 0.1f
        )
    }

    fun update(dtSeconds: Float) {
        val s = _state.value
        if (s.isGamePaused || s.isGameOver || s.isVictory || s.isLevelUpPending) return

        val dt = min(0.05f, dtSeconds)
        val newTimeRemaining = max(0f, s.timeRemainingSeconds - dt)
        val newTotalSurvived = s.totalTimeSurvived + dt

        // Check 5:00 Victory condition
        if (newTimeRemaining <= 0f && !s.isVictory) {
            _state.value = s.copy(
                timeRemainingSeconds = 0f,
                totalTimeSurvived = newTotalSurvived,
                isVictory = true,
                score = s.score + 5000 + (s.killCount * 20)
            )
            soundFx.playLevelUp()
            return
        }

        // Screen shake decay
        val newShake = max(0f, s.screenShake - (dt * 15f))

        // Update player movement & trail
        val speed = s.playerSpeed * (1f + (s.skillLevels[SkillId.SPEED_BOOTS] ?: 0) * 0.15f)
        var newPx = s.playerX + (s.moveDirection.x * speed * dt)
        var newPy = s.playerY + (s.moveDirection.y * speed * dt)
        val newWalkTime = if (s.isMoving) s.playerWalkTime + dt * 8f else s.playerWalkTime + dt * 2f

        // Trails
        trailTimer += dt
        val updatedTrails = s.trails.mapNotNull {
            val na = it.alpha - (dt * 1.5f)
            if (na > 0.05f) it.copy(alpha = na) else null
        }.toMutableList()

        if (s.isMoving && trailTimer > 0.12f) {
            trailTimer = 0f
            updatedTrails.add(TrailPoint(newPx, newPy + 6f, alpha = 0.45f))
        }

        // Regeneration
        val regenLevel = s.skillLevels[SkillId.REGENERATION] ?: 0
        var newHp = s.playerHp
        if (regenLevel > 0) {
            regenTimer += dt
            if (regenTimer >= 2.0f) {
                regenTimer = 0f
                val healAmt = regenLevel * 3f
                newHp = min(s.playerMaxHp, newHp + healAmt)
            }
        }

        val newHurtTimer = max(0f, s.playerHurtTimer - dt)

        // Passive Skills Calculation
        val magnetLevel = s.skillLevels[SkillId.MAGNET_RING] ?: 0
        val magnetRadius = 80f + (magnetLevel * 60f)

        val fireLevel = s.skillLevels[SkillId.FIRE_ORBIT] ?: 0
        val fireCount = if (fireLevel > 0) 1 + fireLevel else 0
        val newFireAngle = (s.fireOrbitAngle + dt * (2.8f + fireLevel * 0.4f)) % (2 * Math.PI.toFloat())

        // Weapons Update
        val newParticles = s.particles.mapNotNull { p ->
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            if (p.life > 0) p else null
        }.toMutableList()

        val newDamageNumbers = s.damageNumbers.mapNotNull { d ->
            d.y -= 30f * dt
            d.life -= dt
            d.alpha = (d.life / 0.7f).coerceIn(0f, 1f)
            if (d.life > 0) d else null
        }.toMutableList()

        val newLightnings = s.lightnings.mapNotNull { l ->
            val np = l.progress - dt * 4f
            if (np > 0f) l.copy(progress = np) else null
        }.toMutableList()

        // Enemies update
        val updatedEnemies = s.enemies.map { it.copy() }.toMutableList()
        val newGems = s.gems.map { it.copy() }.toMutableList()
        var newKillCount = s.killCount
        var newScore = s.score

        // 1. Spawning Enemies based on timeline
        enemySpawnTimer += dt
        val spawnInterval = when {
            newTotalSurvived < 60f -> 1.4f
            newTotalSurvived < 120f -> 0.9f
            newTotalSurvived < 200f -> 0.6f
            else -> 0.4f
        }

        if (enemySpawnTimer >= spawnInterval && updatedEnemies.size < 75) {
            enemySpawnTimer = 0f
            val spawnAngle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val spawnDist = 450f + Random.nextFloat() * 100f
            val spawnX = newPx + cos(spawnAngle) * spawnDist
            val spawnY = newPy + sin(spawnAngle) * spawnDist

            val randType = when {
                newTotalSurvived < 60f -> EnemyType.SPORE_FUNGUS
                newTotalSurvived < 150f -> if (Random.nextFloat() < 0.65f) EnemyType.SPORE_FUNGUS else EnemyType.FLAME_BAT
                newTotalSurvived < 240f -> {
                    val r = Random.nextFloat()
                    when {
                        r < 0.45f -> EnemyType.SPORE_FUNGUS
                        r < 0.85f -> EnemyType.FLAME_BAT
                        else -> EnemyType.FOREST_GOLEM
                    }
                }
                else -> {
                    if (Random.nextFloat() < 0.5f) EnemyType.FLAME_BAT else EnemyType.FOREST_GOLEM
                }
            }

            updatedEnemies.add(
                Enemy(
                    id = idGen.incrementAndGet(),
                    type = randType,
                    x = spawnX,
                    y = spawnY,
                    hp = randType.maxHp * (1f + (newTotalSurvived / 200f)),
                    maxHp = randType.maxHp * (1f + (newTotalSurvived / 200f))
                )
            )
        }

        // Spawn Boss Old Tree Ent at 4:00 (240s)
        var bossActiveNow = s.bossActive
        var bossCurHp = s.bossHp
        var bossMaxHpVal = s.bossMaxHp
        var addedShake = 0f

        if (newTotalSurvived >= 240f && !bossSpawned) {
            bossSpawned = true
            bossActiveNow = true
            val bType = EnemyType.OLD_TREE_ENT_BOSS
            val boss = Enemy(
                id = idGen.incrementAndGet(),
                type = bType,
                x = newPx + 380f,
                y = newPy + 300f,
                hp = bType.maxHp,
                maxHp = bType.maxHp
            )
            updatedEnemies.add(boss)
            bossCurHp = bType.maxHp
            bossMaxHpVal = bType.maxHp
            addedShake = 12f
            soundFx.playBossRoar()
        }

        // 2. Weapon 1: Chain Lightning Auto-Aim
        val lightningLevel = s.skillLevels[SkillId.CHAIN_LIGHTNING] ?: 0
        if (lightningLevel > 0) {
            lightningCooldownTimer += dt
            val cooldown = max(0.5f, 1.8f - (lightningLevel * 0.22f))
            if (lightningCooldownTimer >= cooldown && updatedEnemies.isNotEmpty()) {
                lightningCooldownTimer = 0f
                // Find nearest enemy
                val livingEnemies = updatedEnemies.filter { !it.isDead && it.hp > 0 }
                if (livingEnemies.isNotEmpty()) {
                    val sorted = livingEnemies.sortedBy { Vector2D(it.x - newPx, it.y - newPy).length() }
                    val targetCount = min(1 + lightningLevel, sorted.size)
                    val baseDmg = (28f + lightningLevel * 14f).toInt()

                    var prevX = newPx
                    var prevY = newPy - 20f

                    for (i in 0 until targetCount) {
                        val target = sorted[i]
                        target.hp -= baseDmg
                        target.hurtTimer = 0.25f

                        // Lightning visual
                        val branches = listOf(
                            Pair((prevX + target.x) / 2f + Random.nextInt(-20, 20), (prevY + target.y) / 2f + Random.nextInt(-20, 20))
                        )
                        newLightnings.add(
                            LightningEffect(
                                id = idGen.incrementAndGet(),
                                startX = prevX,
                                startY = prevY,
                                targetX = target.x,
                                targetY = target.y,
                                progress = 1f,
                                branches = branches
                            )
                        )

                        // Sparks particles
                        for (p in 0..5) {
                            val spkAngle = Random.nextFloat() * 2 * Math.PI.toFloat()
                            val spkSpd = 60f + Random.nextFloat() * 80f
                            newParticles.add(
                                Particle(
                                    x = target.x,
                                    y = target.y,
                                    vx = cos(spkAngle) * spkSpd,
                                    vy = sin(spkAngle) * spkSpd,
                                    color = NeonCyan,
                                    size = 4f,
                                    life = 0.3f,
                                    maxLife = 0.3f
                                )
                            )
                        }

                        // Damage number
                        newDamageNumbers.add(
                            DamageNumber(
                                id = idGen.incrementAndGet(),
                                x = target.x + Random.nextInt(-10, 10),
                                y = target.y - 15f,
                                damage = baseDmg,
                                color = NeonCyan,
                                isCrit = i == 0
                            )
                        )

                        prevX = target.x
                        prevY = target.y
                    }
                    soundFx.playLightning()
                }
            }
        }

        // 3. Weapon 2: Fire Orbit Hit-Testing
        if (fireLevel > 0 && fireCount > 0) {
            val fireRadius = 65f + (fireLevel * 10f)
            val fireDmg = (12f + fireLevel * 6f) * dt * 4f

            for (f in 0 until fireCount) {
                val orbAngle = newFireAngle + (f * (2 * Math.PI.toFloat() / fireCount))
                val orbX = newPx + cos(orbAngle) * fireRadius
                val orbY = newPy + sin(orbAngle) * fireRadius

                // Fire trail particle
                if (Random.nextFloat() < 0.4f) {
                    newParticles.add(
                        Particle(
                            x = orbX + Random.nextInt(-4, 4),
                            y = orbY + Random.nextInt(-4, 4),
                            vx = -cos(orbAngle) * 20f,
                            vy = -sin(orbAngle) * 20f,
                            color = if (Random.nextBoolean()) NeonFireOrange else NeonFireYellow,
                            size = 5f,
                            life = 0.25f,
                            maxLife = 0.25f
                        )
                    )
                }

                // Check collision with enemies
                updatedEnemies.forEach { enemy ->
                    if (!enemy.isDead && enemy.hp > 0) {
                        val d = sqrt((enemy.x - orbX) * (enemy.x - orbX) + (enemy.y - orbY) * (enemy.y - orbY))
                        if (d < enemy.type.sizeRadius + 16f) {
                            enemy.hp -= fireDmg
                            enemy.hurtTimer = 0.15f
                        }
                    }
                }
            }
        }

        // 4. Weapon 3: Spinning Axe
        val axeLevel = s.skillLevels[SkillId.SPINNING_AXE] ?: 0
        var currentAxes = s.spinningAxes.map { it.copy() }.toMutableList()
        if (axeLevel > 0) {
            axeCooldownTimer += dt
            val axeCooldown = max(1.2f, 3.2f - (axeLevel * 0.4f))
            if (axeCooldownTimer >= axeCooldown) {
                axeCooldownTimer = 0f
                val count = min(4, 1 + axeLevel)
                for (a in 0 until count) {
                    currentAxes.add(
                        SpinningAxe(
                            angle = a * (2 * Math.PI.toFloat() / count),
                            distance = 15f
                        )
                    )
                }
                soundFx.playAxeSlash()
            }

            // Update active spinning axes
            currentAxes = currentAxes.mapNotNull { axe ->
                axe.angle += dt * 6.5f
                axe.distance += dt * 160f
                val axeX = newPx + cos(axe.angle) * axe.distance
                val axeY = newPy + sin(axe.angle) * axe.distance

                // Damage enemies in slice radius
                val axeDmg = (35f + axeLevel * 18f).toInt()
                updatedEnemies.forEach { enemy ->
                    if (!enemy.isDead && enemy.hp > 0) {
                        val d = sqrt((enemy.x - axeX) * (enemy.x - axeX) + (enemy.y - axeY) * (enemy.y - axeY))
                        if (d < enemy.type.sizeRadius + 18f) {
                            enemy.hp -= axeDmg * dt * 3f
                            enemy.hurtTimer = 0.2f
                            if (Random.nextFloat() < 0.1f) {
                                newDamageNumbers.add(
                                    DamageNumber(
                                        id = idGen.incrementAndGet(),
                                        x = enemy.x,
                                        y = enemy.y - 12f,
                                        damage = axeDmg,
                                        color = PixelGold
                                    )
                                )
                            }
                        }
                    }
                }

                if (axe.distance < 380f) axe else null
            }.toMutableList()
        }

        // 5. Weapon 4: Toxic / Holy Puddles
        val puddleLevel = s.skillLevels[SkillId.HOLY_PUDDLE] ?: 0
        val updatedPuddles = s.puddles.mapNotNull { pud ->
            val rem = pud.durationRemaining - dt
            if (rem > 0f) {
                // Damage enemies inside
                val pudDmg = (15f + puddleLevel * 8f) * dt * 2.5f
                updatedEnemies.forEach { e ->
                    if (!e.isDead && e.hp > 0) {
                        val d = sqrt((e.x - pud.x) * (e.x - pud.x) + (e.y - pud.y) * (e.y - pud.y))
                        if (d < pud.radius + e.type.sizeRadius) {
                            e.hp -= pudDmg
                            e.hurtTimer = 0.1f
                        }
                    }
                }
                pud.copy(durationRemaining = rem)
            } else null
        }.toMutableList()

        if (puddleLevel > 0) {
            puddleDropTimer += dt
            if (puddleDropTimer >= 2.0f - (puddleLevel * 0.25f)) {
                puddleDropTimer = 0f
                updatedPuddles.add(
                    GroundPuddle(
                        id = idGen.incrementAndGet(),
                        x = newPx,
                        y = newPy,
                        radius = 45f + puddleLevel * 12f,
                        durationRemaining = 4f
                    )
                )
            }
        }

        // 6. Update enemies positions & check player collision
        var playerHitDmg = 0f
        val finalEnemies = mutableListOf<Enemy>()

        updatedEnemies.forEach { enemy ->
            if (enemy.hp <= 0 && !enemy.isDead) {
                enemy.isDead = true
                newKillCount++
                newScore += if (enemy.type.isBoss) 1000 else if (enemy.type.isRedGem) 100 else 30

                // Drop XP Gem
                newGems.add(
                    XpGem(
                        id = idGen.incrementAndGet(),
                        x = enemy.x,
                        y = enemy.y,
                        value = enemy.type.xpReward,
                        isRed = enemy.type.isRedGem,
                        isGold = enemy.type.isBoss
                    )
                )

                // Death burst particles
                val partColor = if (enemy.type.isRedGem) SporePink else SporePurple
                for (i in 0..8) {
                    val a = Random.nextFloat() * 2 * Math.PI.toFloat()
                    val sp = 40f + Random.nextFloat() * 60f
                    newParticles.add(
                        Particle(
                            x = enemy.x,
                            y = enemy.y,
                            vx = cos(a) * sp,
                            vy = sin(a) * sp,
                            color = partColor,
                            size = 4.5f,
                            life = 0.35f,
                            maxLife = 0.35f
                        )
                    )
                }

                // If boss dead, trigger victory!
                if (enemy.type.isBoss) {
                    bossActiveNow = false
                }
            } else if (!enemy.isDead) {
                // Move towards player
                val toPlayer = Vector2D(newPx - enemy.x, newPy - enemy.y)
                val dist = toPlayer.length()
                val dir = toPlayer.normalized()

                // Separation from other enemies
                var sepX = 0f
                var sepY = 0f
                // Simple nudge
                val moveSpd = enemy.type.baseSpeed
                enemy.x += dir.x * moveSpd * dt + sepX
                enemy.y += dir.y * moveSpd * dt + sepY
                enemy.walkPhase += dt * 5f
                enemy.flapPhase += dt * 14f
                enemy.hurtTimer = max(0f, enemy.hurtTimer - dt)

                // Collision with player
                if (dist < enemy.type.sizeRadius + 14f) {
                    playerHitDmg += enemy.type.touchDamage * dt
                }

                if (enemy.type.isBoss) {
                    bossCurHp = enemy.hp
                }

                finalEnemies.add(enemy)
            }
        }

        // Apply player damage with grace period
        if (playerHitDmg > 0f && newHurtTimer <= 0f) {
            val armor = (s.skillLevels[SkillId.MAX_HP_BOOST] ?: 0) * 1.5f
            val actualDmg = max(1f, playerHitDmg * 5f - armor)
            newHp = max(0f, newHp - actualDmg)
            soundFx.playHurt()
            _state.value = s.copy(playerHurtTimer = 0.3f, screenShake = min(15f, newShake + 4f))
        }

        // 7. Gem Attraction and Pickup
        var gainedXp = 0
        val remainingGems = mutableListOf<XpGem>()

        newGems.forEach { gem ->
            val distToP = sqrt((newPx - gem.x) * (newPx - gem.x) + (newPy - gem.y) * (newPy - gem.y))
            if (distToP < 24f) {
                // Picked up!
                gainedXp += gem.value
                soundFx.playGemPickup()
                for (g in 0..2) {
                    newParticles.add(
                        Particle(
                            x = gem.x,
                            y = gem.y,
                            vx = Random.nextInt(-30, 30).toFloat(),
                            vy = Random.nextInt(-30, 30).toFloat(),
                            color = if (gem.isRed) GemRed else GemGreen,
                            size = 3.5f,
                            life = 0.2f,
                            maxLife = 0.2f
                        )
                    )
                }
            } else if (distToP < magnetRadius || gem.isPulled) {
                gem.isPulled = true
                gem.pullSpeed = min(420f, gem.pullSpeed + dt * 600f)
                val dir = Vector2D(newPx - gem.x, newPy - gem.y).normalized()
                gem.x += dir.x * gem.pullSpeed * dt
                gem.y += dir.y * gem.pullSpeed * dt
                remainingGems.add(gem)
            } else {
                remainingGems.add(gem)
            }
        }

        // 8. Level Up Progression
        var curLevel = s.playerLevel
        var curXp = s.currentXp + gainedXp
        var neededXp = s.xpNeeded
        var triggerLevelUp = false
        var levelUpCards = emptyList<SkillCardOption>()

        while (curXp >= neededXp) {
            curXp -= neededXp
            curLevel++
            neededXp = (curLevel * 22) + 15
            triggerLevelUp = true
        }

        if (triggerLevelUp) {
            soundFx.playLevelUp()
            levelUpCards = generateLevelUpChoices(s.skillLevels)
        }

        // Check game over
        val isDead = newHp <= 0f

        _state.value = s.copy(
            playerX = newPx,
            playerY = newPy,
            playerHp = newHp,
            playerWalkTime = newWalkTime,
            timeRemainingSeconds = newTimeRemaining,
            totalTimeSurvived = newTotalSurvived,
            isGameOver = isDead,
            isVictory = (bossSpawned && !bossActiveNow && newTotalSurvived >= 240f) || newTimeRemaining <= 0f,
            killCount = newKillCount,
            score = newScore + gainedXp * 10,
            screenShake = max(0f, newShake + addedShake),
            enemies = finalEnemies,
            gems = remainingGems,
            particles = newParticles,
            damageNumbers = newDamageNumbers,
            trails = updatedTrails,
            lightnings = newLightnings,
            puddles = updatedPuddles,
            fireOrbitAngle = newFireAngle,
            fireOrbCount = fireCount,
            spinningAxes = currentAxes,
            currentXp = curXp,
            playerLevel = curLevel,
            xpNeeded = neededXp,
            isLevelUpPending = triggerLevelUp,
            pendingLevelUpCards = if (triggerLevelUp) levelUpCards else s.pendingLevelUpCards,
            bossActive = bossActiveNow,
            bossHp = bossCurHp,
            bossMaxHp = bossMaxHpVal
        )
    }

    private fun generateLevelUpChoices(currentSkills: Map<SkillId, Int>): List<SkillCardOption> {
        val allSkills = listOf(
            Triple(SkillId.CHAIN_LIGHTNING, "Tia Sét Định Vị", "Giật sét đánh kẻ địch gần nhất, tự động lan truyền tia điện cực mạnh."),
            Triple(SkillId.FIRE_ORBIT, "Vòng Lửa Bảo Vệ", "Quả cầu lửa ma thuật xoay tròn quanh Slime thiêu rụi quái vật tiếp cận."),
            Triple(SkillId.SPINNING_AXE, "Rìu Xoay Càn Quét", "Phóng những chiếc rìu ma thuật xoay xoáy theo hình xoắn ốc xé toạc bầy quái."),
            Triple(SkillId.HOLY_PUDDLE, "Bãi Độc Thánh", "Để lại những vũng axit ma thuật trên mặt đất thiêu đốt kẻ địch giẫm vào."),
            Triple(SkillId.SPEED_BOOTS, "Giày Gió Thần Tốc", "Tăng tốc độ di chuyển thêm +15%, giúp Slime luồn lách dễ dàng."),
            Triple(SkillId.MAGNET_RING, "Nhẫn Hút Ngọc", "Mở rộng bán kính hút ngọc kinh nghiệm từ khoảng cách xa."),
            Triple(SkillId.MAX_HP_BOOST, "Giáp Thạch Cường Hóa", "Tăng 25 Máu tối đa và giảm sát thương gánh chịu từ quái vật."),
            Triple(SkillId.REGENERATION, "Trái Tim Rừng Xanh", "Tự động hồi phục sinh lực theo thời gian một cách ổn định.")
        )

        val shuffled = allSkills.shuffled().take(3)
        return shuffled.map { (id, vnTitle, desc) ->
            val curLvl = currentSkills[id] ?: 0
            val targetLvl = curLvl + 1
            SkillCardOption(
                skillId = id,
                currentLevel = curLvl,
                targetLevel = targetLvl,
                title = id.name.replace("_", " "),
                vietnameseTitle = vnTitle,
                description = desc,
                iconName = id.name.lowercase(),
                isNew = curLvl == 0
            )
        }
    }

    fun applySkillUpgrade(card: SkillCardOption) {
        val s = _state.value
        val updatedMap = s.skillLevels.toMutableMap()
        val nextLevel = (updatedMap[card.skillId] ?: 0) + 1
        updatedMap[card.skillId] = nextLevel

        // If HP boost, heal player slightly
        var newMaxHp = s.playerMaxHp
        var newHp = s.playerHp
        if (card.skillId == SkillId.MAX_HP_BOOST) {
            newMaxHp += 25f
            newHp = min(newMaxHp, newHp + 25f)
        }

        _state.value = s.copy(
            skillLevels = updatedMap,
            playerMaxHp = newMaxHp,
            playerHp = newHp,
            isLevelUpPending = false,
            pendingLevelUpCards = emptyList()
        )
    }

    fun togglePause() {
        val s = _state.value
        _state.value = s.copy(isGamePaused = !s.isGamePaused)
    }
}
