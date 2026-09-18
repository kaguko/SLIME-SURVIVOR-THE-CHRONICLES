package com.example.game.engine

import com.example.game.audio.SoundFxSynth
import com.example.game.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.*
import kotlin.random.Random

data class GameState(
    val timeRemainingSeconds: Float = 300f, // 5:00 minutes
    val totalTimeSurvived: Float = 0f,
    val playerHp: Float = 100f,
    val playerMaxHp: Float = 100f,
    val playerLevel: Int = 1,
    val currentXp: Int = 0,
    val xpNeeded: Int = 20,
    val playerX: Float = 0f,
    val playerY: Float = 0f,
    val moveDirection: Pair<Float, Float> = Pair(0f, 0f),
    val isMoving: Boolean = false,
    val playerWalkTime: Float = 0f,
    val playerHurtTimer: Float = 0f,
    val isInvincible: Boolean = false,
    val invincibleTimer: Float = 0f,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val isGamePaused: Boolean = false,
    val killCount: Int = 0,
    val goldCollectedInRun: Int = 0,
    val score: Int = 0,
    val canRevive: Boolean = true,
    val screenShake: Float = 0f,
    val selectedHero: SlimeHero = SlimeHero.KNIGHT_SLIME,
    val selectedStage: GameStage = GameStage.ENCHANTED_FOREST,

    // Entities
    val enemies: List<Enemy> = emptyList(),
    val gems: List<XpGem> = emptyList(),
    val goldCoins: List<GoldCoin> = emptyList(),
    val chests: List<TreasureChest> = emptyList(),
    val puddles: List<ToxicPuddle> = emptyList(),
    val trails: List<SlimeWaterTrail> = emptyList(),
    val lightnings: List<LightningStrike> = emptyList(),
    val spinningAxes: List<SpinningAxeEntity> = emptyList(),
    val particles: List<Particle> = emptyList(),
    val damageNumbers: List<DamageNumber> = emptyList(),
    val bossRootHazards: List<BossRootHazard> = emptyList(),

    // Weapon States
    val fireOrbCount: Int = 0,
    val fireOrbitAngle: Float = 0f,
    val skillLevels: Map<SkillId, Int> = mapOf(SkillId.CHAIN_LIGHTNING to 1),

    // Level-Up System
    val isLevelUpPending: Boolean = false,
    val pendingLevelUpCards: List<SkillCardOption> = emptyList(),

    // Boss Info
    val bossActive: Boolean = false,
    val bossHp: Float = 0f,
    val bossMaxHp: Float = 0f
) {
    val isMaxLevel: Boolean get() = playerLevel >= 50
}

class GameEngine(private val soundFx: SoundFxSynth) {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var enemyIdSeq = 1L
    private var gemIdSeq = 1L
    private var goldIdSeq = 1L
    private var chestIdSeq = 1L

    private var inputDx = 0f
    private var inputDy = 0f

    // Weapon Cooldown Timers
    private var lightningCooldown = 0f
    private var axeCooldown = 0f
    private var puddleCooldown = 0f
    private var regenTimer = 0f
    private var enemySpawnTimer = 0f
    private var trailTimer = 0f
    private var bossSpawned = false

    // Meta-progression multipliers
    var metaHpBonus = 0f
    var metaSpeedMultiplier = 1f
    var metaDmgMultiplier = 1f
    var metaGoldMultiplier = 1f
    var metaMagnetMultiplier = 1f
    var metaFreeRevive = false
    var isBatterySaver = false

    fun restoreSavedState(
        hero: SlimeHero,
        stage: GameStage,
        hp: Float,
        maxHp: Float,
        level: Int,
        xp: Int,
        xpNeeded: Int,
        timeRemainingSec: Float,
        timeSurvivedSec: Float,
        kills: Int,
        gold: Int,
        score: Int,
        skills: Map<SkillId, Int>,
        canRevive: Boolean
    ) {
        bossSpawned = timeRemainingSec <= 60f
        enemySpawnTimer = 0f
        lightningCooldown = 0f
        axeCooldown = 0f
        puddleCooldown = 0f
        regenTimer = 0f
        trailTimer = 0f

        val fireCount = when {
            skills.containsKey(SkillId.SOLAR_SUPERNOVA) -> 4
            skills.containsKey(SkillId.FIRE_ORBIT) -> if ((skills[SkillId.FIRE_ORBIT] ?: 0) >= 4) 3 else 2
            else -> 0
        }

        _state.value = GameState(
            playerHp = hp,
            playerMaxHp = maxHp,
            playerLevel = level,
            currentXp = xp,
            xpNeeded = xpNeeded,
            timeRemainingSeconds = timeRemainingSec,
            totalTimeSurvived = timeSurvivedSec,
            killCount = kills,
            goldCollectedInRun = gold,
            score = score,
            selectedHero = hero,
            selectedStage = stage,
            skillLevels = skills,
            fireOrbCount = fireCount,
            canRevive = canRevive
        )
        val stageTrack = when (stage) {
            GameStage.ENCHANTED_FOREST -> com.example.game.audio.BgmTrack.FOREST
            GameStage.MAGMA_CORE -> com.example.game.audio.BgmTrack.MAGMA
            GameStage.GLACIAL_FROST -> com.example.game.audio.BgmTrack.FROST
            GameStage.GOLDEN_TOMB -> com.example.game.audio.BgmTrack.TOMB
        }
        soundFx.setBgmTrack(stageTrack)
    }

