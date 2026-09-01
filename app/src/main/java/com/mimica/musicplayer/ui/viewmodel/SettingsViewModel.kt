package com.mimica.musicplayer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mimica.musicplayer.data.preferences.SettingsDataStore
import com.mimica.musicplayer.data.preferences.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application.applicationContext)

    val settings: StateFlow<UserSettings> = dataStore.userSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    val themeMode: StateFlow<String> = dataStore.userSettingsFlow
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "SYSTEM"
        )

    // Section 1: Playback
    fun setCrossfadeDuration(duration: Float) {
        viewModelScope.launch {
            dataStore.setCrossfadeDuration(duration)
        }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setGaplessPlayback(enabled)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            dataStore.setPlaybackSpeed(speed)
        }
    }

    fun setVolumeNormalization(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setVolumeNormalization(enabled)
        }
    }

    // Section 2: Audio
    fun setEqualizerPreset(preset: String) {
        viewModelScope.launch {
            dataStore.setEqualizerPreset(preset)
        }
    }

    fun setBassBoostLevel(level: Int) {
        viewModelScope.launch {
            dataStore.setBassBoostLevel(level)
        }
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setVirtualizerEnabled(enabled)
        }
    }

    // Section 3: Appearance
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStore.setThemeMode(mode)
        }
    }

    fun setDynamicTheming(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setDynamicTheming(enabled)
        }
    }

    fun setAccentColorHex(colorHex: Long) {
        viewModelScope.launch {
            dataStore.setAccentColorHex(colorHex)
        }
    }

    // Section 4: Library
    fun setScanOnStartup(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setScanOnStartup(enabled)
        }
    }

    fun addExcludedFolder(folderPath: String) {
        if (folderPath.isNotBlank()) {
            viewModelScope.launch {
                dataStore.addExcludedFolder(folderPath.trim())
            }
        }
    }

    fun removeExcludedFolder(folderPath: String) {
        viewModelScope.launch {
            dataStore.removeExcludedFolder(folderPath)
        }
    }

    // Section 5: Playback Queue
    fun setRememberPosition(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setRememberPosition(enabled)
        }
    }

    fun setAutoPlayNext(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setAutoPlayNext(enabled)
        }
    }

    fun setShuffleOnPlay(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setShuffleOnPlay(enabled)
        }
    }
}
