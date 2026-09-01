package com.mimica.musicplayer.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index(value = ["songId"])]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val position: Int = 0
)