    fun resetGame(
        hero: SlimeHero = SlimeHero.KNIGHT_SLIME,
        stage: GameStage = GameStage.ENCHANTED_FOREST
    ) {
        bossSpawned = false
        enemySpawnTimer = 0f
        lightningCooldown = 0f
        axeCooldown = 0f
        puddleCooldown = 0f
        regenTimer = 0f
        trailTimer = 0f

        val initialSkills = mutableMapOf<SkillId, Int>()
        initialSkills[hero.starterSkill] = 1

        val initialFireCount = if (hero.starterSkill == SkillId.FIRE_ORBIT) 2 else 0
        val maxHp = (hero.baseHp + metaHpBonus)

        _state.value = GameState(
            playerHp = maxHp,
            playerMaxHp = maxHp,
            selectedHero = hero,
            selectedStage = stage,
            skillLevels = initialSkills,
            fireOrbCount = initialFireCount,
            canRevive = true
        )
        val stageTrack = when (stage) {
            GameStage.ENCHANTED_FOREST -> com.example.game.audio.BgmTrack.FOREST
            GameStage.MAGMA_CORE -> com.example.game.audio.BgmTrack.MAGMA
            GameStage.GLACIAL_FROST -> com.example.game.audio.BgmTrack.FROST
            GameStage.GOLDEN_TOMB -> com.example.game.audio.BgmTrack.TOMB
        }
        soundFx.setBgmTrack(stageTrack)
    }

    fun setMovementInput(dx: Float, dy: Float) {
        inputDx = dx
        inputDy = dy
    }

    fun togglePause() {
        _state.update { it.copy(isGamePaused = !it.isGamePaused) }
    }

