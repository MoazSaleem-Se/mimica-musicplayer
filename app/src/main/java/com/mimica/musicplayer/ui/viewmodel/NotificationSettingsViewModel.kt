package com.mimica.musicplayer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mimica.musicplayer.data.preferences.SettingsDataStore
import com.mimica.musicplayer.data.preferences.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application.applicationContext)

    val settings: StateFlow<UserSettings> = dataStore.userSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    fun setShowAlbumArtInNotification(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setShowAlbumArtInNotification(enabled)
        }
    }

    fun setShowControlsInNotification(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setShowControlsInNotification(enabled)
        }
    }

    fun setShowProgressInNotification(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setShowProgressInNotification(enabled)
        }
    }

    fun setNotificationPriority(priority: String) {
        viewModelScope.launch {
            dataStore.setNotificationPriority(priority)
        }
    }

    fun setShowOnLockScreen(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setShowOnLockScreen(enabled)
        }
    }

    fun setShowControlsOnLockScreen(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setShowControlsOnLockScreen(enabled)
        }
    }

    fun setAutoPlayOnHeadsetConnect(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setAutoPlayOnHeadsetConnect(enabled)
        }
    }

    fun setAutoPauseOnHeadsetDisconnect(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setAutoPauseOnHeadsetDisconnect(enabled)
        }
    }
}
