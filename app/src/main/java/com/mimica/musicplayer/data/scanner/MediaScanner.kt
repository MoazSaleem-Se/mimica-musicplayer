package com.mimica.musicplayer.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.mimica.musicplayer.data.local.AudioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaScanner(private val context: Context) {

    private data class ScannedAudio(
        val entity: AudioEntity,
        val fileSize: Long
    )

    /**
     * Scans local audio files from MediaStore using ContentResolver.
     * Extracts title, artist, album, duration, file path, MIME type / format, and album art URI.
     * Robustly skips corrupted entries, files residing in excluded folders,
     * and performs two-tier deduplication: (1) path-based and (2) content-based (title + artist + duration + size).
     */
    suspend fun scanLocalMusic(excludedFolders: Set<String> = emptySet()): List<AudioEntity> = withContext(Dispatchers.IO) {
        val rawAudioList = mutableListOf<ScannedAudio>()
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
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
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
                val mimeTypeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn)?.trim() ?: "Unknown Title"
                        val artist = cursor.getString(artistColumn)?.trim() ?: "Unknown Artist"
                        val album = cursor.getString(albumColumn)?.trim() ?: "Unknown Album"
                        val duration = cursor.getLong(durationColumn)
                        val filePath = cursor.getString(dataColumn) ?: ""
                        val albumId = cursor.getLong(albumIdColumn)
                        val mimeType = if (mimeTypeColumn != -1) cursor.getString(mimeTypeColumn) else null
                        val fileSize = if (sizeColumn != -1) cursor.getLong(sizeColumn) else 0L

                        // Check if file is inside an excluded folder
                        if (isPathExcluded(filePath, excludedFolders)) {
                            continue
                        }

                        // Determine file format display label
                        val fileFormat = extractFileFormat(mimeType, filePath)

                        // Album art URI using MediaStore.Audio.Albums
                        val albumArtUri = if (albumId > 0) {
                            ContentUris.withAppendedId(
                                Uri.parse("content://media/external/audio/albumart"),
                                albumId
                            ).toString()
                        } else null

                        // Skip corrupted entries with invalid duration or missing path
                        if (id > 0 && duration > 0 && filePath.isNotEmpty()) {
                            rawAudioList.add(
                                ScannedAudio(
                                    entity = AudioEntity(
                                        id = id,
                                        title = if (title.isBlank()) "Unknown Title" else title,
                                        artist = if (artist.isBlank() || artist == "<unknown>") "Unknown Artist" else artist,
                                        album = if (album.isBlank() || album == "<unknown>") "Unknown Album" else album,
                                        duration = duration,
                                        filePath = filePath,
                                        albumArtUri = albumArtUri,
                                        albumId = albumId,
                                        fileFormat = fileFormat
                                    ),
                                    fileSize = fileSize
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

        val rawTotalCount = rawAudioList.size

        // -------------------------------------------------------------
        // Step 1: Diagnostic Logging for Duplicate Candidate Groups
        // -------------------------------------------------------------
        val candidateGroups = rawAudioList.groupBy {
            "${it.entity.title.lowercase().trim()}|${it.entity.artist.lowercase().trim()}|${it.entity.duration}"
        }

        candidateGroups.filter { it.value.size > 1 }.forEach { (key, group) ->
            val first = group.first().entity
            Log.d(
                "MediaScanner",
                "[DIAGNOSTIC] Duplicate candidate group found (count=${group.size}): Key='$key', Title='${first.title}', Artist='${first.artist}', Duration=${first.duration}ms"
            )
            group.forEachIndexed { index, item ->
                Log.d(
                    "MediaScanner",
                    "  [Entry $index] ID=${item.entity.id}, Size=${item.fileSize} bytes, Path='${item.entity.filePath}'"
                )
            }
        }

        // -------------------------------------------------------------
        // Step 2: Pass 1 - Path-Based Deduplication
        // -------------------------------------------------------------
        val pathDeduplicated = rawAudioList
            .groupBy { it.entity.filePath.lowercase() }
            .mapValues { (_, entries) -> entries.maxByOrNull { it.entity.id } ?: entries.first() }
            .values
            .toList()

        val afterPass1Count = pathDeduplicated.size

        // -------------------------------------------------------------
        // Step 3: Pass 2 - Content-Based Deduplication (Title + Artist + Duration + File Size)
        // -------------------------------------------------------------
        val contentDeduplicated = pathDeduplicated
            .groupBy { item ->
                val normTitle = item.entity.title.lowercase().trim()
                val normArtist = item.entity.artist.lowercase().trim()
                val duration = item.entity.duration
                val size = item.fileSize
                "$normTitle|$normArtist|$duration|$size"
            }
            .mapValues { (_, entries) -> selectPreferredEntry(entries) }
            .values
            .map { it.entity }
            .sortedBy { it.title.lowercase() }

        val finalCount = contentDeduplicated.size

        Log.d(
            "MediaScanner",
            "MediaStore scan complete: rawCount=$rawTotalCount -> afterPass1(path)=$afterPass1Count -> finalCount(content)=$finalCount (total duplicates removed: ${rawTotalCount - finalCount})"
        )

        contentDeduplicated
    }

    private fun selectPreferredEntry(entries: List<ScannedAudio>): ScannedAudio {
        if (entries.size == 1) return entries.first()

        // Path preference comparator:
        // 1. Prefer canonical storage (/storage/emulated/...) over legacy mounts (/sdcard/, /mnt/...)
        // 2. Prefer dedicated Music/Audio directories over generic folders (/Documents/, /Download/, etc.)
        // 3. Prefer highest MediaStore ID (most recently added/scanned)
        return entries.maxWithOrNull(
            compareBy<ScannedAudio> { item ->
                val path = item.entity.filePath
                when {
                    path.startsWith("/storage/emulated/", ignoreCase = true) -> 2
                    path.startsWith("/sdcard/", ignoreCase = true) -> 1
                    path.startsWith("/mnt/", ignoreCase = true) -> 0
                    else -> 1
                }
            }.thenBy { item ->
                val path = item.entity.filePath
                when {
                    path.contains("/Music/", ignoreCase = true) || path.contains("/Audio/", ignoreCase = true) -> 2
                    path.contains("/Downloads/", ignoreCase = true) || path.contains("/Download/", ignoreCase = true) -> 1
                    path.contains("/Documents/", ignoreCase = true) -> 0
                    else -> 1
                }
            }.thenBy { item ->
                item.entity.id
            }
        ) ?: entries.first()
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

        fun extractFileFormat(mimeType: String?, filePath: String): String {
            val mime = mimeType?.lowercase()?.trim() ?: ""
            return when {
                mime == "audio/mpeg" || mime == "audio/mp3" -> "MP3"
                mime == "audio/flac" || mime == "audio/x-flac" -> "FLAC"
                mime == "audio/x-wav" || mime == "audio/wav" -> "WAV"
                mime == "audio/ogg" || mime == "application/ogg" || mime == "audio/vorbis" -> "OGG"
                mime == "audio/mp4" || mime == "audio/m4a" -> "M4A"
                mime == "audio/aac" || mime == "audio/aacp" -> "AAC"
                mime == "audio/opus" -> "OPUS"
                mime == "audio/3gpp" || mime == "audio/amr" -> "AMR"
                mime == "audio/midi" || mime == "audio/mid" -> "MIDI"
                else -> {
                    val ext = filePath.substringAfterLast('.', "").uppercase().trim()
                    if (ext.isNotBlank() && ext.length in 2..5) ext else "MP3"
                }
            }
        }
    }
}