    fun update(dt: Float) {
        val cur = _state.value
        if (cur.isGameOver || cur.isVictory || cur.isGamePaused || cur.isLevelUpPending) {
            return
        }

        val effectiveDt = dt.coerceAtMost(0.05f)

        // 1. Update Survival Time & Check 5:00 Victory
        val newTimeRemaining = (cur.timeRemainingSeconds - effectiveDt).coerceAtLeast(0f)
        val newTimeSurvived = cur.totalTimeSurvived + effectiveDt
        val isVictorious = newTimeRemaining <= 0f

        if (isVictorious && !cur.isVictory) {
            soundFx.playLevelUp()
            soundFx.setBgmTrack(com.example.game.audio.BgmTrack.VICTORY)
            _state.update { it.copy(isVictory = true, isGameOver = false) }
            return
        }

        // 2. Player Movement
        val rawLen = sqrt(inputDx * inputDx + inputDy * inputDy)
        val normDx = if (rawLen > 0.01f) inputDx / rawLen else 0f
        val normDy = if (rawLen > 0.01f) inputDy / rawLen else 0f
        val isMoving = rawLen > 0.1f

        val bootsLevel = cur.skillLevels[SkillId.SPEED_BOOTS] ?: 0
        val speedMultiplier = (1f + bootsLevel * 0.12f) * metaSpeedMultiplier
        val playerSpeed = cur.selectedHero.baseSpeed * speedMultiplier

        val newPlayerX = cur.playerX + normDx * playerSpeed * effectiveDt
        val newPlayerY = cur.playerY + normDy * playerSpeed * effectiveDt
        val newWalkTime = cur.playerWalkTime + (if (isMoving) effectiveDt * 10f else effectiveDt * 3f)

        // 3. Slime Water Trails
        val trails = cur.trails.toMutableList()
        trailTimer += effectiveDt
        if (isMoving && trailTimer > 0.12f) {
            trailTimer = 0f
            trails.add(SlimeWaterTrail(x = newPlayerX, y = newPlayerY, alpha = 0.45f))
        }
        val updatedTrails = trails.mapNotNull {
            it.alpha -= effectiveDt * 0.5f
            if (it.alpha > 0.02f) it else null
        }

        // 4. Invincibility & Hurt Timer
        val hurtTimer = (cur.playerHurtTimer - effectiveDt).coerceAtLeast(0f)
        val invTimer = (cur.invincibleTimer - effectiveDt).coerceAtLeast(0f)
        val isInv = invTimer > 0f

        // 5. HP Regeneration
        val regenLevel = cur.skillLevels[SkillId.REGENERATION] ?: 0
        var updatedHp = cur.playerHp
        if (regenLevel > 0) {
            regenTimer += effectiveDt
            if (regenTimer >= 2.5f) {
                regenTimer = 0f
                val healAmount = regenLevel * 3.5f
                updatedHp = (updatedHp + healAmount).coerceAtMost(cur.playerMaxHp)
            }
        }

        // 6. Spawn Enemies by Stage Progression
        val enemies = cur.enemies.toMutableList()
        enemySpawnTimer += effectiveDt
        val spawnInterval = when {
            newTimeSurvived < 60f -> 1.2f
            newTimeSurvived < 120f -> 0.85f
            newTimeSurvived < 180f -> 0.6f
            newTimeSurvived < 240f -> 0.45f
            else -> 0.35f
        }

        val maxEnemiesAllowed = if (isBatterySaver) 45 else 65
        if (enemySpawnTimer >= spawnInterval && enemies.size < maxEnemiesAllowed) {
            enemySpawnTimer = 0f
            spawnStageEnemyWave(cur.selectedStage, newTimeSurvived, newPlayerX, newPlayerY, enemies)
        }

        // Boss Spawn at 4:00 (240s)
        if (newTimeSurvived >= 240f && !bossSpawned) {
            bossSpawned = true
            soundFx.playBossRoar()
            soundFx.setBgmTrack(com.example.game.audio.BgmTrack.BOSS)
            val bossType = cur.selectedStage.bossType
            val boss = Enemy(
                id = enemyIdSeq++,
                x = newPlayerX + 450f,
                y = newPlayerY,
                hp = bossType.baseHp,
                maxHp = bossType.baseHp,
                type = bossType
            )
            enemies.add(boss)
        }

        // 7. Fire Orbit weapon updates
        val fireOrbitLevel = cur.skillLevels[SkillId.FIRE_ORBIT] ?: 0
        val isSupernova = (cur.skillLevels[SkillId.SOLAR_SUPERNOVA] ?: 0) > 0
        val activeFireCount = when {
            isSupernova -> 4
            fireOrbitLevel >= 4 -> 3
            fireOrbitLevel >= 1 -> 2
            else -> 0
        }
        val fireOrbitAngle = (cur.fireOrbitAngle + effectiveDt * 3.6f) % (2 * Math.PI.toFloat())

        // 8. Weapon Logic Execution
        val damageNumbers = cur.damageNumbers.toMutableList()
        val particles = cur.particles.toMutableList()
        val lightnings = cur.lightnings.toMutableList()
        val spinningAxes = cur.spinningAxes.toMutableList()
        val puddles = cur.puddles.toMutableList()
        var killsGained = 0
        var scoreGained = 0
        val droppedGems = cur.gems.toMutableList()
        val droppedGold = cur.goldCoins.toMutableList()
        val droppedChests = cur.chests.toMutableList()

        val globalDmgMult = metaDmgMultiplier * (if (cur.selectedHero == SlimeHero.FIRE_SLIME) 1.3f else 1.0f)

        // Weapon A: Chain Lightning / Thunder Wrath
        val lightningLevel = cur.skillLevels[SkillId.CHAIN_LIGHTNING] ?: 0
        val isThunderWrath = (cur.skillLevels[SkillId.THUNDER_WRATH] ?: 0) > 0
        if (lightningLevel > 0 || isThunderWrath) {
            lightningCooldown -= effectiveDt
            val strikeInterval = if (isThunderWrath) 0.65f else (1.4f - lightningLevel * 0.15f).coerceAtLeast(0.6f)
            if (lightningCooldown <= 0f && enemies.isNotEmpty()) {
                lightningCooldown = strikeInterval
                val targets = if (isThunderWrath) 5 else (1 + lightningLevel / 2)
                val closestEnemies = enemies.sortedBy { e ->
                    (e.x - newPlayerX).pow(2) + (e.y - newPlayerY).pow(2)
                }.take(targets)

                closestEnemies.forEach { target ->
                    val baseDmg = if (isThunderWrath) 75f else (28f + lightningLevel * 14f)
                    val dmg = (baseDmg * globalDmgMult).toInt()
                    target.hp -= dmg
                    target.hurtTimer = 0.2f
                    soundFx.playLightning()

                    damageNumbers.add(DamageNumber(target.x, target.y - 20f, dmg, NeonCyan, isCrit = true))
                    lightnings.add(
                        LightningStrike(
                            startX = target.x + Random.nextFloat() * 40f - 20f,
                            startY = target.y - 320f,
                            targetX = target.x,
                            targetY = target.y,
                            branches = listOf(
                                Pair(target.x - 20f, target.y - 180f),
                                Pair(target.x + 18f, target.y - 80f)
                            )
                        )
                    )
                    repeat(6) {
                        particles.add(
                            Particle(
                                x = target.x,
                                y = target.y,
                                vx = Random.nextFloat() * 160f - 80f,
                                vy = Random.nextFloat() * 160f - 80f,
                                life = 0.35f,
                                maxLife = 0.35f,
                                size = 6f,
                                color = NeonCyan
                            )
                        )
                    }
                }
            }
        }

        // Weapon B: Fire Orbit Contact Damage
        if (activeFireCount > 0) {
            val fireRadius = 65f + fireOrbitLevel * 10f
            for (f in 0 until activeFireCount) {
                val orbAngle = fireOrbitAngle + (f * (2 * Math.PI.toFloat() / activeFireCount))
                val ox = newPlayerX + cos(orbAngle) * fireRadius
                val oy = newPlayerY + sin(orbAngle) * fireRadius

                enemies.forEach { e ->
                    val distSq = (e.x - ox).pow(2) + (e.y - oy).pow(2)
                    if (distSq < (e.type.sizeRadius + 18f).pow(2) && e.hurtTimer <= 0f) {
                        val baseDmg = if (isSupernova) 38f else (14f + fireOrbitLevel * 6f)
                        val dmg = (baseDmg * globalDmgMult).toInt()
                        e.hp -= dmg
                        e.hurtTimer = 0.25f
                        damageNumbers.add(DamageNumber(e.x, e.y - 15f, dmg, NeonFireOrange))
                        repeat(4) {
                            particles.add(
                                Particle(
                                    x = ox,
                                    y = oy,
                                    vx = Random.nextFloat() * 100f - 50f,
                                    vy = Random.nextFloat() * 100f - 50f,
                                    life = 0.3f,
                                    maxLife = 0.3f,
                                    size = 5f,
                                    color = NeonFireYellow
                                )
                            )
                        }
                    }
                }
            }
        }

        // Weapon C: Spinning Axe
        val axeLevel = cur.skillLevels[SkillId.SPINNING_AXE] ?: 0
        if (axeLevel > 0) {
            axeCooldown -= effectiveDt
            if (axeCooldown <= 0f) {
                axeCooldown = (2.2f - axeLevel * 0.25f).coerceAtLeast(1.0f)
                spinningAxes.add(
                    SpinningAxeEntity(
                        angle = Random.nextFloat() * 6.28f,
                        distance = 30f,
                        damage = 35f + axeLevel * 15f
                    )
                )
                soundFx.playAxeSlash()
            }
        }

        val updatedAxes = spinningAxes.mapNotNull { axe ->
            axe.angle += axe.speed * effectiveDt
            axe.distance += 160f * effectiveDt
            val ax = newPlayerX + cos(axe.angle) * axe.distance
            val ay = newPlayerY + sin(axe.angle) * axe.distance

            enemies.forEach { e ->
                val dSq = (e.x - ax).pow(2) + (e.y - ay).pow(2)
                if (dSq < (e.type.sizeRadius + 24f).pow(2) && e.hurtTimer <= 0f) {
                    val dmg = (axe.damage * globalDmgMult).toInt()
                    e.hp -= dmg
                    e.hurtTimer = 0.2f
                    damageNumbers.add(DamageNumber(e.x, e.y - 18f, dmg, PixelGold, isCrit = true))
                }
            }

            if (axe.distance < 450f) axe else null
        }

        // Weapon D: Holy Puddle
        val puddleLevel = cur.skillLevels[SkillId.HOLY_PUDDLE] ?: 0
        if (puddleLevel > 0) {
            puddleCooldown -= effectiveDt
            if (puddleCooldown <= 0f && isMoving) {
                puddleCooldown = (3.0f - puddleLevel * 0.4f).coerceAtLeast(1.2f)
                puddles.add(
                    ToxicPuddle(
                        x = newPlayerX,
                        y = newPlayerY,
                        damage = 8f + puddleLevel * 5f
                    )
                )
            }
        }

        val updatedPuddles = puddles.mapNotNull { pud ->
            pud.durationRemaining -= effectiveDt
            enemies.forEach { e ->
                val dSq = (e.x - pud.x).pow(2) + (e.y - pud.y).pow(2)
                if (dSq < (pud.radius + e.type.sizeRadius).pow(2) && e.hurtTimer <= 0f) {
                    val dmg = (pud.damage * globalDmgMult).toInt()
                    e.hp -= dmg
                    e.hurtTimer = 0.35f
                    damageNumbers.add(DamageNumber(e.x, e.y - 10f, dmg, SporePurple))
                }
            }
            if (pud.durationRemaining > 0f) pud else null
        }

        // 9. Enemy Movement & Player Collisions
        var playerTookHit = false
        var screenShakeIntensity = (cur.screenShake - effectiveDt * 12f).coerceAtLeast(0f)

        val stageGoldMultiplier = cur.selectedStage.goldMultiplier
        val stageXpMultiplier = cur.selectedStage.xpMultiplier

        val survivingEnemies = enemies.mapNotNull { enemy ->
            enemy.hurtTimer = (enemy.hurtTimer - effectiveDt).coerceAtLeast(0f)
            enemy.walkPhase += effectiveDt * 8f
            enemy.flapPhase += effectiveDt * 16f

            if (enemy.hp <= 0f) {
                killsGained++
                scoreGained += (enemy.type.xpValue * 10 * stageXpMultiplier).toInt()
                soundFx.playGemPickup()

                // Drop XP Gem
                val xpVal = (enemy.type.xpValue * stageXpMultiplier).toInt().coerceAtLeast(1)
                droppedGems.add(
                    XpGem(
                        id = gemIdSeq++,
                        x = enemy.x,
                        y = enemy.y,
                        xpValue = xpVal,
                        isRed = enemy.type == EnemyType.FLAME_BAT || enemy.type == EnemyType.LAVA_SKULL,
                        isGold = enemy.type.isBoss
                    )
                )

                // Drop Gold Coin or Chest
                val goldRoll = Random.nextFloat()
                if (enemy.type.isBoss) {
                    droppedChests.add(TreasureChest(chestIdSeq++, enemy.x, enemy.y, (250 * stageGoldMultiplier).toInt()))
                    screenShakeIntensity = 18f
                } else if (enemy.type.isElite || goldRoll < 0.28f) {
                    val amt = if (enemy.type.isElite) 30 else 6
                    val totalGoldAmt = (amt * metaGoldMultiplier * stageGoldMultiplier).toInt().coerceAtLeast(1)
                    droppedGold.add(GoldCoin(goldIdSeq++, enemy.x, enemy.y, totalGoldAmt))
                }

                // Death Particles
                repeat(8) {
                    particles.add(
                        Particle(
                            x = enemy.x,
                            y = enemy.y,
                            vx = Random.nextFloat() * 120f - 60f,
                            vy = Random.nextFloat() * 120f - 60f,
                            life = 0.4f,
                            maxLife = 0.4f,
                            size = 6f,
                            color = cur.selectedStage.primaryParticleColor
                        )
                    )
                }
                return@mapNotNull null
            }

            // AI Movement towards player
            val edx = newPlayerX - enemy.x
            val edy = newPlayerY - enemy.y
            val eDist = sqrt(edx * edx + edy * edy)
            if (eDist > 5f) {
                enemy.x += (edx / eDist) * enemy.type.baseSpeed * effectiveDt
                enemy.y += (edy / eDist) * enemy.type.baseSpeed * effectiveDt
            }

            // Contact Damage to player
            if (eDist < (enemy.type.sizeRadius + 20f) && !isInv && hurtTimer <= 0f) {
                val dmgTaken = enemy.type.damage
                updatedHp = (updatedHp - dmgTaken).coerceAtLeast(0f)
                playerTookHit = true
                screenShakeIntensity = 8f
            }

            enemy
        }

        if (playerTookHit) {
            soundFx.playHurt()
        }

        // 10. XP Gem & Gold Magnet Pickup
        val magnetLevel = cur.skillLevels[SkillId.MAGNET_RING] ?: 0
        val magnetRadius = (100f + magnetLevel * 45f) * metaMagnetMultiplier

        var xpGained = 0
        val remainingGems = droppedGems.mapNotNull { gem ->
            val gdx = newPlayerX - gem.x
            val gdy = newPlayerY - gem.y
            val gDist = sqrt(gdx * gdx + gdy * gdy)

            if (gDist < 26f) {
                xpGained += gem.xpValue
                scoreGained += gem.xpValue * 5
                return@mapNotNull null
            }

            if (gDist < magnetRadius) {
                gem.x += (gdx / gDist) * 380f * effectiveDt
                gem.y += (gdy / gDist) * 380f * effectiveDt
            }
            gem
        }

        var goldGained = 0
        val remainingGold = droppedGold.mapNotNull { coin ->
            val cdx = newPlayerX - coin.x
            val cdy = newPlayerY - coin.y
            val cDist = sqrt(cdx * cdx + cdy * cdy)

            if (cDist < 26f) {
                goldGained += coin.amount
                soundFx.playGoldPickup()
                return@mapNotNull null
            }

            if (cDist < magnetRadius) {
                coin.x += (cdx / cDist) * 400f * effectiveDt
                coin.y += (cdy / cDist) * 400f * effectiveDt
            }
            coin
        }

        val remainingChests = droppedChests.mapNotNull { chest ->
            val chdx = newPlayerX - chest.x
            val chdy = newPlayerY - chest.y
            val chDist = sqrt(chdx * chdx + chdy * chdy)

            if (chDist < 35f) {
                goldGained += chest.goldReward
                soundFx.playChestOpen()
                return@mapNotNull null
            }
            chest
        }

        // 11. XP Level Up Calculation
        var newXp = cur.currentXp + xpGained
        var newLevel = cur.playerLevel
        var newXpNeeded = cur.xpNeeded
        var triggerLevelUp = false
        val levelCards = mutableListOf<SkillCardOption>()

        while (newXp >= newXpNeeded && newLevel < 50) {
            newXp -= newXpNeeded
            newLevel++
            newXpNeeded = (newXpNeeded * 1.35f + 10).toInt()
            triggerLevelUp = true
        }

        if (triggerLevelUp) {
            soundFx.playLevelUp()
            levelCards.addAll(generateSkillCards(cur.skillLevels))
        }

        // 12. Check Game Over or Phoenix Revive
        var isDead = updatedHp <= 0f
        var isGameOver = false
        var canReviveState = cur.canRevive

        if (isDead) {
            if (metaFreeRevive && canReviveState) {
                canReviveState = false
                updatedHp = cur.playerMaxHp * 0.6f
                isDead = false
                soundFx.playRevive()
                screenShakeIntensity = 12f
            } else {
                isGameOver = true
                soundFx.setBgmTrack(com.example.game.audio.BgmTrack.GAME_OVER)
            }
        }

        // 13. Update VFX Lifetimes
        val updatedParticles = particles.mapNotNull { p ->
            p.x += p.vx * effectiveDt
            p.y += p.vy * effectiveDt
            p.life -= effectiveDt
            if (p.life > 0f) p else null
        }

        val updatedDamageNumbers = damageNumbers.mapNotNull { d ->
            d.y -= 35f * effectiveDt
            d.life -= effectiveDt
            if (d.life > 0f) d else null
        }

        val updatedLightnings = lightnings.mapNotNull { l ->
            l.progress -= effectiveDt * 3.5f
            if (l.progress > 0f) l else null
        }

        // Boss state
        val boss = survivingEnemies.find { it.type.isBoss }

        _state.update { st ->
            st.copy(
                timeRemainingSeconds = newTimeRemaining,
                totalTimeSurvived = newTimeSurvived,
                playerX = newPlayerX,
                playerY = newPlayerY,
                moveDirection = Pair(normDx, normDy),
                isMoving = isMoving,
                playerWalkTime = newWalkTime,
                playerHp = updatedHp,
                playerHurtTimer = if (playerTookHit) 0.25f else hurtTimer,
                invincibleTimer = invTimer,
                isGameOver = isGameOver,
                isVictory = isVictorious,
                killCount = st.killCount + killsGained,
                goldCollectedInRun = st.goldCollectedInRun + goldGained,
                score = st.score + scoreGained,
                playerLevel = newLevel,
                currentXp = newXp,
                xpNeeded = newXpNeeded,
                isLevelUpPending = triggerLevelUp,
                pendingLevelUpCards = levelCards,
                fireOrbitAngle = fireOrbitAngle,
                fireOrbCount = activeFireCount,
                screenShake = screenShakeIntensity,
                canRevive = canReviveState,
                enemies = survivingEnemies,
                gems = remainingGems,
                goldCoins = remainingGold,
                chests = remainingChests,
                trails = updatedTrails,
                puddles = updatedPuddles,
                spinningAxes = updatedAxes,
                lightnings = updatedLightnings,
                particles = updatedParticles,
                damageNumbers = updatedDamageNumbers,
                bossActive = boss != null,
                bossHp = boss?.hp ?: 0f,
                bossMaxHp = boss?.maxHp ?: 0f
            )
        }
    }

