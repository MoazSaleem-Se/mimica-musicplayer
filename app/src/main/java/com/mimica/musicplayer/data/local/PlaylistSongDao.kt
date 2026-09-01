package com.mimica.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistSongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylist(playlistSong: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("SELECT a.* FROM audio a INNER JOIN playlist_songs ps ON a.id = ps.songId WHERE ps.playlistId = :playlistId ORDER BY ps.position ASC, a.title ASC")
    fun getSongsForPlaylist(playlistId: Long): Flow<List<AudioEntity>>

    @Query("SELECT a.* FROM audio a INNER JOIN playlist_songs ps ON a.id = ps.songId WHERE ps.playlistId = :playlistId ORDER BY ps.position ASC, a.title ASC")
    suspend fun getSongsForPlaylistList(playlistId: Long): List<AudioEntity>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getSongCount(playlistId: Long): Int

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)
}
