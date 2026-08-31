package com.mimica.musicplayer.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val artworkUrl: String = "",
    val duration: String = "3:30",
    val durationMs: Long = 210000L,
    val audioUrl: String = ""
)
