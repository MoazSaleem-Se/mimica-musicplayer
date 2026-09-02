package com.mimica.musicplayer.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.data.local.PlaylistEntity
import java.io.File

fun copyUriToInternalStorage(context: Context, sourceUri: Uri, prefix: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val filename = "${prefix}_${System.currentTimeMillis()}.jpg"
        val destFile = File(context.filesDir, filename)
        destFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        destFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun EditSongMetadataDialog(
    song: AudioEntity,
    onDismiss: () -> Unit,
    onSave: (customArtist: String?, customArtworkUri: String?) -> Unit
) {
    val context = LocalContext.current
    var customArtist by remember { mutableStateOf(song.displayArtist) }
    var currentArtwork by remember { mutableStateOf(song.displayArtworkUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val localPath = copyUriToInternalStorage(context, uri, "song_art_${song.id}")
            if (localPath != null) {
                currentArtwork = localPath
            }
        }
    }

    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val localPath = copyUriToInternalStorage(context, uri, "song_art_${song.id}")
            if (localPath != null) {
                currentArtwork = localPath
            }
        }
    }

    fun launchImagePicker() {
        try {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                legacyPickerLauncher.launch("image/*")
            }
        } catch (e: Exception) {
            legacyPickerLauncher.launch("image/*")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Song Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Artwork Preview & Change Trigger
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { launchImagePicker() },
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentArtwork.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentArtwork)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Overlay badge
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(topStart = 8.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Artwork",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                TextButton(onClick = { launchImagePicker() }) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Change Artwork")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customArtist,
                    onValueChange = { customArtist = it },
                    label = { Text("Artist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (song.customArtistName != null || song.customArtworkUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            customArtist = song.artist
                            currentArtwork = song.albumArtUri
                        }
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset to Default")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalArtist = if (customArtist.trim() == song.artist || customArtist.isBlank()) null else customArtist.trim()
                    val finalArtwork = if (currentArtwork == song.albumArtUri) null else currentArtwork
                    onSave(finalArtist, finalArtwork)
                    onDismiss()
                }
            ) {
                Text("Save")
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
fun EditPlaylistArtworkDialog(
    playlist: PlaylistEntity,
    onDismiss: () -> Unit,
    onSave: (customArtworkUri: String?) -> Unit
) {
    val context = LocalContext.current
    var currentArtwork by remember { mutableStateOf(playlist.customArtworkUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val localPath = copyUriToInternalStorage(context, uri, "playlist_art_${playlist.id}")
            if (localPath != null) {
                currentArtwork = localPath
            }
        }
    }

    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val localPath = copyUriToInternalStorage(context, uri, "playlist_art_${playlist.id}")
            if (localPath != null) {
                currentArtwork = localPath
            }
        }
    }

    fun launchImagePicker() {
        try {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                legacyPickerLauncher.launch("image/*")
            }
        } catch (e: Exception) {
            legacyPickerLauncher.launch("image/*")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Playlist Artwork", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { launchImagePicker() },
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentArtwork.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentArtwork)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Playlist Artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(onClick = { launchImagePicker() }) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Choose Image")
                }

                if (currentArtwork != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(onClick = { currentArtwork = null }) {
                        Text("Remove Artwork")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(currentArtwork)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
