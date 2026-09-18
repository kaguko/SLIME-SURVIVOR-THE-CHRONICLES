package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

enum class BgmTrack(val title: String, val vietnameseTitle: String, val bpm: Int) {
    MENU("Hero Awakening", "Khúc Ca Thức Tỉnh", 128),
    FOREST("Enchanted Whispers", "Rừng Thiêng Huyền Bí", 132),
    MAGMA("Molten Inferno", "Hỏa Diệm Dung Nham", 148),
    FROST("Glacial Blizzard", "Bão Tuyết Băng Cực", 140),
    TOMB("Pharaoh's Curse", "Lời Nguyền Kim Tự Tháp", 136),
    BOSS("Dragon Sovereign Clash", "Đại Chiến Ma Vương", 160),
    VICTORY("Triumph of the Slime", "Khải Hoàn Sử Thi", 144),
    GAME_OVER("Rest in Slime", "Khúc Nguyện Yên Nghỉ", 100)
}

/**
 * 8-Bit Retro Sound Effects & Multi-Track Polyphonic BGM Synthesizer
 * Uses an isolated background audio thread to guarantee zero UI lockup or coroutine thread pool exhaustion.
 */
class SoundFxSynth {
    // Dedicated isolated single-thread dispatcher so audio operations NEVER block the main app thread pool
    private val audioDispatcher = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "SlimeAudioSynth").apply {
            priority = Thread.MIN_PRIORITY
            isDaemon = true
        }
    }.asCoroutineDispatcher()

    private val scope = CoroutineScope(SupervisorJob() + audioDispatcher)
    
    var isMuted: Boolean = false
    var isBgmMuted: Boolean = false
    
    @Volatile
    var currentTrack: BgmTrack = BgmTrack.MENU
        private set

    private var bgmJob: Job? = null
    private var bgmTrack: AudioTrack? = null

    private fun playToneBuffer(sampleRate: Int = 22050, bufferGenerator: (Int) -> ShortArray) {
        if (isMuted) return
        scope.launch {
            try {
                val samples = bufferGenerator(sampleRate)
                val bufferBytes = samples.size * 2
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(bufferBytes)

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
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioTrack.write(samples, 0, samples.size, AudioTrack.WRITE_NON_BLOCKING)
                } else {
                    audioTrack.write(samples, 0, samples.size)
                }
                
                audioTrack.play()
                delay((samples.size * 1000L / sampleRate).coerceAtLeast(50L) + 30L)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Throwable) {}
            } catch (_: Throwable) {}
        }
    }

    fun setBgmTrack(track: BgmTrack) {
        if (currentTrack == track && bgmJob?.isActive == true) return
        currentTrack = track
        stopBgm()
        if (!isBgmMuted) {
            startBgm(track)
        }
    }

    fun startBgm(track: BgmTrack = currentTrack) {
        currentTrack = track
        if (bgmJob?.isActive == true || isBgmMuted) return

        bgmJob = scope.launch {
            val sampleRate = 22050
            val (melodyNotes, bassNotes, noteDurationMs, waveStyle) = getTrackComposition(currentTrack)
            val samplesPerNote = ((sampleRate * noteDurationMs) / 1000).coerceAtLeast(100)

            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(samplesPerNote * 4)

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
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack?.play()

                var step = 0
                var leadPhase = 0.0
                var harmonyPhase = 0.0
                var bassPhase = 0.0

                while (isActive && !isBgmMuted) {
                    val buffer = ShortArray(samplesPerNote)
                    val leadFreq = melodyNotes[step % melodyNotes.size]
                    val harmonyFreq = leadFreq * 1.25
                    val bassFreq = bassNotes[(step / 2) % bassNotes.size]
                    val isBeat = (step % 2 == 0)

                    for (i in 0 until samplesPerNote) {
                        val progress = i.toDouble() / samplesPerNote
                        val envelope = (1.0 - progress * 0.25)

                        leadPhase += 2.0 * PI * leadFreq / sampleRate
                        harmonyPhase += 2.0 * PI * harmonyFreq / sampleRate
                        bassPhase += 2.0 * PI * bassFreq / sampleRate

                        val leadSample = when (waveStyle) {
                            1 -> if (sin(leadPhase) > 0.2) 0.18 else -0.18
                            2 -> (sin(leadPhase) * 0.15) + (if (sin(leadPhase * 2) > 0) 0.08 else -0.08)
                            else -> if (sin(leadPhase) > 0) 0.16 else -0.16
                        }
                        val harmonySample = sin(harmonyPhase) * 0.08
                        val bassSample = sin(bassPhase) * 0.28 * (1.0 - progress * 0.4)

                        val drumSample = if (isBeat && progress < 0.15) {
                            (Random.nextDouble(-0.12, 0.12) + sin(progress * PI * 8) * 0.2) * (1.0 - progress / 0.15)
                        } else 0.0

                        val mixed = (leadSample + harmonySample + bassSample + drumSample) * envelope
                        buffer[i] = (mixed * Short.MAX_VALUE * 0.45).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        bgmTrack?.write(buffer, 0, buffer.size, AudioTrack.WRITE_NON_BLOCKING)
                    } else {
                        bgmTrack?.write(buffer, 0, buffer.size)
                    }
                    
                    delay(noteDurationMs.toLong())
                    step++
                }
            } catch (_: Throwable) {
            } finally {
                try {
                    bgmTrack?.stop()
                    bgmTrack?.release()
                    bgmTrack = null
                } catch (_: Throwable) {}
            }
        }
    }

    private data class TrackData(
        val melody: DoubleArray,
        val bass: DoubleArray,
        val noteMs: Int,
        val waveStyle: Int
    )

    private fun getTrackComposition(track: BgmTrack): TrackData {
        return when (track) {
            BgmTrack.MENU -> TrackData(
                melody = doubleArrayOf(
                    261.63, 329.63, 392.00, 523.25, 440.00, 392.00, 329.63, 392.00,
                    293.66, 349.23, 440.00, 587.33, 523.25, 440.00, 349.23, 392.00,
                    329.63, 392.00, 493.88, 659.25, 587.33, 493.88, 392.00, 440.00,
                    523.25, 659.25, 783.99, 1046.50, 783.99, 659.25, 523.25, 392.00
                ),
                bass = doubleArrayOf(130.81, 146.83, 164.81, 130.81, 174.61, 196.00, 130.81, 196.00),
                noteMs = 170,
                waveStyle = 0
            )
            BgmTrack.FOREST -> TrackData(
                melody = doubleArrayOf(
                    329.63, 392.00, 440.00, 493.88, 587.33, 493.88, 440.00, 392.00,
                    293.66, 349.23, 440.00, 523.25, 440.00, 349.23, 293.66, 329.63,
                    329.63, 493.88, 659.25, 587.33, 493.88, 440.00, 392.00, 329.63,
                    261.63, 329.63, 392.00, 493.88, 440.00, 392.00, 329.63, 293.66
                ),
                bass = doubleArrayOf(164.81, 146.83, 130.81, 146.83, 164.81, 196.00, 146.83, 164.81),
                noteMs = 160,
                waveStyle = 1
            )
            BgmTrack.MAGMA -> TrackData(
                melody = doubleArrayOf(
                    220.00, 246.94, 261.63, 329.63, 293.66, 261.63, 246.94, 220.00,
                    174.61, 220.00, 261.63, 329.63, 349.23, 329.63, 261.63, 220.00,
                    246.94, 293.66, 349.23, 440.00, 392.00, 349.23, 293.66, 246.94,
                    220.00, 261.63, 329.63, 440.00, 392.00, 329.63, 261.63, 220.00
                ),
                bass = doubleArrayOf(110.00, 123.47, 130.81, 110.00, 87.31, 110.00, 130.81, 110.00),
                noteMs = 145,
                waveStyle = 2
            )
            BgmTrack.FROST -> TrackData(
                melody = doubleArrayOf(
                    392.00, 440.00, 523.25, 587.33, 659.25, 587.33, 523.25, 440.00,
                    349.23, 392.00, 440.00, 523.25, 587.33, 523.25, 440.00, 392.00,
                    440.00, 523.25, 659.25, 783.99, 880.00, 783.99, 659.25, 523.25,
                    329.63, 392.00, 493.88, 587.33, 659.25, 587.33, 493.88, 392.00
                ),
                bass = doubleArrayOf(196.00, 174.61, 220.00, 164.81, 196.00, 174.61, 220.00, 164.81),
                noteMs = 155,
                waveStyle = 0
            )
            BgmTrack.TOMB -> TrackData(
                melody = doubleArrayOf(
                    293.66, 311.13, 369.99, 392.00, 440.00, 392.00, 369.99, 311.13,
                    293.66, 369.99, 440.00, 493.88, 587.33, 493.88, 440.00, 369.99,
                    311.13, 369.99, 440.00, 554.37, 587.33, 554.37, 440.00, 369.99,
                    293.66, 311.13, 369.99, 440.00, 392.00, 369.99, 311.13, 293.66
                ),
                bass = doubleArrayOf(146.83, 155.56, 185.00, 146.83, 155.56, 185.00, 220.00, 146.83),
                noteMs = 160,
                waveStyle = 2
            )
            BgmTrack.BOSS -> TrackData(
                melody = doubleArrayOf(
                    220.00, 220.00, 440.00, 415.30, 392.00, 369.99, 349.23, 329.63,
                    220.00, 246.94, 261.63, 293.66, 329.63, 369.99, 440.00, 523.25,
                    587.33, 554.37, 523.25, 493.88, 440.00, 392.00, 349.23, 329.63,
                    220.00, 293.66, 369.99, 440.00, 587.33, 440.00, 369.99, 293.66
                ),
                bass = doubleArrayOf(110.00, 110.00, 130.81, 146.83, 110.00, 110.00, 164.81, 146.83),
                noteMs = 125,
                waveStyle = 2
            )
            BgmTrack.VICTORY -> TrackData(
                melody = doubleArrayOf(
                    293.66, 369.99, 440.00, 587.33, 587.33, 587.33, 739.99, 880.00,
                    659.25, 739.99, 880.00, 1108.73, 1174.66, 1174.66, 880.00, 1174.66
                ),
                bass = doubleArrayOf(146.83, 146.83, 185.00, 220.00, 146.83, 220.00, 293.66, 146.83),
                noteMs = 190,
                waveStyle = 0
            )
            BgmTrack.GAME_OVER -> TrackData(
                melody = doubleArrayOf(
                    440.00, 415.30, 392.00, 369.99, 349.23, 329.63, 293.66, 261.63, 220.00
                ),
                bass = doubleArrayOf(110.00, 103.83, 98.00, 92.50, 87.31, 82.41, 73.42, 65.41, 55.00),
                noteMs = 280,
                waveStyle = 0
            )
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        try {
            bgmTrack?.stop()
            bgmTrack?.release()
            bgmTrack = null
        } catch (_: Throwable) {}
    }

    fun playLightning() {
        playToneBuffer { sampleRate ->
            val durationMs = 120
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val noise = Random.nextDouble(-1.0, 1.0)
                val freq = 440.0 * (1.0 - (i.toDouble() / samples) * 0.7)
                val tone = sin(2.0 * PI * freq * i / sampleRate)
                val mixed = (noise * 0.65 + tone * 0.35) * (1.0 - i.toDouble() / samples)
                buffer[i] = (mixed * Short.MAX_VALUE * 0.45).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playFireOrb() {
        playToneBuffer { sampleRate ->
            val durationMs = 90
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val freq = 180.0 + (i.toDouble() / samples) * 140.0
                val sample = sin(2.0 * PI * freq * i / sampleRate) * (1.0 - i.toDouble() / samples)
                buffer[i] = (sample * Short.MAX_VALUE * 0.35).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playAxeSpin() {
        playToneBuffer { sampleRate ->
            val durationMs = 100
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val freq = 120.0 + sin(i.toDouble() / samples * PI) * 160.0
                val square = if (sin(2.0 * PI * freq * i / sampleRate) > 0) 0.25 else -0.25
                val sample = square * (1.0 - i.toDouble() / samples)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playHolyWater() {
        playToneBuffer { sampleRate ->
            val durationMs = 140
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val freq = 523.25 + (i.toDouble() / samples) * 260.0
                val sample = sin(2.0 * PI * freq * i / sampleRate) * sin(i.toDouble() / samples * PI)
                buffer[i] = (sample * Short.MAX_VALUE * 0.4).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playHitMonster() {
        playToneBuffer { sampleRate ->
            val durationMs = 50
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val noise = Random.nextDouble(-0.5, 0.5) * (1.0 - i.toDouble() / samples)
                buffer[i] = (noise * Short.MAX_VALUE * 0.4).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playPlayerHurt() {
        playToneBuffer { sampleRate ->
            val durationMs = 110
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val freq = 180.0 * (1.0 - (i.toDouble() / samples) * 0.6)
                val square = if (sin(2.0 * PI * freq * i / sampleRate) > 0) 0.3 else -0.3
                val sample = square * (1.0 - i.toDouble() / samples)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playGemPickup() {
        playToneBuffer { sampleRate ->
            val durationMs = 80
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val freq = if (i < samples / 2) 784.0 else 1046.5
                val sample = sin(2.0 * PI * freq * i / sampleRate) * (1.0 - i.toDouble() / samples)
                buffer[i] = (sample * Short.MAX_VALUE * 0.35).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playLevelUp() {
        playToneBuffer { sampleRate ->
            val durationMs = 280
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            val noteLen = samples / notes.size
            for (i in 0 until samples) {
                val noteIdx = (i / noteLen).coerceIn(0, notes.size - 1)
                val freq = notes[noteIdx]
                val sample = sin(2.0 * PI * freq * i / sampleRate) * (1.0 - (i % noteLen).toDouble() / noteLen * 0.5)
                buffer[i] = (sample * Short.MAX_VALUE * 0.45).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playChestOpen() {
        playToneBuffer { sampleRate ->
            val durationMs = 240
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            val notes = doubleArrayOf(392.00, 523.25, 659.25, 783.99, 1046.50)
            val noteLen = samples / notes.size
            for (i in 0 until samples) {
                val noteIdx = (i / noteLen).coerceIn(0, notes.size - 1)
                val freq = notes[noteIdx]
                val pulse = if (sin(2.0 * PI * freq * i / sampleRate) > 0.3) 0.3 else -0.3
                val sample = pulse * (1.0 - (i % noteLen).toDouble() / noteLen * 0.4)
                buffer[i] = (sample * Short.MAX_VALUE * 0.4).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playBossRoar() {
        playToneBuffer { sampleRate ->
            val durationMs = 380
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val progress = i.toDouble() / samples
                val freq = 65.0 + sin(progress * PI * 4) * 25.0
                val noise = Random.nextDouble(-0.35, 0.35)
                val tone = sin(2.0 * PI * freq * i / sampleRate) * 0.6
                val sample = (tone + noise) * (1.0 - progress * 0.3)
                buffer[i] = (sample * Short.MAX_VALUE * 0.55).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playRevive() {
        playToneBuffer { sampleRate ->
            val durationMs = 350
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val progress = i.toDouble() / samples
                val freq = 220.0 + progress * 660.0
                val tone = sin(2.0 * PI * freq * i / sampleRate) * (1.0 - progress * 0.2)
                buffer[i] = (tone * Short.MAX_VALUE * 0.45).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playAxeSlash() = playAxeSpin()
    fun playHurt() = playPlayerHurt()
    fun playGoldPickup() = playGemPickup()

    fun playButtonClick() {
        playToneBuffer { sampleRate ->
            val durationMs = 35
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val freq = 650.0 + (i.toDouble() / samples) * 200.0
                val sample = sin(2.0 * PI * freq * i / sampleRate) * (1.0 - i.toDouble() / samples)
                buffer[i] = (sample * Short.MAX_VALUE * 0.3).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }
}
