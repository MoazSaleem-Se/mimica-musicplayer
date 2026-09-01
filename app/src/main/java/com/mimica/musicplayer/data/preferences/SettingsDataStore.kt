package com.mimica.musicplayer.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserSettings(
    // Section 1: Playback
    val crossfadeDuration: Float = 0f,
    val gaplessPlayback: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val volumeNormalization: Boolean = false,

    // Section 2: Audio
    val equalizerPreset: String = "Normal",
    val bassBoostLevel: Int = 0,
    val virtualizerEnabled: Boolean = false,

    // Section 3: Appearance
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val dynamicTheming: Boolean = true,
    val accentColorHex: Long = 0xFF6750A4,

    // Section 4: Library
    val scanOnStartup: Boolean = true,
    val excludedFolders: Set<String> = emptySet(),

    // Section 5: Playback Queue
    val rememberPosition: Boolean = true,
    val autoPlayNext: Boolean = true,
    val shuffleOnPlay: Boolean = false,

    // Section 6: Notifications & Headset
    val showAlbumArtInNotification: Boolean = true,
    val showControlsInNotification: Boolean = true,
    val showProgressInNotification: Boolean = true,
    val notificationPriority: String = "DEFAULT", // "DEFAULT", "HIGH", "LOW"
    val showOnLockScreen: Boolean = true,
    val showControlsOnLockScreen: Boolean = true,
    val autoPlayOnHeadsetConnect: Boolean = false,
    val autoPauseOnHeadsetDisconnect: Boolean = true
)

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_CROSSFADE = floatPreferencesKey("crossfade_duration")
        val KEY_GAPLESS = booleanPreferencesKey("gapless_playback")
        val KEY_PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val KEY_VOLUME_NORMALIZATION = booleanPreferencesKey("volume_normalization")

        val KEY_EQUALIZER_PRESET = stringPreferencesKey("equalizer_preset")
        val KEY_BASS_BOOST = intPreferencesKey("bass_boost_level")
        val KEY_VIRTUALIZER = booleanPreferencesKey("virtualizer_enabled")

        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_THEMING = booleanPreferencesKey("dynamic_theming")
        val KEY_ACCENT_COLOR = longPreferencesKey("accent_color_hex")

        val KEY_SCAN_ON_STARTUP = booleanPreferencesKey("scan_on_startup")
        val KEY_EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")

        val KEY_REMEMBER_POSITION = booleanPreferencesKey("remember_position")
        val KEY_AUTO_PLAY_NEXT = booleanPreferencesKey("auto_play_next")
        val KEY_SHUFFLE_ON_PLAY = booleanPreferencesKey("shuffle_on_play")

        // Notification & Headset Keys
        val KEY_NOTIF_ALBUM_ART = booleanPreferencesKey("notif_album_art")
        val KEY_NOTIF_CONTROLS = booleanPreferencesKey("notif_controls")
        val KEY_NOTIF_PROGRESS = booleanPreferencesKey("notif_progress")
        val KEY_NOTIF_PRIORITY = stringPreferencesKey("notif_priority")
        val KEY_NOTIF_LOCKSCREEN = booleanPreferencesKey("notif_lockscreen")
        val KEY_NOTIF_LOCKSCREEN_CONTROLS = booleanPreferencesKey("notif_lockscreen_controls")
        val KEY_HEADSET_AUTOPLAY = booleanPreferencesKey("headset_autoplay")
        val KEY_HEADSET_AUTOPAUSE = booleanPreferencesKey("headset_autopause")
    }

    val userSettingsFlow: Flow<UserSettings> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                crossfadeDuration = preferences[KEY_CROSSFADE] ?: 0f,
                gaplessPlayback = preferences[KEY_GAPLESS] ?: true,
                playbackSpeed = preferences[KEY_PLAYBACK_SPEED] ?: 1.0f,
                volumeNormalization = preferences[KEY_VOLUME_NORMALIZATION] ?: false,
                equalizerPreset = preferences[KEY_EQUALIZER_PRESET] ?: "Normal",
                bassBoostLevel = preferences[KEY_BASS_BOOST] ?: 0,
                virtualizerEnabled = preferences[KEY_VIRTUALIZER] ?: false,
                themeMode = preferences[KEY_THEME_MODE] ?: "SYSTEM",
                dynamicTheming = preferences[KEY_DYNAMIC_THEMING] ?: true,
                accentColorHex = preferences[KEY_ACCENT_COLOR] ?: 0xFF6750A4,
                scanOnStartup = preferences[KEY_SCAN_ON_STARTUP] ?: true,
                excludedFolders = preferences[KEY_EXCLUDED_FOLDERS] ?: emptySet(),
                rememberPosition = preferences[KEY_REMEMBER_POSITION] ?: true,
                autoPlayNext = preferences[KEY_AUTO_PLAY_NEXT] ?: true,
                shuffleOnPlay = preferences[KEY_SHUFFLE_ON_PLAY] ?: false,
                showAlbumArtInNotification = preferences[KEY_NOTIF_ALBUM_ART] ?: true,
                showControlsInNotification = preferences[KEY_NOTIF_CONTROLS] ?: true,
                showProgressInNotification = preferences[KEY_NOTIF_PROGRESS] ?: true,
                notificationPriority = preferences[KEY_NOTIF_PRIORITY] ?: "DEFAULT",
                showOnLockScreen = preferences[KEY_NOTIF_LOCKSCREEN] ?: true,
                showControlsOnLockScreen = preferences[KEY_NOTIF_LOCKSCREEN_CONTROLS] ?: true,
                autoPlayOnHeadsetConnect = preferences[KEY_HEADSET_AUTOPLAY] ?: false,
                autoPauseOnHeadsetDisconnect = preferences[KEY_HEADSET_AUTOPAUSE] ?: true
            )
        }

    suspend fun setCrossfadeDuration(duration: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_CROSSFADE] = duration
        }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_GAPLESS] = enabled
        }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_PLAYBACK_SPEED] = speed
        }
    }

    suspend fun setVolumeNormalization(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_VOLUME_NORMALIZATION] = enabled
        }
    }

    suspend fun setEqualizerPreset(preset: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_EQUALIZER_PRESET] = preset
        }
    }

    suspend fun setBassBoostLevel(level: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_BASS_BOOST] = level
        }
    }

    suspend fun setVirtualizerEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_VIRTUALIZER] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setDynamicTheming(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_THEMING] = enabled
        }
    }

    suspend fun setAccentColorHex(colorHex: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_ACCENT_COLOR] = colorHex
        }
    }

    suspend fun setScanOnStartup(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_SCAN_ON_STARTUP] = enabled
        }
    }

    suspend fun addExcludedFolder(folderPath: String) {
        context.settingsDataStore.edit { preferences ->
            val current = preferences[KEY_EXCLUDED_FOLDERS] ?: emptySet()
            preferences[KEY_EXCLUDED_FOLDERS] = current + folderPath
        }
    }

    suspend fun removeExcludedFolder(folderPath: String) {
        context.settingsDataStore.edit { preferences ->
            val current = preferences[KEY_EXCLUDED_FOLDERS] ?: emptySet()
            preferences[KEY_EXCLUDED_FOLDERS] = current - folderPath
        }
    }

    suspend fun setRememberPosition(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_REMEMBER_POSITION] = enabled
        }
    }

    suspend fun setAutoPlayNext(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_AUTO_PLAY_NEXT] = enabled
        }
    }

    suspend fun setShuffleOnPlay(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_SHUFFLE_ON_PLAY] = enabled
        }
    }

    // Notification Setters
    suspend fun setShowAlbumArtInNotification(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIF_ALBUM_ART] = enabled
        }
    }

    suspend fun setShowControlsInNotification(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIF_CONTROLS] = enabled
        }
    }

    suspend fun setShowProgressInNotification(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIF_PROGRESS] = enabled
        }
    }

    suspend fun setNotificationPriority(priority: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIF_PRIORITY] = priority
        }
    }

    suspend fun setShowOnLockScreen(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIF_LOCKSCREEN] = enabled
        }
    }

    suspend fun setShowControlsOnLockScreen(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIF_LOCKSCREEN_CONTROLS] = enabled
        }
    }

    suspend fun setAutoPlayOnHeadsetConnect(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_HEADSET_AUTOPLAY] = enabled
        }
    }

    suspend fun setAutoPauseOnHeadsetDisconnect(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_HEADSET_AUTOPAUSE] = enabled
        }
    }
}
