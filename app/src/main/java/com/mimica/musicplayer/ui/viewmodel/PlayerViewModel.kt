package com.mimica.musicplayer.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.mimica.musicplayer.data.local.AudioEntity
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlayerUiState(
    val currentSong: AudioEntity? = null,
    val isPlaying: Boolean = false,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val isFavorite: Boolean = false
)

@OptIn(UnstableApi::class)
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

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

    // Map remembering last playback position per song ID
    private val playbackPositions = mutableMapOf<Long, Long>()

    private var progressJob: Job? = null

    init {
        initializeController()
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
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
                        skipToNext()
                    }
                    else -> {}
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _playbackError.value = "Playback error: ${error.localizedMessage ?: "Unknown error"}"
                _isPlaying.value = false
                stopProgressTracker()
            }
        })
    }

    fun play(song: AudioEntity, playlist: List<AudioEntity> = emptyList()) {
        _currentSong.value = song
        if (playlist.isNotEmpty()) {
            _currentPlaylist.value = playlist
        } else if (_currentPlaylist.value.none { it.id == song.id }) {
            _currentPlaylist.value = listOf(song)
        }

        // Auto-extract Palette colors from album art in background
        viewModelScope.launch {
            val palette = ColorExtractor.extractPaletteFromUri(getApplication(), song.albumArtUri)
            _albumPalette.value = palette
        }

        val controller = mediaController
        if (controller == null) {
            initializeController()
        }

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(Uri.parse(song.filePath))
            .setMediaMetadata(mediaMetadata)
            .build()

        controller?.let {
            it.setMediaItem(mediaItem)
            it.prepare()

            val rememberedPosition = playbackPositions[song.id] ?: 0L
            if (rememberedPosition > 0L) {
                it.seekTo(rememberedPosition)
                _currentPosition.value = rememberedPosition
            } else {
                _currentPosition.value = 0L
            }

            it.play()
            _isPlaying.value = true
            _duration.value = song.duration
            _playbackError.value = null
            startProgressTracker()
        }
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

    fun skipPrevious() {
        skipToPrevious()
    }

    fun skipToPrevious() {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        if (_currentPosition.value > 3000L) {
            seekTo(0L)
            return
        }

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
        mediaController?.repeatMode = if (newRepeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
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
                mediaController?.let {
                    _currentPosition.value = it.currentPosition.coerceAtLeast(0L)
                    if (it.duration > 0) {
                        _duration.value = it.duration
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

    override fun onCleared() {
        stopProgressTracker()
        saveCurrentPosition()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onCleared()
    }
}
