package com.mimica.musicplayer.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import kotlin.math.abs
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.ui.viewmodel.PlayerViewModel
import com.mimica.musicplayer.ui.viewmodel.PlaylistViewModel
import com.mimica.musicplayer.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingBottomSheet(
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val duration by playerViewModel.duration.collectAsState()
    val isShuffle by playerViewModel.isShuffle.collectAsState()
    val isRepeat by playerViewModel.isRepeat.collectAsState()
    val currentPlaylist by playerViewModel.currentPlaylist.collectAsState()
    val playbackError by playerViewModel.playbackError.collectAsState()
    val uiState by playerViewModel.uiState.collectAsState()
    val albumPalette by playerViewModel.albumPalette.collectAsState()
    val sleepTimerMinutes by playerViewModel.sleepTimerMinutes.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(currentSong) {
        if (currentSong == null) {
            isExpanded = false
        }
    }

    BackHandler(enabled = isExpanded) {
        coroutineScope.launch {
            sheetState.hide()
            isExpanded = false
        }
    }

    // 1. Persistent MiniPlayer: ALWAYS visible whenever a song is loaded or playing
    AnimatedVisibility(
        visible = currentSong != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        currentSong?.let { song ->
            MiniPlayerContent(
                song = song,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                palette = albumPalette,
                onPlayPauseClick = { playerViewModel.togglePlayPause() },
                onNextClick = { playerViewModel.skipToNext() },
                onPreviousClick = { playerViewModel.skipToPrevious() },
                onClick = { isExpanded = true },
                onImageLoaded = { bitmap -> playerViewModel.updatePalette(bitmap) }
            )
        }
    }

    // 2. Full Player ModalBottomSheet with Album Art & Dynamic Palette Theming
    if (isExpanded && currentSong != null) {
        ModalBottomSheet(
            onDismissRequest = { isExpanded = false },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
        ) {
            FullPlayerContent(
                song = currentSong!!,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                isShuffle = isShuffle,
                isRepeat = isRepeat,
                isFavorite = uiState.isFavorite,
                currentPlaylist = currentPlaylist,
                playbackError = playbackError,
                palette = albumPalette,
                activeSleepTimerMinutes = sleepTimerMinutes,
                onPlayPauseClick = { playerViewModel.togglePlayPause() },
                onNextClick = { playerViewModel.skipToNext() },
                onPreviousClick = { playerViewModel.skipToPrevious() },
                onShuffleClick = { playerViewModel.toggleShuffle() },
                onRepeatClick = { playerViewModel.toggleRepeat() },
                onFavoriteClick = { playerViewModel.toggleFavorite() },
                onSeek = { targetMs -> playerViewModel.seekTo(targetMs) },
                onSetSleepTimer = { minutes -> playerViewModel.setSleepTimer(minutes) },
                onPlayTrack = { track -> playerViewModel.play(track, currentPlaylist) },
                onCollapse = {
                    coroutineScope.launch {
                        sheetState.hide()
                        isExpanded = false
                    }
                },
                onImageLoaded = { bitmap -> playerViewModel.updatePalette(bitmap) }
            )
        }
    }
}

