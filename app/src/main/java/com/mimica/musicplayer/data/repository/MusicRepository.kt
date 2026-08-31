package com.mimica.musicplayer.data.repository

import com.mimica.musicplayer.data.local.AudioDao
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.data.scanner.MediaScanner
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val audioDao: AudioDao,
    private val mediaScanner: MediaScanner
) {
    val cachedAudioFlow: Flow<List<AudioEntity>> = audioDao.getAllAudio()

    suspend fun getCachedAudio(): List<AudioEntity> {
        return audioDao.getAllAudioList()
    }

    suspend fun scanAndCacheMusic(): List<AudioEntity> {
        val scanned = mediaScanner.scanLocalMusic()
        if (scanned.isNotEmpty()) {
            audioDao.clearAll()
            audioDao.insertAll(scanned)
        }
        return scanned
    }
}
