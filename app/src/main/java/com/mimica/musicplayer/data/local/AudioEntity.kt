package com.mimica.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale
import java.util.concurrent.TimeUnit

@Entity(tableName = "audio")
data class AudioEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val filePath: String,
    val albumArtUri: String? = null,
    val albumId: Long = 0L,
    val plays: Int = 0,
    val lastPlayed: Long = 0L,
    val totalTime: Long = 0L
) {
    val durationFormatted: String
        get() {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes)
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
}