    fun revivePlayer() {
        soundFx.playRevive()
        val stage = _state.value.selectedStage
        val stageTrack = when (stage) {
            GameStage.ENCHANTED_FOREST -> com.example.game.audio.BgmTrack.FOREST
            GameStage.MAGMA_CORE -> com.example.game.audio.BgmTrack.MAGMA
            GameStage.GLACIAL_FROST -> com.example.game.audio.BgmTrack.FROST
            GameStage.GOLDEN_TOMB -> com.example.game.audio.BgmTrack.TOMB
        }
        soundFx.setBgmTrack(if (bossSpawned) com.example.game.audio.BgmTrack.BOSS else stageTrack)
        _state.update {
            it.copy(
                playerHp = it.playerMaxHp * 0.7f,
                isGameOver = false,
                canRevive = false,
                isInvincible = true,
                invincibleTimer = 3.5f,
                screenShake = 10f
            )
        }
    }

    private fun spawnStageEnemyWave(
        stage: GameStage,
        timeSurvived: Float,
        px: Float,
        py: Float,
        outList: MutableList<Enemy>
    ) {
        val angle = Random.nextFloat() * 6.28f
        val distance = 420f + Random.nextFloat() * 120f
        val sx = px + cos(angle) * distance
        val sy = py + sin(angle) * distance

        val roll = Random.nextFloat()
        val type = when (stage) {
            GameStage.ENCHANTED_FOREST -> when {
                timeSurvived >= 180f && roll < 0.2f -> EnemyType.FOREST_GOLEM
                timeSurvived >= 100f && roll < 0.45f -> EnemyType.FLAME_BAT
                else -> EnemyType.SPORE_FUNGUS
            }
            GameStage.MAGMA_CORE -> when {
                timeSurvived >= 160f && roll < 0.25f -> EnemyType.LAVA_SKULL
                timeSurvived >= 80f && roll < 0.5f -> EnemyType.MAGMA_CRAB
                else -> EnemyType.FLAME_BAT
            }
            GameStage.GLACIAL_FROST -> when {
                timeSurvived >= 170f && roll < 0.22f -> EnemyType.ICE_GOLEM
                timeSurvived >= 90f && roll < 0.48f -> EnemyType.FROST_IMP
                else -> EnemyType.VOID_SPECTER
            }
            GameStage.GOLDEN_TOMB -> when {
                timeSurvived >= 180f && roll < 0.2f -> EnemyType.VOID_SPECTER
                timeSurvived >= 110f && roll < 0.45f -> EnemyType.TOMB_MUMMY
                else -> EnemyType.ANUBIS_SCARAB
            }
        }

        outList.add(
            Enemy(
                id = enemyIdSeq++,
                x = sx,
                y = sy,
                hp = type.baseHp * (1f + timeSurvived / 200f),
                maxHp = type.baseHp * (1f + timeSurvived / 200f),
                type = type
            )
        )
    }

