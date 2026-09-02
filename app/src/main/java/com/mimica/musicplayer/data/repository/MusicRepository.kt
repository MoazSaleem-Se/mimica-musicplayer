package com.mimica.musicplayer.data.repository

import com.mimica.musicplayer.data.local.AudioDao
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.data.preferences.SettingsDataStore
import com.mimica.musicplayer.data.scanner.MediaScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class MusicRepository(
    private val audioDao: AudioDao,
    private val mediaScanner: MediaScanner,
    private val settingsDataStore: SettingsDataStore
) {
    val cachedAudioFlow: Flow<List<AudioEntity>> = combine(
        audioDao.getAllAudio(),
        settingsDataStore.userSettingsFlow
    ) { songs, settings ->
        if (settings.excludedFolders.isEmpty()) {
            songs
        } else {
            songs.filter { !MediaScanner.isPathExcluded(it.filePath, settings.excludedFolders) }
        }
    }

    suspend fun getCachedAudio(): List<AudioEntity> {
        val allAudio = audioDao.getAllAudioList()
        val settings = settingsDataStore.userSettingsFlow.first()
        return if (settings.excludedFolders.isEmpty()) {
            allAudio
        } else {
            allAudio.filter { !MediaScanner.isPathExcluded(it.filePath, settings.excludedFolders) }
        }
    }

    suspend fun scanAndCacheMusic(): List<AudioEntity> {
        val settings = settingsDataStore.userSettingsFlow.first()
        val scanned = mediaScanner.scanLocalMusic(settings.excludedFolders)
        if (scanned.isNotEmpty()) {
            audioDao.clearAll()
            audioDao.insertAll(scanned)
        }
        return scanned
    }
}
