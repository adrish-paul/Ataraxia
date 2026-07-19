package com.example.ataraxia.features.breathe.presentation

import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import com.example.ataraxia.R

class AmbientSoundEngine {
    private var mediaPlayer: MediaPlayer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentSound: String = "None"
    private var currentVolume: Float = 0.5f
    private var isPausedState: Boolean = false

    fun start(context: Context, sound: String, multiplier: Float) {
        if (sound == "None") {
            stop()
            return
        }
        
        currentVolume = multiplier
        
        if (currentSound == sound) {
            // Already playing this sound, just adjust volume and resume if paused
            setPaused(false)
            setVolume(multiplier)
            return
        }

        // Stop current sound first
        stop()

        currentSound = sound
        val resId = when (sound) {
            "Rain" -> R.raw.rain
            "Ocean" -> R.raw.ocean
            "Wind" -> R.raw.wind
            "Fireplace" -> R.raw.fireplace
            "Night" -> R.raw.night
            "Forest" -> R.raw.forest
            else -> null
        }

        if (resId != null) {
            try {
                mediaPlayer = MediaPlayer.create(context, resId).apply {
                    isLooping = true
                    setVolume(multiplier, multiplier)

                    // Attach LoudnessEnhancer to boost low volume loops natively above 100%
                    try {
                        val enhancer = LoudnessEnhancer(audioSessionId).apply {
                            val gainDb = when (sound) {
                                "Wind" -> 22.0f       // Max boost for very quiet wind audio
                                "Forest" -> 16.0f     // Significant boost for bird chirps
                                else -> 12.0f         // Generous boost for others (Rain, Ocean, etc.)
                            }
                            setTargetGain((gainDb * 100).toInt()) // converting to millibels
                            setEnabled(true)
                        }
                        loudnessEnhancer = enhancer
                    } catch (_: Exception) {}

                    if (!isPausedState) {
                        start()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun setVolume(multiplier: Float) {
        currentVolume = multiplier
        mediaPlayer?.let {
            try {
                it.setVolume(multiplier, multiplier)
            } catch (_: Exception) {}
        }
    }

    fun setPaused(paused: Boolean) {
        isPausedState = paused
        mediaPlayer?.let {
            try {
                if (paused) {
                    if (it.isPlaying) {
                        it.pause()
                    }
                } else {
                    if (!it.isPlaying) {
                        it.start()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        loudnessEnhancer?.let {
            try {
                it.enabled = false
                it.release()
            } catch (_: Exception) {}
        }
        loudnessEnhancer = null

        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (_: Exception) {}
        }
        mediaPlayer = null
        currentSound = "None"
    }

    fun release() {
        stop()
    }
}