    private fun generateSkillCards(currentSkills: Map<SkillId, Int>): List<SkillCardOption> {
        val allSkills = listOf(
            SkillCardOption(
                SkillId.CHAIN_LIGHTNING,
                currentSkills[SkillId.CHAIN_LIGHTNING] ?: 0,
                (currentSkills[SkillId.CHAIN_LIGHTNING] ?: 0) + 1,
                "Chain Lightning",
                "Tia Sét Định Vị",
                "Giật sét từ trời tiêu diệt quái gần nhất (+1 tia sét, +15 sát thương)",
                "lightning",
                isNew = !currentSkills.containsKey(SkillId.CHAIN_LIGHTNING)
            ),
            SkillCardOption(
                SkillId.FIRE_ORBIT,
                currentSkills[SkillId.FIRE_ORBIT] ?: 0,
                (currentSkills[SkillId.FIRE_ORBIT] ?: 0) + 1,
                "Fire Orbit",
                "Vòng Lửa Bảo Vệ",
                "Cầu lửa xoay quanh thiêu rụi kẻ địch (+1 cầu lửa, +bán kính)",
                "fire_orbit",
                isNew = !currentSkills.containsKey(SkillId.FIRE_ORBIT)
            ),
            SkillCardOption(
                SkillId.SPINNING_AXE,
                currentSkills[SkillId.SPINNING_AXE] ?: 0,
                (currentSkills[SkillId.SPINNING_AXE] ?: 0) + 1,
                "Spinning Axe",
                "Rìu Xoay Càn Quét",
                "Phóng rìu xoắn ốc xé nát quái vật (+sát thương, +tốc độ xoay)",
                "axe",
                isNew = !currentSkills.containsKey(SkillId.SPINNING_AXE)
            ),
            SkillCardOption(
                SkillId.HOLY_PUDDLE,
                currentSkills[SkillId.HOLY_PUDDLE] ?: 0,
                (currentSkills[SkillId.HOLY_PUDDLE] ?: 0) + 1,
                "Holy Puddle",
                "Bãi Độc Thánh",
                "Để lại vũng độc ma thuật trên đường đi (+sát thương độc)",
                "puddle",
                isNew = !currentSkills.containsKey(SkillId.HOLY_PUDDLE)
            ),
            SkillCardOption(
                SkillId.SPEED_BOOTS,
                currentSkills[SkillId.SPEED_BOOTS] ?: 0,
                (currentSkills[SkillId.SPEED_BOOTS] ?: 0) + 1,
                "Speed Boots",
                "Giày Tốc Độ",
                "Tăng 12% tốc độ di chuyển giúp né quái vật linh hoạt",
                "boots",
                isNew = !currentSkills.containsKey(SkillId.SPEED_BOOTS)
            ),
            SkillCardOption(
                SkillId.MAGNET_RING,
                currentSkills[SkillId.MAGNET_RING] ?: 0,
                (currentSkills[SkillId.MAGNET_RING] ?: 0) + 1,
                "Magnet Ring",
                "Nhẫn Hút Ngọc",
                "Mở rộng 45% bán kính hút ngọc kinh nghiệm và tiền vàng",
                "magnet",
                isNew = !currentSkills.containsKey(SkillId.MAGNET_RING)
            ),
            SkillCardOption(
                SkillId.MAX_HP_BOOST,
                currentSkills[SkillId.MAX_HP_BOOST] ?: 0,
                (currentSkills[SkillId.MAX_HP_BOOST] ?: 0) + 1,
                "Armor Slime",
                "Giáp Thạch Tinh Hoa",
                "Tăng thêm +25 Max HP và hồi ngay 25 HP",
                "shield",
                isNew = !currentSkills.containsKey(SkillId.MAX_HP_BOOST)
            ),
            SkillCardOption(
                SkillId.REGENERATION,
                currentSkills[SkillId.REGENERATION] ?: 0,
                (currentSkills[SkillId.REGENERATION] ?: 0) + 1,
                "Forest Heart",
                "Trái Tim Rừng Xanh",
                "Tự động hồi phục HP mỗi 2.5 giây",
                "heart",
                isNew = !currentSkills.containsKey(SkillId.REGENERATION)
            )
        )

        val evolutionCards = mutableListOf<SkillCardOption>()
        if ((currentSkills[SkillId.CHAIN_LIGHTNING] ?: 0) >= 4 && (currentSkills[SkillId.MAGNET_RING] ?: 0) >= 3) {
            evolutionCards.add(
                SkillCardOption(
                    SkillId.THUNDER_WRATH,
                    0, 1,
                    "Thunder Wrath (EVOLUTION)",
                    "⚡ TIẾN HÓA: CUỒNG NỘ THIÊN LÔI",
                    "Tiến hóa tối thượng! Sấm sét giật liên hoàn 5 mục tiêu với sát thương hủy diệt!",
                    "thunder_wrath",
                    isNew = true,
                    isEvolution = true
                )
            )
        }

        if ((currentSkills[SkillId.FIRE_ORBIT] ?: 0) >= 4 && (currentSkills[SkillId.SPEED_BOOTS] ?: 0) >= 3) {
            evolutionCards.add(
                SkillCardOption(
                    SkillId.SOLAR_SUPERNOVA,
                    0, 1,
                    "Solar Supernova (EVOLUTION)",
                    "🔥 TIẾN HÓA: SIÊU TÂN TINH MẶT TRỜI",
                    "Tiến hóa tối thượng! 4 mặt trời thu nhỏ xoay quanh thiêu rụi toàn bộ rừng già!",
                    "supernova",
                    isNew = true,
                    isEvolution = true
                )
            )
        }

        val pool = (evolutionCards + allSkills.filter { (currentSkills[it.skillId] ?: 0) < 5 }).shuffled()
        return pool.take(3)
    }

    fun applySkillUpgrade(card: SkillCardOption) {
        val cur = _state.value
        val updatedSkills = cur.skillLevels.toMutableMap()
        updatedSkills[card.skillId] = card.targetLevel

        var updatedMaxHp = cur.playerMaxHp
        var updatedHp = cur.playerHp
        var updatedFireCount = cur.fireOrbCount

        when (card.skillId) {
            SkillId.MAX_HP_BOOST -> {
                updatedMaxHp += 25f
                updatedHp = (updatedHp + 25f).coerceAtMost(updatedMaxHp)
            }
            SkillId.FIRE_ORBIT -> {
                updatedFireCount = when (card.targetLevel) {
                    in 1..3 -> 2
                    else -> 3
                }
            }
            SkillId.SOLAR_SUPERNOVA -> {
                updatedFireCount = 4
            }
            else -> {}
        }

        _state.update {
            it.copy(
                skillLevels = updatedSkills,
                playerMaxHp = updatedMaxHp,
                playerHp = updatedHp,
                fireOrbCount = updatedFireCount,
                isLevelUpPending = false,
                pendingLevelUpCards = emptyList()
            )
        }
    }
}
