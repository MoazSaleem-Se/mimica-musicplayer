package com.mimica.musicplayer.playback

import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mimica.musicplayer.MainActivity
import com.mimica.musicplayer.data.preferences.SettingsDataStore
import com.mimica.musicplayer.data.preferences.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class MusicPlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var settingsDataStore: SettingsDataStore
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Audio Effects
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentAudioSessionId: Int = C.AUDIO_SESSION_ID_UNSET
    private var currentSettings: UserSettings = UserSettings()

    override fun onCreate() {
        super.onCreate()
        settingsDataStore = SettingsDataStore(this)

        // Configure audio attributes for music playback with automatic audio focus handling
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val renderersFactory = DefaultRenderersFactory(this)

        player = ExoPlayer.Builder(this, renderersFactory)
            .setAudioAttributes(audioAttributes, true /* handleAudioFocus */)
            .setHandleAudioBecomingNoisy(true) // Pauses automatically on headset/Bluetooth disconnection
            .build()

        // Listen for Audio Session ID changes to bind Equalizer, BassBoost, Virtualizer, LoudnessEnhancer
        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId > 0) {
                    initAudioEffects(audioSessionId)
                }
            }
        })

        // PendingIntent to launch MainActivity when clicking the media notification
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(MediaSessionCallback())
            .build()

        // Reactively observe playback speed and apply to ExoPlayer
        serviceScope.launch {
            settingsDataStore.userSettingsFlow
                .map { it.playbackSpeed }
                .distinctUntilChanged()
                .collect { speed ->
                    val safeSpeed = speed.coerceIn(0.25f, 3.0f)
                    player.playbackParameters = PlaybackParameters(safeSpeed)
                }
        }

        // Reactively observe Audio Effects & Volume Normalization settings
        serviceScope.launch {
            settingsDataStore.userSettingsFlow.collect { settings ->
                currentSettings = settings
                applyCurrentSettings(settings)
            }
        }

        // Initial attempt to bind effects if audioSessionId is already known
        if (player.audioSessionId != C.AUDIO_SESSION_ID_UNSET && player.audioSessionId > 0) {
            initAudioEffects(player.audioSessionId)
        }
    }

    private fun initAudioEffects(sessionId: Int) {
        if (sessionId == currentAudioSessionId && equalizer != null) return

        releaseAudioEffects()
        currentAudioSessionId = sessionId

        try {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.e("MusicPlayerService", "Could not initialize Equalizer on session $sessionId", e)
        }

        try {
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.e("MusicPlayerService", "Could not initialize BassBoost on session $sessionId", e)
        }

        try {
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.e("MusicPlayerService", "Could not initialize Virtualizer on session $sessionId", e)
        }

        try {
            loudnessEnhancer = LoudnessEnhancer(sessionId)
        } catch (e: Exception) {
            Log.e("MusicPlayerService", "Could not initialize LoudnessEnhancer on session $sessionId", e)
        }

        applyCurrentSettings(currentSettings)
    }

    private fun applyCurrentSettings(settings: UserSettings) {
        // 1. Equalizer Preset
        equalizer?.let { eq ->
            try {
                val numPresets = eq.numberOfPresets
                var presetIndex: Short = -1
                for (i in 0 until numPresets) {
                    if (eq.getPresetName(i.toShort()).equals(settings.equalizerPreset, ignoreCase = true)) {
                        presetIndex = i.toShort()
                        break
                    }
                }
                if (presetIndex >= 0) {
                    eq.usePreset(presetIndex)
                } else {
                    applyFallbackPreset(eq, settings.equalizerPreset)
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerService", "Error applying equalizer preset", e)
            }
        }

        // 2. Bass Boost (0 - 1000 strength)
        bassBoost?.let { bb ->
            try {
                if (bb.strengthSupported) {
                    val strength = (settings.bassBoostLevel.coerceIn(0, 100) * 10).toShort()
                    bb.setStrength(strength)
                    bb.enabled = settings.bassBoostLevel > 0
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerService", "Error applying bass boost", e)
            }
        }

        // 3. Virtualizer (Surround effect)
        virtualizer?.let { virt ->
            try {
                if (virt.strengthSupported) {
                    virt.enabled = settings.virtualizerEnabled
                    virt.setStrength(if (settings.virtualizerEnabled) 1000.toShort() else 0.toShort())
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerService", "Error applying virtualizer", e)
            }
        }

        // 4. Loudness Enhancer (Volume Normalization)
        loudnessEnhancer?.let { le ->
            try {
                le.enabled = settings.volumeNormalization
                if (settings.volumeNormalization) {
                    le.setTargetGain(650) // 650 mB gain for smooth normalization
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerService", "Error applying loudness enhancer", e)
            }
        }
    }

    private fun applyFallbackPreset(eq: Equalizer, presetName: String) {
        val numBands = eq.numberOfBands
        if (numBands == 0.toShort()) return
        val bandLevels = when (presetName.lowercase()) {
            "bass boost" -> listOf(600, 400, 100, 0, 0)
            "vocal" -> listOf(-200, 100, 500, 300, -100)
            "rock" -> listOf(400, 200, -100, 200, 500)
            "pop" -> listOf(-100, 200, 500, 200, -100)
            "dance" -> listOf(500, 300, 0, 200, 400)
            "classical" -> listOf(400, 200, -200, 200, 300)
            "heavy metal" -> listOf(300, 0, 0, 400, 500)
            "hip hop" -> listOf(500, 300, 0, 100, 300)
            "jazz" -> listOf(300, 100, -100, 100, 300)
            "electronic" -> listOf(400, 200, 0, 200, 400)
            else -> listOf(0, 0, 0, 0, 0) // Normal / Flat
        }
        for (band in 0 until numBands) {
            val level = bandLevels.getOrNull(band) ?: 0
            val min = eq.bandLevelRange[0]
            val max = eq.bandLevelRange[1]
            val clamped = level.toShort().coerceIn(min, max)
            eq.setBandLevel(band.toShort(), clamped)
        }
    }

    private fun releaseAudioEffects() {
        try { equalizer?.release() } catch (e: Exception) {}
        try { bassBoost?.release() } catch (e: Exception) {}
        try { virtualizer?.release() } catch (e: Exception) {}
        try { loudnessEnhancer?.release() } catch (e: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // If player is not playing or has no active playback, stop the foreground service
        if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        releaseAudioEffects()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(),
                connectionResult.availablePlayerCommands
            )
        }
    }
}
