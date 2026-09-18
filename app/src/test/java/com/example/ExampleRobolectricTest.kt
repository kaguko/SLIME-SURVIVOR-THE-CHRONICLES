package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.audio.SoundFxSynth
import com.example.game.engine.GameEngine
import com.example.game.model.SkillCardOption
import com.example.game.model.SkillId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
  fun `test game engine initial state and upgrade`() {
    val synth = SoundFxSynth().apply { isMuted = true }
    val engine = GameEngine(synth)
    
    val initialState = engine.state.value
    assertEquals(100f, initialState.playerHp, 0.01f)
    assertEquals(1, initialState.playerLevel)
    assertEquals(300f, initialState.timeRemainingSeconds, 0.01f)

    // Test Movement input
    engine.setMovementInput(1f, 0f)
    assertTrue(engine.state.value.isMoving)

    // Test Upgrade Skill
    val card = SkillCardOption(
      skillId = SkillId.FIRE_ORBIT,
      currentLevel = 0,
      targetLevel = 1,
      title = "FIRE ORBIT",
      vietnameseTitle = "Vòng Lửa Bảo Vệ",
      description = "Orbital flames",
      iconName = "fire_orbit",
      isNew = true
    )
    engine.applySkillUpgrade(card)
    assertEquals(1, engine.state.value.skillLevels[SkillId.FIRE_ORBIT])
  }
}