@Composable
fun MiniPlayerContent(
    song: AudioEntity,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    palette: Palette?,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit = {},
    onClick: () -> Unit,
    onImageLoaded: (android.graphics.Bitmap) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 100.dp.toPx() }
    val offsetX = remember { Animatable(0f) }

    val defaultPrimary = MaterialTheme.colorScheme.primary
    val vibrantColor = remember(palette, defaultPrimary) {
        palette?.let { Color(it.getVibrantColor(defaultPrimary.toArgb())) } ?: defaultPrimary
    }
    val animatedVibrant by animateColorAsState(vibrantColor, tween(400), label = "MiniVibrant")

    val totalDuration = if (duration > 0) duration else song.duration.coerceAtLeast(1L)
    val progress = (currentPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .pointerInput(song.id) {
                Log.d("SwipeDebug", "PointerInput attached")
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalDragX = 0f
                    var isDragging = false
                    val touchSlop = viewConfiguration.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) {
                            if (!isDragging) {
                                // Detected tap -> Expand player
                                Log.d("SwipeDebug", "Tap detected")
                                onClick()
                            } else {
                                // Drag finished -> Evaluate swipe threshold
                                coroutineScope.launch {
                                    if (offsetX.value < -thresholdPx) {
                                        // Swiped Left -> Skip Next
                                        Log.d("SwipeDebug", "Threshold exceeded! Left")
                                        offsetX.animateTo(-250f, tween(150))
                                        onNextClick()
                                        offsetX.snapTo(250f)
                                        offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                    } else if (offsetX.value > thresholdPx) {
                                        // Swiped Right -> Skip Previous
                                        Log.d("SwipeDebug", "Threshold exceeded! Right")
                                        offsetX.animateTo(250f, tween(150))
                                        onPreviousClick()
                                        offsetX.snapTo(-250f)
                                        offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                    } else {
                                        // Snap back if threshold not met
                                        offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                    }
                                }
                            }
                            break
                        }

                        val dragAmount = change.position.x - change.previousPosition.x
                        totalDragX += dragAmount

                        if (!isDragging && abs(totalDragX) > touchSlop) {
                            isDragging = true
                        }

                        if (isDragging) {
                            Log.d("SwipeDebug", "Drag detected: $dragAmount")
                            change.consume()
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        }
                    }
                }
            },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!song.albumArtUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(song.albumArtUri)
                                .crossfade(500)
                                .allowHardware(false)
                                .build(),
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                            onSuccess = { success ->
                                val bitmap = (success.result.drawable as? BitmapDrawable)?.bitmap
                                if (bitmap != null) {
                                    onImageLoaded(bitmap)
                                }
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = animatedVibrant
                    )
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = {
                            (fadeIn(tween(200)) + scaleIn(tween(200)))
                                .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200)))
                        },
                        label = "MiniPlayPauseMorph"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = animatedVibrant,
                trackColor = Color.Transparent,
            )
        }
    }
}

