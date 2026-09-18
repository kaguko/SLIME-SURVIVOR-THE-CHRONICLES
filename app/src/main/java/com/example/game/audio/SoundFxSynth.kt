package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin

/**
 * 8-Bit Retro Sound Effects & Background Music Synthesizer
 * Uses real-time PCM synthesis on AudioTrack for zero latency and instant responsiveness.
 */
class SoundFxSynth {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isMuted: Boolean = false
    var isBgmMuted: Boolean = false
    
    private var bgmJob: Job? = null
    private var bgmTrack: AudioTrack? = null

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
                
                delay((samples.size * 1000L / sampleRate) + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    fun startBgm() {
        if (bgmJob?.isActive == true || isBgmMuted) return
        bgmJob = scope.launch {
            val sampleRate = 22050
            val melody = doubleArrayOf(
                261.63, 329.63, 392.00, 523.25, // C4 E4 G4 C5
                293.66, 349.23, 440.00, 587.33, // D4 F4 A4 D5
                329.63, 392.00, 493.88, 659.25, // E4 G4 B4 E5
                261.63, 392.00, 329.63, 523.25  // C4 G4 E4 C5
            )
            val bass = doubleArrayOf(130.81, 146.83, 164.81, 130.81) // C3, D3, E3, C3

            val noteDurationMs = 180
            val samplesPerNote = (sampleRate * noteDurationMs) / 1000

            try {
                val bufferSize = samplesPerNote * 2 * 4
                bgmTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack?.play()

                var step = 0
                while (isActive && !isBgmMuted) {
                    val buffer = ShortArray(samplesPerNote)
                    val leadFreq = melody[step % melody.size]
                    val bassFreq = bass[(step / 4) % bass.size]

                    var leadPhase = 0.0
                    var bassPhase = 0.0

                    for (i in 0 until samplesPerNote) {
                        val progress = i.toDouble() / samplesPerNote
                        val envelope = (1.0 - progress * 0.3)

                        leadPhase += 2.0 * Math.PI * leadFreq / sampleRate
                        bassPhase += 2.0 * Math.PI * bassFreq / sampleRate

                        // 8-bit pulse wave + bass triangle
                        val leadSample = if (sin(leadPhase) > 0) 0.15 else -0.15
                        val bassSample = sin(bassPhase) * 0.25

                        val mixed = (leadSample + bassSample) * envelope
                        buffer[i] = (mixed * Short.MAX_VALUE * 0.4).toInt().toShort()
                    }

                    bgmTrack?.write(buffer, 0, buffer.size)
                    step++
                }
            } catch (_: Exception) {
            } finally {
                try {
                    bgmTrack?.stop()
                    bgmTrack?.release()
                    bgmTrack = null
                } catch (_: Exception) {}
            }
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        try {
            bgmTrack?.stop()
            bgmTrack?.release()
            bgmTrack = null
        } catch (_: Exception) {}
    }

    fun playLightning() {
        playToneBuffer { sampleRate ->
            val durationMs = 120
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 1800.0 - (progress * 1300.0)
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
                val freq = 1100.0 + (progress * 800.0)
                val envelope = 1.0 - progress
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = sin(phase)
                buffer[i] = (raw * Short.MAX_VALUE * 0.4 * envelope).toInt().toShort()
            }
            buffer
        }
    }

    fun playGoldPickup() {
        playToneBuffer { sampleRate ->
            val durationMs = 100
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 1400.0 + (progress * 900.0)
                val envelope = 1.0 - progress
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = if (sin(phase) > 0) 0.3 else -0.3
                buffer[i] = (raw * Short.MAX_VALUE * 0.35 * envelope).toInt().toShort()
            }
            buffer
        }
    }

    fun playChestOpen() {
        playToneBuffer { sampleRate ->
            val durationMs = 450
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0) // A4 C#5 E5 A5
            val samplesPerNote = numSamples / notes.size
            var phase = 0.0
            for (i in 0 until numSamples) {
                val noteIdx = (i / samplesPerNote).coerceIn(0, notes.size - 1)
                val freq = notes[noteIdx]
                val noteProgress = (i % samplesPerNote).toDouble() / samplesPerNote
                val env = 1.0 - (noteProgress * 0.3)
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = if (sin(phase) > 0) 0.5 else -0.5
                buffer[i] = (raw * Short.MAX_VALUE * 0.55 * env).toInt().toShort()
            }
            buffer
        }
    }

    fun playRevive() {
        playToneBuffer { sampleRate ->
            val durationMs = 500
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 300.0 + (progress * 900.0)
                val envelope = sin(progress * Math.PI)
                phase += 2.0 * Math.PI * freq / sampleRate
                val raw = sin(phase)
                buffer[i] = (raw * Short.MAX_VALUE * 0.6 * envelope).toInt().toShort()
            }
            buffer
        }
    }

    fun playLevelUp() {
        playToneBuffer { sampleRate ->
            val durationMs = 400
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            val samplesPerNote = numSamples / notes.size
            var phase = 0.0

            for (i in 0 until numSamples) {
                val noteIdx = (i / samplesPerNote).coerceIn(0, notes.size - 1)
                val freq = notes[noteIdx]
                val noteProgress = (i % samplesPerNote).toDouble() / samplesPerNote
                val env = 1.0 - (noteProgress * 0.4)
                phase += 2.0 * Math.PI * freq / sampleRate
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

    fun playButtonClick() {
        playToneBuffer { sampleRate ->
            val durationMs = 40
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var phase = 0.0
            for (i in 0 until numSamples) {
                phase += 2.0 * Math.PI * 950.0 / sampleRate
                val raw = sin(phase) * (1.0 - (i.toDouble() / numSamples))
                buffer[i] = (raw * Short.MAX_VALUE * 0.3).toInt().toShort()
            }
            buffer
        }
    }
}
