package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * 8-Bit Retro Sound Effects Generator using pure PCM AudioTrack.
 * Zero asset loading required, ultra low latency and instant responsive playback!
 */
class SoundFxSynth {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isMuted: Boolean = false

    private fun playToneBuffer(sampleRate: Int = 22050, bufferGenerator: (Int) -> ShortArray) {
        if (isMuted) return
        scope.launch {
            try {
                val samples = bufferGenerator(sampleRate)
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                
                // Release after playback finished
                kotlinx.coroutines.delay((samples.size * 1000L / sampleRate) + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    fun playLightning() {
        playToneBuffer { sampleRate ->
            val durationMs = 120
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 1800.0 - (progress * 1300.0) // 1800Hz down to 500Hz
                val envelope = 1.0 - progress
                val noise = if (i % 3 == 0) (Math.random() * 0.4 - 0.2) else 0.0
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = (sin(phase) + noise).coerceIn(-1.0, 1.0)
                buffer[i] = (raw * Short.MAX_VALUE * 0.5 * envelope).toInt().toShort()
            }
            buffer
        }
    }

    fun playGemPickup() {
        playToneBuffer { sampleRate ->
            val durationMs = 90
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 1100.0 + (progress * 800.0) // 1100Hz -> 1900Hz sparkle
                val envelope = 1.0 - progress
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = sin(phase)
                buffer[i] = (raw * Short.MAX_VALUE * 0.4 * envelope).toInt().toShort()
            }
            buffer
        }
    }

    fun playLevelUp() {
        playToneBuffer { sampleRate ->
            val durationMs = 400
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            val samplesPerNote = numSamples / notes.size
            var phase = 0.0

            for (i in 0 until numSamples) {
                val noteIdx = (i / samplesPerNote).coerceIn(0, notes.size - 1)
                val freq = notes[noteIdx]
                val noteProgress = (i % samplesPerNote).toDouble() / samplesPerNote
                val env = 1.0 - (noteProgress * 0.4)
                phase += 2.0 * Math.PI * freq / sampleRate
                // Square-ish wave for 8-bit chip feeling
                val raw = if (sin(phase) > 0) 0.6 else -0.6
                buffer[i] = (raw * Short.MAX_VALUE * 0.5 * env).toInt().toShort()
            }
            buffer
        }
    }

    fun playHurt() {
        playToneBuffer { sampleRate ->
            val durationMs = 140
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 160.0 - (progress * 80.0)
                val envelope = 1.0 - progress
                val noise = (Math.random() * 2.0 - 1.0) * 0.5
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = (sin(phase) * 0.5 + noise).coerceIn(-1.0, 1.0)
                buffer[i] = (raw * Short.MAX_VALUE * 0.6 * envelope).toInt().toShort()
            }
            buffer
        }
    }

    fun playFireOrbit() {
        playToneBuffer { sampleRate ->
            val durationMs = 150
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 400.0 + sin(progress * Math.PI * 4) * 80.0
                val envelope = sin(progress * Math.PI)
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = sin(phase)
                buffer[i] = (raw * Short.MAX_VALUE * 0.35 * envelope).toInt().toShort()
            }
            buffer
        }
    }

    fun playAxeSlash() {
        playToneBuffer { sampleRate ->
            val durationMs = 180
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 750.0 - (progress * 450.0)
                val envelope = (1.0 - progress) * (1.0 - progress)
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = if (sin(phase) > 0) 0.5 else -0.5
                buffer[i] = (raw * Short.MAX_VALUE * 0.45 * envelope).toInt().toShort()
            }
            buffer
        }
    }

    fun playBossRoar() {
        playToneBuffer { sampleRate ->
            val durationMs = 600
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 90.0 + sin(progress * Math.PI * 8) * 35.0
                val envelope = sin(progress * Math.PI)
                val rumble = (Math.random() * 2.0 - 1.0) * 0.4
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = (sin(phase) + rumble).coerceIn(-1.0, 1.0)
                buffer[i] = (raw * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
            }
            buffer
        }
    }
}
