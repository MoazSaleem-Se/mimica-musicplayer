package com.mimica.musicplayer.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.mimica.musicplayer.data.local.AppDatabase
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.data.preferences.SettingsDataStore
import com.mimica.musicplayer.playback.MusicPlayerService
import com.mimica.musicplayer.utils.ColorExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class PlayerUiState(
    val currentSong: AudioEntity? = null,
    val isPlaying: Boolean = false,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val isFavorite: Boolean = false
)

@OptIn(UnstableApi::class)
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val audioDao = AppDatabase.getDatabase(application).audioDao()
    private val settingsDataStore = SettingsDataStore(application)

    private var currentPlaybackSpeed: Float = 1.0f
    private var currentCrossfadeDuration: Float = 0f

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentSong = MutableStateFlow<AudioEntity?>(null)
    val currentSong: StateFlow<AudioEntity?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()
    val isShuffled: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<AudioEntity>>(emptyList())
    val currentPlaylist: StateFlow<List<AudioEntity>> = _currentPlaylist.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)

    // Palette state for dynamic theming
    private val _albumPalette = MutableStateFlow<Palette?>(null)
    val albumPalette: StateFlow<Palette?> = _albumPalette.asStateFlow()
    private var paletteJob: Job? = null

    // Sleep Timer state
    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()
    private var sleepTimerJob: Job? = null

    val uiState: StateFlow<PlayerUiState> = combine(
        currentSong,
        isPlaying,
        isShuffle,
        isRepeat,
        _isFavorite
    ) { song, playing, shuffle, repeat, favorite ->
        PlayerUiState(song, playing, shuffle, repeat, favorite)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    private var progressJob: Job? = null
    private var pendingPlayRequest: Pair<AudioEntity, List<AudioEntity>>? = null

    // Persistent playback position map
    private val playbackPositions = mutableMapOf<Long, Long>()

    init {
        initializeController()
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsDataStore.userSettingsFlow
                .map { it.playbackSpeed }
                .distinctUntilChanged()
                .collect { speed ->
                    currentPlaybackSpeed = speed
                    mediaController?.setPlaybackParameters(PlaybackParameters(speed))
                }
        }
        viewModelScope.launch {
            settingsDataStore.userSettingsFlow
                .map { it.crossfadeDuration }
                .distinctUntilChanged()
                .collect { duration ->
                    currentCrossfadeDuration = duration
                }
        }
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), MusicPlayerService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
                // Sync current settings with controller
                mediaController?.repeatMode = if (_isRepeat.value) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
                mediaController?.shuffleModeEnabled = _isShuffle.value
                mediaController?.setPlaybackParameters(PlaybackParameters(currentPlaybackSpeed))

                if (pendingPlayRequest != null) {
                    val request = pendingPlayRequest
                    pendingPlayRequest = null
                    request?.let { (song, playlist) ->
                        play(song, playlist)
                    }
                } else {
                    restoreControllerState(mediaController)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun restoreControllerState(controller: MediaController?) {
        val c = controller ?: return
        val currentItem = c.currentMediaItem ?: return
        val songId = currentItem.mediaId.toLongOrNull() ?: return

        _isPlaying.value = c.isPlaying
        val dur = if (c.duration > 0) c.duration else 0L
        if (dur > 0) {
            _duration.value = dur
        }
        _currentPosition.value = c.currentPosition.coerceAtLeast(0L)

        if (c.isPlaying) {
            startProgressTracker()
        }

        viewModelScope.launch(Dispatchers.IO) {
            val count = c.mediaItemCount
            val restoredPlaylist = mutableListOf<AudioEntity>()
            for (i in 0 until count) {
                val item = c.getMediaItemAt(i)
                val itemId = item.mediaId.toLongOrNull()
                if (itemId != null) {
                    val dbSong = audioDao.getAudioById(itemId)
                    if (dbSong != null) {
                        restoredPlaylist.add(dbSong)
                    } else {
                        val meta = item.mediaMetadata
                        restoredPlaylist.add(
                            AudioEntity(
                                id = itemId,
                                title = meta.title?.toString() ?: "Unknown Title",
                                artist = meta.artist?.toString() ?: "Unknown Artist",
                                album = meta.albumTitle?.toString() ?: "Unknown Album",
                                duration = 0L,
                                filePath = item.requestMetadata.mediaUri?.toString() ?: "",
                                albumArtUri = meta.artworkUri?.toString()
                            )
                        )
                    }
                }
            }

            val currentSongEntity = restoredPlaylist.firstOrNull { it.id == songId }
                ?: audioDao.getAudioById(songId)

            withContext(Dispatchers.Main) {
                if (restoredPlaylist.isNotEmpty()) {
                    _currentPlaylist.value = restoredPlaylist
                }
                if (currentSongEntity != null) {
                    _currentSong.value = currentSongEntity
                    if (_duration.value == 0L && currentSongEntity.duration > 0) {
                        _duration.value = currentSongEntity.duration
                    }
                    extractPalette(currentSongEntity.albumArtUri, currentSongEntity.id)
                }
            }
        }
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val songId = mediaItem?.mediaId?.toLongOrNull()
                if (songId != null) {
                    val song = _currentPlaylist.value.firstOrNull { it.id == songId }
                    if (song != null) {
                        _currentSong.value = song
                        _duration.value = song.duration
                        extractPalette(song.albumArtUri, song.id)
                    } else {
                        viewModelScope.launch(Dispatchers.IO) {
                            val dbSong = audioDao.getAudioById(songId)
                            if (dbSong != null) {
                                withContext(Dispatchers.Main) {
                                    _currentSong.value = dbSong
                                    _duration.value = dbSong.duration
                                    extractPalette(dbSong.albumArtUri, dbSong.id)
                                }
                            }
                        }
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                    saveCurrentPosition()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                        _playbackError.value = null
                    }
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                        stopProgressTracker()
                        saveCurrentPosition()
                    }
                    else -> {}
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _isPlaying.value = false
                stopProgressTracker()
                _playbackError.value = "Playback error: ${error.localizedMessage ?: "Unknown audio error"}"
            }
        })
    }

    private fun extractPalette(albumArtUri: String?, songId: Long) {
        paletteJob?.cancel()
        if (albumArtUri.isNullOrEmpty()) {
            _albumPalette.value = null
            return
        }

        paletteJob = viewModelScope.launch(Dispatchers.IO) {
            val palette = ColorExtractor.extractPaletteFromUri(getApplication(), albumArtUri)
            withContext(Dispatchers.Main) {
                if (_currentSong.value?.id == songId) {
                    _albumPalette.value = palette
                }
            }
        }
    }

    fun play(song: AudioEntity, playlist: List<AudioEntity> = emptyList()) {
        // 1. Validate file path format
        if (song.filePath.isBlank()) {
            _playbackError.value = "Cannot play track: File path is empty or song is unavailable."
            _isPlaying.value = false
            return
        }

        // 2. Validate physical file existence if absolute file path
        if (song.filePath.startsWith("/")) {
            val file = File(song.filePath)
            if (!file.exists()) {
                _playbackError.value = "Cannot play track: File no longer exists on device storage."
                _isPlaying.value = false
                return
            }
        }

        _currentSong.value = song
        val effectivePlaylist = if (playlist.isNotEmpty()) {
            playlist
        } else if (_currentPlaylist.value.any { it.id == song.id }) {
            _currentPlaylist.value
        } else {
            listOf(song)
        }
        _currentPlaylist.value = effectivePlaylist

        extractPalette(song.displayArtworkUri, song.id)

        val controller = mediaController
        if (controller == null) {
            pendingPlayRequest = Pair(song, effectivePlaylist)
            if (controllerFuture == null) {
                initializeController()
            }
            return
        }

        // Build MediaItem list for entire playlist so system notification & lock screen have full queue
        val mediaItems = effectivePlaylist.map { item ->
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(item.title)
                .setArtist(item.displayArtist)
                .setAlbumTitle(item.album)
                .setArtworkUri(item.displayArtworkUri?.let { Uri.parse(it) })
                .build()

            MediaItem.Builder()
                .setMediaId(item.id.toString())
                .setUri(Uri.parse(item.filePath))
                .setMediaMetadata(mediaMetadata)
                .build()
        }

        val startIndex = effectivePlaylist.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        val rememberedPosition = playbackPositions[song.id] ?: 0L

        controller.setMediaItems(mediaItems, startIndex, rememberedPosition)
        controller.repeatMode = if (_isRepeat.value) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        controller.shuffleModeEnabled = _isShuffle.value
        controller.setPlaybackParameters(PlaybackParameters(currentPlaybackSpeed))
        controller.prepare()
        controller.play()

        _isPlaying.value = true
        _duration.value = song.duration
        _playbackError.value = null
        startProgressTracker()

        viewModelScope.launch(Dispatchers.IO) {
            Log.d("StatsDebug", "incrementStats called for songId: ${song.id}")
            audioDao.incrementStats(song.id, System.currentTimeMillis(), song.duration)
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerMinutes.value = null
            return
        }
        _sleepTimerMinutes.value = minutes
        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes
            while (remaining > 0 && isActive) {
                delay(60 * 1000L)
                remaining -= 1
                _sleepTimerMinutes.value = if (remaining > 0) remaining else null
            }
            pause()
            _sleepTimerMinutes.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerMinutes.value = null
    }

    fun updatePalette(bitmap: Bitmap) {
        viewModelScope.launch {
            val palette = ColorExtractor.extractColors(bitmap)
            _albumPalette.value = palette
        }
    }

    fun pause() {
        mediaController?.pause()
        _isPlaying.value = false
        stopProgressTracker()
        saveCurrentPosition()
    }

    fun resume() {
        mediaController?.play()
        _isPlaying.value = true
        startProgressTracker()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _currentPosition.value = positionMs
        saveCurrentPosition()
    }

    fun skipNext() {
        skipToNext()
    }

    fun skipToNext() {
        val controller = mediaController
        if (controller != null && controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else {
            val playlist = _currentPlaylist.value
            if (playlist.isEmpty()) return

            val currentIndex = playlist.indexOfFirst { it.id == _currentSong.value?.id }
            if (currentIndex != -1) {
                val nextIndex = if (_isShuffle.value) {
                    playlist.indices.random()
                } else {
                    (currentIndex + 1) % playlist.size
                }
                play(playlist[nextIndex], playlist)
            }
        }
    }

    fun skipPrevious() {
        skipToPrevious()
    }

    fun skipToPrevious() {
        val controller = mediaController
        if (controller != null) {
            if (controller.currentPosition > 3000L) {
                controller.seekTo(0L)
                _currentPosition.value = 0L
                return
            }
            if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
                return
            }
        }

        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        val currentIndex = playlist.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex != -1) {
            val prevIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
            play(playlist[prevIndex], playlist)
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffle.value
        _isShuffle.value = newShuffle
        mediaController?.shuffleModeEnabled = newShuffle
    }

    fun toggleRepeat() {
        val newRepeat = !_isRepeat.value
        _isRepeat.value = newRepeat
        mediaController?.repeatMode = if (newRepeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    fun toggleFavorite() {
        _isFavorite.value = !_isFavorite.value
    }

    private fun saveCurrentPosition() {
        _currentSong.value?.let { song ->
            val pos = mediaController?.currentPosition ?: _currentPosition.value
            playbackPositions[song.id] = pos
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                mediaController?.let { controller ->
                    val pos = controller.currentPosition.coerceAtLeast(0L)
                    val dur = if (controller.duration > 0) controller.duration else _duration.value
                    _currentPosition.value = pos
                    if (controller.duration > 0) {
                        _duration.value = controller.duration
                    }

                    // Crossfade volume ramping effect
                    if (currentCrossfadeDuration > 0f && dur > 0) {
                        val fadeWindowMs = (currentCrossfadeDuration * 1000).toLong().coerceAtLeast(1000L)
                        val remainingMs = dur - pos
                        val targetVol = when {
                            pos < fadeWindowMs -> (pos.toFloat() / fadeWindowMs.toFloat()).coerceIn(0.05f, 1f)
                            remainingMs < fadeWindowMs -> (remainingMs.toFloat() / fadeWindowMs.toFloat()).coerceIn(0.05f, 1f)
                            else -> 1.0f
                        }
                        controller.volume = targetVol
                    } else {
                        if (controller.volume != 1.0f) {
                            controller.volume = 1.0f
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun updateSongCustomMetadata(songId: Long, customArtist: String?, customArtworkUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            audioDao.updateCustomMetadata(songId, customArtist, customArtworkUri)
            val updated = audioDao.getAudioById(songId)
            withContext(Dispatchers.Main) {
                if (_currentSong.value?.id == songId && updated != null) {
                    _currentSong.value = updated
                    extractPalette(updated.displayArtworkUri, updated.id)
                }
                _currentPlaylist.value = _currentPlaylist.value.map {
                    if (it.id == songId && updated != null) updated else it
                }
            }
        }
    }

    override fun onCleared() {
        stopProgressTracker()
        paletteJob?.cancel()
        sleepTimerJob?.cancel()
        saveCurrentPosition()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onCleared()
    }
}
