package com.mimica.musicplayer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.ui.components.AddToPlaylistDialog
import com.mimica.musicplayer.ui.components.EditSongMetadataDialog
import com.mimica.musicplayer.ui.viewmodel.MusicScanViewModel
import com.mimica.musicplayer.ui.viewmodel.PlayerViewModel
import com.mimica.musicplayer.ui.viewmodel.PlaylistViewModel
import com.mimica.musicplayer.ui.viewmodel.ScanUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAudioClick: (AudioEntity, List<AudioEntity>) -> Unit = { _, _ -> },
    scanViewModel: MusicScanViewModel = viewModel(),
    playlistViewModel: PlaylistViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val scanState by scanViewModel.scanState.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlayerPlaying by playerViewModel.isPlaying.collectAsState()

    var songForPlaylistDialog by remember { mutableStateOf<AudioEntity?>(null) }
    var songToEdit by remember { mutableStateOf<AudioEntity?>(null) }

    val allLocalSongs: List<AudioEntity> = remember(scanState) {
        when (val state = scanState) {
            is ScanUiState.Success -> state.songs
            else -> emptyList()
        }
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs = remember(searchQuery, allLocalSongs) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allLocalSongs.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                        song.displayArtist.contains(searchQuery, ignoreCase = true) ||
                        song.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = {
                Text(
                    text = "Search songs, artists, albums...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isBlank()) {
            // Empty State (No query yet)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Search for songs, artists, or albums",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Type in the search bar above to quickly find local music tracks on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (filteredSongs.isNotEmpty()) {
            // Results Found List
            Text(
                text = "Results (${filteredSongs.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSongs, key = { it.id }) { audio ->
                    val coroutineScope = rememberCoroutineScope()
                    lateinit var dismissStateRef: SwipeToDismissBoxState
                    val dismissState = rememberSwipeToDismissBoxState(
                        positionalThreshold = { totalDistance -> totalDistance * 0.65f },
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                songForPlaylistDialog = audio
                                coroutineScope.launch { dismissStateRef.reset() }
                            }
                            false
                        }
                    )
                    dismissStateRef = dismissState

                    val isCurrentSong = currentSong?.id == audio.id
                    val isSongPlaying = isCurrentSong && isPlayerPlaying

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = "Add to Playlist",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    ) {
                        SearchSongListItemContent(
                            audio = audio,
                            isPlaying = isSongPlaying,
                            onEditClick = { songToEdit = audio },
                            onClick = {
                                if (audio.filePath.isBlank()) {
                                    Toast.makeText(context, "This song is not available offline", Toast.LENGTH_SHORT).show()
                                } else if (isCurrentSong) {
                                    playerViewModel.togglePlayPause()
                                } else {
                                    onAudioClick(audio, filteredSongs)
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // No Results Found
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No matching songs found",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Try searching with a different title, artist, or album name.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Add to Playlist Dialog
    if (songForPlaylistDialog != null) {
        AddToPlaylistDialog(
            song = songForPlaylistDialog!!,
            playlistViewModel = playlistViewModel,
            onDismiss = { songForPlaylistDialog = null }
        )
    }

    // Edit Song Details Dialog
    if (songToEdit != null) {
        EditSongMetadataDialog(
            song = songToEdit!!,
            onDismiss = { songToEdit = null },
            onSave = { newArtist, newArtwork ->
                playerViewModel.updateSongCustomMetadata(songToEdit!!.id, newArtist, newArtwork)
            }
        )
    }
}

@Composable
fun SearchSongListItemContent(
    audio: AudioEntity,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = lerp(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant,
            0.4f
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!audio.displayArtworkUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(audio.displayArtworkUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = audio.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audio.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${audio.displayArtist} • ${audio.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // File format badge and duration
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = audio.fileFormat,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = audio.durationFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onEditClick != null) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Song",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
