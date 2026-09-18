package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.game.audio.SoundFxSynth
import com.example.game.engine.GameEngine
import com.example.game.model.*
import com.example.ui.theme.FireRed
import com.example.ui.theme.SlimeBlue
import com.example.util.AppLanguage
import com.example.util.Localization
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Slime Survivor", appName)
  }

  @Test
  fun `test game engine initial state and hero selection`() {
    val synth = SoundFxSynth().apply { isMuted = true; isBgmMuted = true }
    val engine = GameEngine(synth)
    
    // Test Knight Slime
    engine.resetGame(SlimeHero.KNIGHT_SLIME, GameStage.ENCHANTED_FOREST)
    val state = engine.state.value
    assertEquals(120f, state.playerHp, 0.01f)
    assertEquals(1, state.playerLevel)
    assertEquals(300f, state.timeRemainingSeconds, 0.01f)
    assertEquals(1, state.skillLevels[SkillId.CHAIN_LIGHTNING])
    assertEquals(GameStage.ENCHANTED_FOREST, state.selectedStage)

    // Test Fire Slime on Magma Stage
    engine.resetGame(SlimeHero.FIRE_SLIME, GameStage.MAGMA_CORE)
    val fireState = engine.state.value
    assertEquals(90f, fireState.playerHp, 0.01f)
    assertEquals(1, fireState.skillLevels[SkillId.FIRE_ORBIT])
    assertEquals(2, fireState.fireOrbCount)
    assertEquals(GameStage.MAGMA_CORE, fireState.selectedStage)
  }

  @Test
  fun `test movement and joystick input`() {
    val synth = SoundFxSynth().apply { isMuted = true; isBgmMuted = true }
    val engine = GameEngine(synth)
    engine.resetGame(SlimeHero.WIND_SLIME, GameStage.GLACIAL_FROST)

    engine.setMovementInput(0.8f, 0.6f)
    engine.update(0.1f)

    val updatedState = engine.state.value
    assertTrue(updatedState.isMoving)
    assertTrue(updatedState.playerX > 0f)
    assertTrue(updatedState.playerY > 0f)
  }

  @Test
  fun `test skill upgrade and evolution`() {
    val synth = SoundFxSynth().apply { isMuted = true; isBgmMuted = true }
    val engine = GameEngine(synth)
    engine.resetGame(SlimeHero.KNIGHT_SLIME, GameStage.GOLDEN_TOMB)

    // Upgrade Max HP
    val hpCard = SkillCardOption(
      skillId = SkillId.MAX_HP_BOOST,
      currentLevel = 0,
      targetLevel = 1,
      title = "Max HP",
      vietnameseTitle = "Giáp Thạch Tinh Hoa",
      description = "+25 Max HP",
      iconName = "shield",
      isNew = true
    )
    engine.applySkillUpgrade(hpCard)
    assertEquals(145f, engine.state.value.playerMaxHp, 0.01f)

    // Evolution Upgrade to Thunder Wrath
    val evolutionCard = SkillCardOption(
      skillId = SkillId.THUNDER_WRATH,
      currentLevel = 0,
      targetLevel = 1,
      title = "Thunder Wrath",
      vietnameseTitle = "Cuồng Nộ Thiên Lôi",
      description = "Evolution",
      iconName = "thunder_wrath",
      isNew = true,
      isEvolution = true
    )
    engine.applySkillUpgrade(evolutionCard)
    assertEquals(1, engine.state.value.skillLevels[SkillId.THUNDER_WRATH])
  }

  @Test
  fun `test revive with radiant shield`() {
    val synth = SoundFxSynth().apply { isMuted = true; isBgmMuted = true }
    val engine = GameEngine(synth)
    engine.resetGame(SlimeHero.KNIGHT_SLIME)

    engine.revivePlayer()
    val st = engine.state.value
    assertTrue(st.isInvincible)
    assertTrue(st.invincibleTimer > 0f)
    assertFalse(st.canRevive)
    assertTrue(st.playerHp > 50f)
  }

  @Test
  fun `test all 4 stages and 10 enemy types`() {
    assertEquals(4, GameStage.values().size)
    assertTrue(EnemyType.values().size >= 10)
    assertTrue(EnemyType.values().count { it.isBoss } >= 4)
    assertEquals(13, AchievementId.values().size)
  }

  @Test
  fun `test localization dictionary`() {
    Localization.setLanguage(AppLanguage.VIETNAMESE)
    assertEquals("SLIME SURVIVOR", Localization.get("app_title"))
    assertEquals("VÀO TRẬN CHIẾN", Localization.get("start_run"))

    Localization.setLanguage(AppLanguage.ENGLISH)
    assertEquals("START SURVIVAL RUN", Localization.get("start_run"))
  }

  @Test
  fun `test save and load restoration state`() {
    val synth = SoundFxSynth().apply { isMuted = true; isBgmMuted = true }
    val engine = GameEngine(synth)

    val customSkills = mapOf(
      SkillId.FIRE_ORBIT to 3,
      SkillId.HOLY_PUDDLE to 2,
      SkillId.SPEED_BOOTS to 2
    )

    engine.restoreSavedState(
      hero = SlimeHero.FIRE_SLIME,
      stage = GameStage.MAGMA_CORE,
      hp = 75f,
      maxHp = 135f,
      level = 7,
      xp = 140,
      xpNeeded = 320,
      timeRemainingSec = 195f,
      timeSurvivedSec = 105f,
      kills = 142,
      gold = 58,
      score = 4200,
      skills = customSkills,
      canRevive = false
    )

    val st = engine.state.value
    assertEquals(SlimeHero.FIRE_SLIME, st.selectedHero)
    assertEquals(GameStage.MAGMA_CORE, st.selectedStage)
    assertEquals(75f, st.playerHp, 0.01f)
    assertEquals(135f, st.playerMaxHp, 0.01f)
    assertEquals(7, st.playerLevel)
    assertEquals(140, st.currentXp)
    assertEquals(320, st.xpNeeded)
    assertEquals(195f, st.timeRemainingSeconds, 0.01f)
    assertEquals(105f, st.totalTimeSurvived, 0.01f)
    assertEquals(142, st.killCount)
    assertEquals(58, st.goldCollectedInRun)
    assertEquals(4200, st.score)
    assertEquals(2, st.fireOrbCount) // Level 3 = 2 orbs (Level 4 gives 3, Supernova gives 4)
    assertFalse(st.canRevive)
    assertEquals(3, st.skillLevels[SkillId.FIRE_ORBIT])
    assertEquals(2, st.skillLevels[SkillId.HOLY_PUDDLE])
  }

  @Test
  fun `test accessibility colorblind transformations`() {
    val normalSettings = GameSettings(colorblindMode = ColorblindMode.NORMAL)
    val deuteranopiaSettings = GameSettings(colorblindMode = ColorblindMode.DEUTERANOPIA)
    val protanopiaSettings = GameSettings(colorblindMode = ColorblindMode.PROTANOPIA)
    val highContrastSettings = GameSettings(colorblindMode = ColorblindMode.HIGH_CONTRAST)

    val original = FireRed
    val normalAdj = normalSettings.adjustColor(original)
    assertEquals(original, normalAdj)

    val deutAdj = deuteranopiaSettings.adjustColor(original)
    assertNotEquals(original, deutAdj)

    val protAdj = protanopiaSettings.adjustColor(original)
    assertNotEquals(original, protAdj)

    val hcAdj = highContrastSettings.adjustColor(SlimeBlue)
    assertNotNull(hcAdj)
  }

  @Test
  fun `test battery saver optimization`() {
    val synth = SoundFxSynth().apply { isMuted = true; isBgmMuted = true }
    val engine = GameEngine(synth)
    engine.isBatterySaver = true
    engine.resetGame(SlimeHero.KNIGHT_SLIME, GameStage.ENCHANTED_FOREST)

    // Run engine updates
    engine.update(1.0f)
    assertTrue(engine.state.value.enemies.size <= 45)
  }

  @Test
  fun `test playtest telemetry dataset`() {
    assertEquals(34, PlaytestTelemetryData.totalPlaytestSessions)
    assertEquals(4.8f, PlaytestTelemetryData.averageSatisfactionRating, 0.01f)
    assertEquals(14, PlaytestTelemetryData.totalBugsIdentifiedAndFixed)
    assertTrue(PlaytestTelemetryData.stageStats.size >= 4)
    assertTrue(PlaytestTelemetryData.weaponTiers.size >= 8)
    assertTrue(PlaytestTelemetryData.simulatedReviews.size >= 6)
  }
}
