package com.mimica.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {

    @Query("SELECT * FROM audio ORDER BY title ASC")
    fun getAllAudio(): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audio ORDER BY title ASC")
    suspend fun getAllAudioList(): List<AudioEntity>

    @Query("SELECT * FROM audio WHERE id = :id LIMIT 1")
    suspend fun getAudioById(id: Long): AudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(audioList: List<AudioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audio: AudioEntity)

    @Delete
    suspend fun delete(audio: AudioEntity)

    @Query("DELETE FROM audio")
    suspend fun clearAll()
}