@Composable
fun FullPlayerContent(
    song: AudioEntity,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isShuffle: Boolean,
    isRepeat: Boolean,
    isFavorite: Boolean,
    currentPlaylist: List<AudioEntity> = emptyList(),
    playbackError: String? = null,
    palette: Palette?,
    activeSleepTimerMinutes: Int? = null,
    playlistViewModel: PlaylistViewModel = viewModel(),
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onPlayTrack: (AudioEntity) -> Unit,
    onCollapse: () -> Unit,
    onImageLoaded: (android.graphics.Bitmap) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val collapseThresholdPx = with(density) { 150.dp.toPx() }
    val dragOffsetY = remember { Animatable(0f) }

    var showMenu by remember { mutableStateOf(false) }

    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showArtistDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
    var currentVolume by remember {
        mutableFloatStateOf(
            (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10).toFloat() / maxVolume.toFloat()
        )
    }

    val defaultDarkBg = Color(0xFF1E1A26)
    val defaultBottomBg = Color(0xFF110E17)
    val defaultOnBg = MaterialTheme.colorScheme.onBackground
    val defaultOnSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val defaultPrimary = MaterialTheme.colorScheme.primary

    val targetBgTop = remember(palette) {
        palette?.let {
            val colorInt = it.getDarkVibrantColor(it.getDominantColor(defaultDarkBg.toArgb()))
            Color(colorInt)
        } ?: defaultDarkBg
    }

    val targetBgBottom = remember(palette) {
        palette?.let {
            val colorInt = it.getDarkMutedColor(defaultBottomBg.toArgb())
            Color(colorInt)
        } ?: defaultBottomBg
    }

    val targetTitleColor = remember(palette, defaultOnBg) {
        palette?.let {
            val colorInt = it.getLightVibrantColor(it.getLightMutedColor(defaultOnBg.toArgb()))
            Color(colorInt)
        } ?: defaultOnBg
    }

    val targetArtistColor = remember(palette, defaultOnSurfaceVar) {
        palette?.let {
            val colorInt = it.getLightMutedColor(it.getMutedColor(defaultOnSurfaceVar.toArgb()))
            Color(colorInt)
        } ?: defaultOnSurfaceVar
    }

    val targetVibrant = remember(palette, defaultPrimary) {
        palette?.let {
            val colorInt = it.getVibrantColor(defaultPrimary.toArgb())
            Color(colorInt)
        } ?: defaultPrimary
    }

    val targetLightVibrant = remember(palette, defaultPrimary) {
        palette?.let {
            val colorInt = it.getLightVibrantColor(it.getVibrantColor(defaultPrimary.toArgb()))
            Color(colorInt)
        } ?: defaultPrimary
    }

    val animatedBgTop by animateColorAsState(targetBgTop, tween(400), label = "BgTop")
    val animatedBgBottom by animateColorAsState(targetBgBottom, tween(400), label = "BgBottom")
    val animatedTitleColor by animateColorAsState(targetTitleColor, tween(400), label = "TitleColor")
    val animatedArtistColor by animateColorAsState(targetArtistColor, tween(400), label = "ArtistColor")
    val animatedVibrant by animateColorAsState(targetVibrant, tween(400), label = "Vibrant")
    val animatedLightVibrant by animateColorAsState(targetLightVibrant, tween(400), label = "LightVibrant")

    val totalDuration = if (duration > 0) duration else song.duration.coerceAtLeast(1L)

    var isSeeking by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableFloatStateOf(0f) }

    val progressFraction = if (isSeeking) {
        seekFraction
    } else {
        (currentPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    }

    val currentDisplayPosition = if (isSeeking) {
        (seekFraction * totalDuration).toLong()
    } else {
        currentPosition
    }

    val nextSong = remember(currentPlaylist, song) {
        val index = currentPlaylist.indexOfFirst { it.id == song.id }
        if (index != -1 && currentPlaylist.size > 1) {
            currentPlaylist[(index + 1) % currentPlaylist.size]
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(animatedBgTop, animatedBgBottom)))
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse Player",
                    modifier = Modifier.size(34.dp),
                    tint = animatedTitleColor
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PLAYING FROM YOUR MUSIC",
                    style = MaterialTheme.typography.labelSmall,
                    color = animatedLightVibrant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = song.album.ifEmpty { "Local Tracks" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = animatedTitleColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = animatedTitleColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Playlist") },
                        leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            showPlaylistDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View Artist: ${song.artist}") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            showArtistDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share Track") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Listening to ${song.title}")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Now playing: \"${song.title}\" by ${song.artist} on Mimica Music Player 🎵"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (activeSleepTimerMinutes != null) "Sleep Timer: ${activeSleepTimerMinutes}m left" else "Sleep Timer"
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            showSleepTimerDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Equalizer") },
                        leadingIcon = { Icon(Icons.Default.Equalizer, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            try {
                                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                                }
                                context.startActivity(eqIntent)
                            } catch (e: ActivityNotFoundException) {
                                showEqualizerDialog = true
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. LARGE ALBUM ART SECTION (With Swipe-Down to Collapse Gesture)
        AnimatedContent(
            targetState = song.id,
            transitionSpec = {
                (fadeIn(tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400)))
                    .togetherWith(fadeOut(tween(300)) + scaleOut(targetScale = 0.95f, animationSpec = tween(300)))
            },
            label = "AlbumArtTransition"
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .aspectRatio(1f)
                    .offset { IntOffset(0, dragOffsetY.value.roundToInt()) }
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (dragOffsetY.value >= collapseThresholdPx) {
                                        onCollapse()
                                        dragOffsetY.snapTo(0f)
                                    } else {
                                        dragOffsetY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    dragOffsetY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                if (dragAmount > 0 || dragOffsetY.value > 0) {
                                    change.consume()
                                    coroutineScope.launch {
                                        dragOffsetY.snapTo((dragOffsetY.value + dragAmount).coerceAtLeast(0f))
                                    }
                                }
                            }
                        )
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!song.albumArtUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(song.albumArtUri)
                                .crossfade(500)
                                .allowHardware(false)
                                .build(),
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                            onSuccess = { success ->
                                val bitmap = (success.result.drawable as? BitmapDrawable)?.bitmap
                                if (bitmap != null) {
                                    onImageLoaded(bitmap)
                                }
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(88.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.40f))
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. SONG INFO SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = animatedTitleColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = song.artist,
                style = MaterialTheme.typography.titleMedium,
                color = animatedArtistColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (playbackError != null) {
            Text(
                text = playbackError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 4. SEEK BAR SECTION
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = progressFraction,
                onValueChange = { frac ->
                    isSeeking = true
                    seekFraction = frac
                },
                onValueChangeFinished = {
                    isSeeking = false
                    val targetMs = (seekFraction * totalDuration).toLong()
                    onSeek(targetMs)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = animatedLightVibrant,
                    activeTrackColor = animatedVibrant,
                    inactiveTrackColor = animatedVibrant.copy(alpha = 0.25f)
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentDisplayPosition),
                    style = MaterialTheme.typography.labelMedium,
                    color = animatedArtistColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatTime(totalDuration),
                    style = MaterialTheme.typography.labelMedium,
                    color = animatedArtistColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 5. PLAYBACK CONTROLS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedScaleIconButton(
                onClick = onShuffleClick,
                size = 46.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) animatedLightVibrant else animatedArtistColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedScaleIconButton(
                onClick = onPreviousClick,
                size = 52.dp
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    modifier = Modifier.size(34.dp),
                    tint = animatedTitleColor
                )
            }

            Surface(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(animatedVibrant, animatedLightVibrant))),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = {
                            (fadeIn(tween(200)) + scaleIn(tween(200)))
                                .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200)))
                        },
                        label = "PlayPauseMorph"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            AnimatedScaleIconButton(
                onClick = onNextClick,
                size = 52.dp
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    modifier = Modifier.size(34.dp),
                    tint = animatedTitleColor
                )
            }

            AnimatedScaleIconButton(
                onClick = onRepeatClick,
                size = 46.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeat) animatedLightVibrant else animatedArtistColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 6. VOLUME SLIDER & UP NEXT ROW
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.94f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (currentVolume > 0.5f) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeDown,
                    contentDescription = "Volume",
                    tint = animatedArtistColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Slider(
                    value = currentVolume,
                    onValueChange = { vol ->
                        currentVolume = vol
                        audioManager?.let { am ->
                            val target = (vol * maxVolume).toInt()
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = animatedLightVibrant,
                        activeTrackColor = animatedVibrant,
                        inactiveTrackColor = animatedVibrant.copy(alpha = 0.2f)
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) animatedLightVibrant else animatedArtistColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (nextSong != null) {
                Text(
                    text = "Up Next: ${nextSong.title} • ${nextSong.artist}",
                    style = MaterialTheme.typography.labelSmall,
                    color = animatedArtistColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // 1. Add to Playlist Dialog
    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            song = song,
            playlistViewModel = playlistViewModel,
            onDismiss = { showPlaylistDialog = false }
        )
    }

    // 2. View Artist Songs Dialog
    if (showArtistDialog) {
        val artistTracks = remember(currentPlaylist, song) {
            val matching = currentPlaylist.filter { it.artist.equals(song.artist, ignoreCase = true) }
            if (matching.isNotEmpty()) matching else listOf(song)
        }
        ArtistSongsDialog(
            artistName = song.artist,
            artistTracks = artistTracks,
            onDismiss = { showArtistDialog = false },
            onTrackClick = { selectedTrack ->
                showArtistDialog = false
                onPlayTrack(selectedTrack)
            }
        )
    }

    // 3. Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentTimerMinutes = activeSleepTimerMinutes,
            onDismiss = { showSleepTimerDialog = false },
            onSetTimer = { minutes ->
                showSleepTimerDialog = false
                onSetSleepTimer(minutes)
                if (minutes > 0) {
                    Toast.makeText(context, "Sleep timer set for $minutes minutes", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Sleep timer cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // 4. In-App Equalizer Dialog
    if (showEqualizerDialog) {
        EqualizerDialog(
            onDismiss = { showEqualizerDialog = false }
        )
    }
}

// -------------------------------------------------------------
// DIALOG COMPOSABLES
// -------------------------------------------------------------

@Composable
fun AddToPlaylistDialog(
    song: AudioEntity,
    playlistViewModel: PlaylistViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val playlists by playlistViewModel.playlists.collectAsState()
    var newPlaylistName by remember { mutableStateOf("") }
    var isCreatingNew by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Add to Playlist", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (playlists.isEmpty()) {
                    Text(
                        text = "No custom playlists found. Create one below!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(playlists, key = { it.id }) { pl ->
                            Surface(
                                onClick = {
                                    playlistViewModel.addSongToPlaylist(pl.id, song.id) {
                                        Toast.makeText(context, "Added '${song.title}' to ${pl.name}", Toast.LENGTH_SHORT).show()
                                    }
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pl.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${pl.songCount} songs",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isCreatingNew) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            placeholder = { Text("Playlist name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    playlistViewModel.createPlaylist(newPlaylistName) { newId ->
                                        playlistViewModel.addSongToPlaylist(newId, song.id) {
                                            Toast.makeText(context, "Created '${newPlaylistName.trim()}' and added song", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { isCreatingNew = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Playlist")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ArtistSongsDialog(
    artistName: String,
    artistTracks: List<AudioEntity>,
    onDismiss: () -> Unit,
    onTrackClick: (AudioEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text(
                text = "Artist: $artistName",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${artistTracks.size} tracks by $artistName",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    items(artistTracks, key = { it.id }) { track ->
                        Surface(
                            onClick = { onTrackClick(track) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!track.albumArtUri.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(track.albumArtUri)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize()
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.MusicNote,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = track.album,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = track.durationFormatted,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SleepTimerDialog(
    currentTimerMinutes: Int?,
    onDismiss: () -> Unit,
    onSetTimer: (Int) -> Unit
) {
    val timerOptions = listOf(15, 30, 45, 60)
    var selectedOption by remember { mutableStateOf(currentTimerMinutes ?: 30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Set Sleep Timer", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (currentTimerMinutes != null) "Active: $currentTimerMinutes minutes left" else "Playback will automatically pause after the timer ends.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                timerOptions.forEach { minutes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = minutes }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == minutes,
                            onClick = { selectedOption = minutes }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$minutes minutes",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedOption == minutes) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                if (currentTimerMinutes != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onSetTimer(0) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Turn Off Sleep Timer")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSetTimer(selectedOption) }) {
                Text("Start Timer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EqualizerDialog(
    onDismiss: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.collectAsState()
    val presets = listOf("Normal", "Classical", "Dance", "Flat", "Folk", "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock", "Electronic", "Bass Boost", "Vocal")

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Equalizer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Equalizer", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("Presets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presets) { preset ->
                        val isSelected = settings.equalizerPreset.equals(preset, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { settingsViewModel.setEqualizerPreset(preset) },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                Text("Bass Boost: ${settings.bassBoostLevel}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Slider(
                    value = settings.bassBoostLevel / 100f,
                    onValueChange = { settingsViewModel.setBassBoostLevel((it * 100).toInt()) },
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Virtualizer (Surround)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (settings.virtualizerEnabled) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.virtualizerEnabled,
                        onCheckedChange = { settingsViewModel.setVirtualizerEnabled(it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Preset: ${settings.equalizerPreset}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { 0.7f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AnimatedScaleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    containerColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "ButtonScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .scale(scale),
        shape = shape,
        color = containerColor,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis.coerceAtLeast(0L))
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
