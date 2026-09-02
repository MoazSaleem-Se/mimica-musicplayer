package com.mimica.musicplayer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mimica.musicplayer.data.local.AppDatabase
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.data.local.PlaylistEntity
import com.mimica.musicplayer.data.local.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val playlistDao = db.playlistDao()
    private val playlistSongDao = db.playlistSongDao()

    val playlists: StateFlow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createPlaylist(name: String, onCreated: (Long) -> Unit = {}) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = playlistDao.insertPlaylist(
                PlaylistEntity(name = name.trim(), songCount = 0)
            )
            onCreated(id)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistSongDao.clearPlaylistSongs(playlistId)
            playlistDao.deletePlaylistById(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val count = playlistSongDao.getSongCount(playlistId)
            playlistSongDao.insertSongToPlaylist(
                PlaylistSongEntity(playlistId = playlistId, songId = songId, position = count)
            )
            val updatedCount = playlistSongDao.getSongCount(playlistId)
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist != null) {
                playlistDao.updatePlaylist(playlist.copy(songCount = updatedCount))
            }
            onSuccess()
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistSongDao.removeSongFromPlaylist(playlistId, songId)
            val updatedCount = playlistSongDao.getSongCount(playlistId)
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist != null) {
                playlistDao.updatePlaylist(playlist.copy(songCount = updatedCount))
            }
        }
    }

    fun updatePlaylistArtwork(playlistId: Long, customArtworkUri: String?) {
        viewModelScope.launch {
            playlistDao.updateCustomArtwork(playlistId, customArtworkUri)
        }
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<AudioEntity>> {
        return playlistSongDao.getSongsForPlaylist(playlistId)
    }
}
