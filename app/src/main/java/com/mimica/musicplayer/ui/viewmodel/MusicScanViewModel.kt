package com.mimica.musicplayer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mimica.musicplayer.data.local.AppDatabase
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.data.preferences.SettingsDataStore
import com.mimica.musicplayer.data.repository.MusicRepository
import com.mimica.musicplayer.data.scanner.MediaScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Loading : ScanUiState
    data class Success(val songs: List<AudioEntity>) : ScanUiState
    data object Empty : ScanUiState
    data object PermissionDenied : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class MusicScanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MusicRepository

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private var scanJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        val scanner = MediaScanner(application)
        val dataStore = SettingsDataStore(application)
        repository = MusicRepository(database.audioDao(), scanner, dataStore)

        // Reactively observe cached audio filtered by current excluded folders
        viewModelScope.launch {
            repository.cachedAudioFlow.distinctUntilChanged().collect { songs ->
                if (songs.isNotEmpty()) {
                    _scanState.value = ScanUiState.Success(songs)
                } else if (_scanState.value !is ScanUiState.Loading && _scanState.value !is ScanUiState.PermissionDenied) {
                    _scanState.value = ScanUiState.Empty
                }
            }
        }
    }

    fun onPermissionGranted() {
        scanMusic()
    }

    fun onPermissionDenied() {
        _scanState.value = ScanUiState.PermissionDenied
    }

    fun scanMusic() {
        if (scanJob?.isActive == true) return
        scanJob = viewModelScope.launch {
            _scanState.value = ScanUiState.Loading
            try {
                delay(200)
                val songs = repository.scanAndCacheMusic()
                if (songs.isEmpty()) {
                    _scanState.value = ScanUiState.Empty
                } else {
                    _scanState.value = ScanUiState.Success(songs)
                }
            } catch (e: SecurityException) {
                _scanState.value = ScanUiState.PermissionDenied
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error(
                    e.localizedMessage ?: "Failed to scan audio files. Please try again."
                )
            }
        }
    }
}
