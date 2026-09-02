package com.mimica.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class ArtistStats(
    val artist: String,
    val songCount: Int,
    val totalPlays: Int,
    val totalTime: Long,
    val percentage: Float = 0f
)

data class StatsSummary(
    val totalTimeListened: Long = 0L,
    val totalPlays: Int = 0,
    val uniqueSongs: Int = 0,
    val uniqueArtists: Int = 0
)

data class DayStats(
    val dayName: String, // "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    val dayOfWeek: Int,  // 1 = Sun, 2 = Mon ... 7 = Sat
    val plays: Int,
    val timeListened: Long
)

data class HourStats(
    val hour: Int,       // 0..23
    val label: String,   // "12AM", "6AM", "12PM", "6PM"
    val plays: Int
)

data class AudioCustomMetadata(
    val id: Long,
    val customArtistName: String?,
    val customArtworkUri: String?
)

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

    @Update
    suspend fun update(audio: AudioEntity)

    @Query("UPDATE audio SET customArtistName = :customArtistName, customArtworkUri = :customArtworkUri WHERE id = :id")
    suspend fun updateCustomMetadata(id: Long, customArtistName: String?, customArtworkUri: String?)

    @Query("SELECT id, customArtistName, customArtworkUri FROM audio WHERE customArtistName IS NOT NULL OR customArtworkUri IS NOT NULL")
    suspend fun getCustomMetadataList(): List<AudioCustomMetadata>

    @Delete
    suspend fun delete(audio: AudioEntity)

    @Query("DELETE FROM audio")
    suspend fun clearAll()

    // --- Stats Queries ---

    @Query("UPDATE audio SET plays = plays + 1, lastPlayed = :timestamp, totalTime = totalTime + :time WHERE id = :songId")
    suspend fun incrementStats(songId: Long, timestamp: Long, time: Long)

    @Query("SELECT * FROM audio WHERE plays > 0 AND lastPlayed >= :sinceTimestamp ORDER BY plays DESC, totalTime DESC LIMIT 1")
    fun getFavoriteSong(sinceTimestamp: Long = 0L): Flow<AudioEntity?>

    @Query("SELECT artist, COUNT(id) as songCount, SUM(plays) as totalPlays, SUM(totalTime) as totalTime, 0.0 as percentage FROM audio WHERE plays > 0 AND lastPlayed >= :sinceTimestamp GROUP BY artist ORDER BY totalPlays DESC, totalTime DESC LIMIT 1")
    fun getFavoriteArtist(sinceTimestamp: Long = 0L): Flow<ArtistStats?>

    @Query("SELECT artist, COUNT(id) as songCount, SUM(plays) as totalPlays, SUM(totalTime) as totalTime, 0.0 as percentage FROM audio WHERE plays > 0 AND lastPlayed >= :sinceTimestamp GROUP BY artist ORDER BY totalPlays DESC, totalTime DESC LIMIT 5")
    fun getArtistBreakdown(sinceTimestamp: Long = 0L): Flow<List<ArtistStats>>

    @Query("SELECT COALESCE(SUM(totalTime), 0) as totalTimeListened, COALESCE(SUM(plays), 0) as totalPlays, COUNT(DISTINCT id) as uniqueSongs, COUNT(DISTINCT artist) as uniqueArtists FROM audio WHERE plays > 0 AND lastPlayed >= :sinceTimestamp")
    fun getStatsSummary(sinceTimestamp: Long = 0L): Flow<StatsSummary>

    @Query("SELECT * FROM audio WHERE plays > 0 AND lastPlayed >= :sinceTimestamp")
    fun getPlayedAudio(sinceTimestamp: Long = 0L): Flow<List<AudioEntity>>
}
