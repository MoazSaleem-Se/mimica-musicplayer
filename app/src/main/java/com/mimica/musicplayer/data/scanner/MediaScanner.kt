package com.mimica.musicplayer.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.mimica.musicplayer.data.local.AudioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaScanner(private val context: Context) {

    /**
     * Scans local audio files from MediaStore using ContentResolver.
     * Extracts title, artist, album, duration, file path, and album art URI.
     * Robustly skips corrupted entries and files residing in excluded folders.
     */
    suspend fun scanLocalMusic(excludedFolders: Set<String> = emptySet()): List<AudioEntity> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<AudioEntity>()
        val contentResolver: ContentResolver = context.contentResolver

        val collectionUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Filter for music files with duration >= 10 seconds (excludes ringtones and alert sounds)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            contentResolver.query(
                collectionUri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn)?.trim() ?: "Unknown Title"
                        val artist = cursor.getString(artistColumn)?.trim() ?: "Unknown Artist"
                        val album = cursor.getString(albumColumn)?.trim() ?: "Unknown Album"
                        val duration = cursor.getLong(durationColumn)
                        val filePath = cursor.getString(dataColumn) ?: ""
                        val albumId = cursor.getLong(albumIdColumn)

                        // Check if file is inside an excluded folder
                        if (isPathExcluded(filePath, excludedFolders)) {
                            continue
                        }

                        // Album art URI using MediaStore.Audio.Albums
                        val albumArtUri = if (albumId > 0) {
                            ContentUris.withAppendedId(
                                Uri.parse("content://media/external/audio/albumart"),
                                albumId
                            ).toString()
                        } else null

                        // Skip corrupted entries with invalid duration or missing path
                        if (id > 0 && duration > 0 && filePath.isNotEmpty()) {
                            audioList.add(
                                AudioEntity(
                                    id = id,
                                    title = if (title.isBlank()) "Unknown Title" else title,
                                    artist = if (artist.isBlank() || artist == "<unknown>") "Unknown Artist" else artist,
                                    album = if (album.isBlank() || album == "<unknown>") "Unknown Album" else album,
                                    duration = duration,
                                    filePath = filePath,
                                    albumArtUri = albumArtUri,
                                    albumId = albumId
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Skip individual corrupted records without crashing the whole scan
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        audioList
    }

    companion object {
        fun isPathExcluded(filePath: String, excludedFolders: Set<String>): Boolean {
            if (excludedFolders.isEmpty() || filePath.isBlank()) return false
            return excludedFolders.any { excluded ->
                val cleanExcluded = excluded.trim().removeSuffix("/")
                filePath.equals(cleanExcluded, ignoreCase = true) ||
                        filePath.startsWith("$cleanExcluded/", ignoreCase = true)
            }
        }
    }
}
