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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.mimica.musicplayer.data.local.PlaylistEntity
import com.mimica.musicplayer.ui.components.AddToPlaylistDialog
import com.mimica.musicplayer.ui.components.EditPlaylistArtworkDialog
import com.mimica.musicplayer.ui.components.EditSongMetadataDialog
import com.mimica.musicplayer.ui.viewmodel.MusicScanViewModel
import com.mimica.musicplayer.ui.viewmodel.PlayerViewModel
import com.mimica.musicplayer.ui.viewmodel.PlaylistViewModel
import com.mimica.musicplayer.ui.viewmodel.ScanUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onAudioClick: (AudioEntity, List<AudioEntity>) -> Unit = { _, _ -> },
    scanViewModel: MusicScanViewModel = viewModel(),
    playlistViewModel: PlaylistViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val scanState by scanViewModel.scanState.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlayerPlaying by playerViewModel.isPlaying.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState()

    var selectedPlaylistForDetail by remember { mutableStateOf<PlaylistEntity?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var songForPlaylistDialog by remember { mutableStateOf<AudioEntity?>(null) }
    var songToEdit by remember { mutableStateOf<AudioEntity?>(null) }
    var playlistToEditArtwork by remember { mutableStateOf<PlaylistEntity?>(null) }

    val songs: List<AudioEntity> = remember(scanState) {
        when (val state = scanState) {
            is ScanUiState.Success -> state.songs
            else -> emptyList()
        }
    }

    var selectedFilter by remember { mutableStateOf("All Tracks") }
    val filters = listOf("All Tracks", "Playlists", "Artists", "Albums")

    val artistGroups = remember(songs) {
        songs.groupBy { it.displayArtist }.toList().sortedBy { it.first.lowercase() }
    }

    val albumGroups = remember(songs) {
        songs.groupBy { it.album }.toList().sortedBy { it.first.lowercase() }
    }

    // Detail Screen Display
    if (selectedPlaylistForDetail != null) {
        PlaylistDetailScreen(
            playlist = selectedPlaylistForDetail!!,
            playlistViewModel = playlistViewModel,
            playerViewModel = playerViewModel,
            onBack = { selectedPlaylistForDetail = null },
            onAudioClick = onAudioClick
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePlaylistDialog = true },
                modifier = Modifier.padding(bottom = 80.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Playlist"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = { scanViewModel.scanMusic() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan Library",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(text = filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (songs.isEmpty() && playlists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Library is Empty",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rescan to populate songs or create playlists.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { scanViewModel.scanMusic() }) {
                        Text("Scan Device")
                    }
                }
            } else {
                when (selectedFilter) {
                    "All Tracks" -> {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 96.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "${songs.size} Songs",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                )
                            }

                            items(songs, key = { it.id }) { audio ->
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
                                    ScannedSongListItemContent(
                                        audio = audio,
                                        isPlaying = isSongPlaying,
                                        onEditClick = { songToEdit = audio },
                                        onClick = {
                                            if (audio.filePath.isBlank()) {
                                                Toast.makeText(context, "This song is not available offline", Toast.LENGTH_SHORT).show()
                                            } else if (isCurrentSong) {
                                                playerViewModel.togglePlayPause()
                                            } else {
                                                onAudioClick(audio, songs)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    "Playlists" -> {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 96.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${playlists.size + 1} Playlists",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(onClick = { showCreatePlaylistDialog = true }) {
                                        Text("+ Create New")
                                    }
                                }
                            }

                            item {
                                LibraryGroupRowItem(
                                    title = "All Scanned Music",
                                    subtitle = "${songs.size} tracks • Local storage",
                                    icon = Icons.Default.Folder,
                                    artUri = songs.firstOrNull { !it.displayArtworkUri.isNullOrEmpty() }?.displayArtworkUri,
                                    onClick = {
                                        val first = songs.firstOrNull()
                                        if (first != null && first.filePath.isNotBlank()) {
                                            onAudioClick(first, songs)
                                        } else {
                                            Toast.makeText(context, "This song is not available offline", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }

                            items(playlists, key = { it.id }) { pl ->
                                LibraryGroupRowItem(
                                    title = pl.name,
                                    subtitle = "${pl.songCount} tracks",
                                    icon = Icons.Default.QueueMusic,
                                    artUri = pl.customArtworkUri,
                                    onClick = {
                                        selectedPlaylistForDetail = pl
                                    }
                                )
                            }
                        }
                    }

                    "Artists" -> {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 96.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "${artistGroups.size} Artists",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                )
                            }

                            items(artistGroups) { (artist, artistSongs) ->
                                LibraryGroupRowItem(
                                    title = artist,
                                    subtitle = "${artistSongs.size} tracks",
                                    icon = Icons.Default.Person,
                                    artUri = artistSongs.firstOrNull { !it.displayArtworkUri.isNullOrEmpty() }?.displayArtworkUri,
                                    onClick = {
                                        val firstTrack = artistSongs.firstOrNull()
                                        if (firstTrack != null && firstTrack.filePath.isNotBlank()) {
                                            onAudioClick(firstTrack, artistSongs)
                                        } else {
                                            Toast.makeText(context, "This song is not available offline", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    "Albums" -> {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 96.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "${albumGroups.size} Albums",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                )
                            }

                            items(albumGroups) { (album, albumSongs) ->
                                LibraryGroupRowItem(
                                    title = album,
                                    subtitle = "${albumSongs.size} tracks",
                                    icon = Icons.Default.Album,
                                    artUri = albumSongs.firstOrNull { !it.displayArtworkUri.isNullOrEmpty() }?.displayArtworkUri,
                                    onClick = {
                                        val firstTrack = albumSongs.firstOrNull()
                                        if (firstTrack != null && firstTrack.filePath.isNotBlank()) {
                                            onAudioClick(firstTrack, albumSongs)
                                        } else {
                                            Toast.makeText(context, "This song is not available offline", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        var playlistNameInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("New Playlist") },
            text = {
                Column {
                    Text(
                        text = "Enter a name for your new playlist:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = playlistNameInput,
                        onValueChange = { playlistNameInput = it },
                        placeholder = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistNameInput.isNotBlank()) {
                            playlistViewModel.createPlaylist(playlistNameInput) {
                                Toast.makeText(context, "Created playlist '${playlistNameInput.trim()}'", Toast.LENGTH_SHORT).show()
                            }
                            showCreatePlaylistDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (songForPlaylistDialog != null) {
        AddToPlaylistDialog(
            song = songForPlaylistDialog!!,
            playlistViewModel = playlistViewModel,
            onDismiss = { songForPlaylistDialog = null }
        )
    }

    if (songToEdit != null) {
        EditSongMetadataDialog(
            song = songToEdit!!,
            onDismiss = { songToEdit = null },
            onSave = { newArtist, newArtwork ->
                playerViewModel.updateSongCustomMetadata(songToEdit!!.id, newArtist, newArtwork)
            }
        )
    }

    if (playlistToEditArtwork != null) {
        EditPlaylistArtworkDialog(
            playlist = playlistToEditArtwork!!,
            onDismiss = { playlistToEditArtwork = null },
            onSave = { newArtwork ->
                playlistViewModel.updatePlaylistArtwork(playlistToEditArtwork!!.id, newArtwork)
            }
        )
    }
}

@Composable
fun LibraryGroupRowItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    artUri: String? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 6.dp),
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
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!artUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
