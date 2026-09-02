package com.mimica.musicplayer.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mimica.musicplayer.data.local.AppDatabase
import com.mimica.musicplayer.data.local.ArtistStats
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.data.local.DayStats
import com.mimica.musicplayer.data.local.HourStats
import com.mimica.musicplayer.data.local.StatsSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val audioDao = AppDatabase.getDatabase(application).audioDao()

    val timeRangeOptions = listOf("Continuous", "1 week", "1 month", "3 months")

    private val _selectedTimeRange = MutableStateFlow("Continuous")
    val selectedTimeRange: StateFlow<String> = _selectedTimeRange.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch(Dispatchers.IO) {
            audioDao.getStatsSummary(0L).collect { summary ->
                Log.d("StatsDebug", "Stats loaded: totalTime: ${summary.totalTimeListened}")
            }
        }
    }

    private val sinceTimestampFlow = _selectedTimeRange.map { range ->
        val now = System.currentTimeMillis()
        when (range) {
            "1 week" -> now - 7L * 24 * 60 * 60 * 1000L
            "1 month" -> now - 30L * 24 * 60 * 60 * 1000L
            "3 months" -> now - 90L * 24 * 60 * 60 * 1000L
            else -> 0L // Continuous
        }
    }

    val statsSummary: StateFlow<StatsSummary> = sinceTimestampFlow
        .flatMapLatest { since -> audioDao.getStatsSummary(since) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsSummary()
        )

    val favoriteSong: StateFlow<AudioEntity?> = sinceTimestampFlow
        .flatMapLatest { since -> audioDao.getFavoriteSong(since) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val favoriteArtist: StateFlow<ArtistStats?> = sinceTimestampFlow
        .flatMapLatest { since -> audioDao.getFavoriteArtist(since) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val artistBreakdown: StateFlow<List<ArtistStats>> = sinceTimestampFlow
        .flatMapLatest { since ->
            audioDao.getArtistBreakdown(since).map { list ->
                val totalPlays = list.sumOf { it.totalPlays }.coerceAtLeast(1)
                list.map { artist ->
                    val percentage = (artist.totalPlays.toFloat() / totalPlays.toFloat()) * 100f
                    artist.copy(percentage = percentage)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val playedSongsFlow = sinceTimestampFlow
        .flatMapLatest { since -> audioDao.getPlayedAudio(since) }

    val dayStats: StateFlow<List<DayStats>> = playedSongsFlow.map { songs ->
        val daysOfWeek = listOf(
            Pair("Sun", Calendar.SUNDAY),
            Pair("Mon", Calendar.MONDAY),
            Pair("Tue", Calendar.TUESDAY),
            Pair("Wed", Calendar.WEDNESDAY),
            Pair("Thu", Calendar.THURSDAY),
            Pair("Fri", Calendar.FRIDAY),
            Pair("Sat", Calendar.SATURDAY)
        )

        val cal = Calendar.getInstance()
        val playMap = mutableMapOf<Int, Pair<Int, Long>>() // dayOfWeek -> (plays, time)

        songs.forEach { song ->
            if (song.lastPlayed > 0) {
                cal.timeInMillis = song.lastPlayed
                val day = cal.get(Calendar.DAY_OF_WEEK)
                val current = playMap[day] ?: Pair(0, 0L)
                playMap[day] = Pair(current.first + song.plays, current.second + song.totalTime)
            }
        }

        daysOfWeek.map { (name, calDay) ->
            val data = playMap[calDay] ?: Pair(0, 0L)
            DayStats(
                dayName = name,
                dayOfWeek = calDay,
                plays = data.first,
                timeListened = data.second
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(
            DayStats("Sun", Calendar.SUNDAY, 0, 0),
            DayStats("Mon", Calendar.MONDAY, 0, 0),
            DayStats("Tue", Calendar.TUESDAY, 0, 0),
            DayStats("Wed", Calendar.WEDNESDAY, 0, 0),
            DayStats("Thu", Calendar.THURSDAY, 0, 0),
            DayStats("Fri", Calendar.FRIDAY, 0, 0),
            DayStats("Sat", Calendar.SATURDAY, 0, 0)
        )
    )

    val hourStats: StateFlow<List<HourStats>> = playedSongsFlow.map { songs ->
        val keyHours = listOf(
            Pair(0, "12AM"),
            Pair(6, "6AM"),
            Pair(12, "12PM"),
            Pair(18, "6PM"),
            Pair(23, "12AM")
        )

        val cal = Calendar.getInstance()
        val hourPlayMap = mutableMapOf<Int, Int>()

        songs.forEach { song ->
            if (song.lastPlayed > 0) {
                cal.timeInMillis = song.lastPlayed
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                hourPlayMap[hour] = (hourPlayMap[hour] ?: 0) + song.plays
            }
        }

        // Map into representative chart slots
        keyHours.map { (hr, label) ->
            val count = when (hr) {
                0 -> (hourPlayMap[0] ?: 0) + (hourPlayMap[1] ?: 0) + (hourPlayMap[2] ?: 0)
                6 -> (hourPlayMap[5] ?: 0) + (hourPlayMap[6] ?: 0) + (hourPlayMap[7] ?: 0)
                12 -> (hourPlayMap[11] ?: 0) + (hourPlayMap[12] ?: 0) + (hourPlayMap[13] ?: 0)
                18 -> (hourPlayMap[17] ?: 0) + (hourPlayMap[18] ?: 0) + (hourPlayMap[19] ?: 0)
                else -> (hourPlayMap[22] ?: 0) + (hourPlayMap[23] ?: 0)
            }
            HourStats(hour = hr, label = label, plays = count)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(
            HourStats(0, "12AM", 0),
            HourStats(6, "6AM", 0),
            HourStats(12, "12PM", 0),
            HourStats(18, "6PM", 0),
            HourStats(23, "12AM", 0)
        )
    )

    val mostActiveHour: StateFlow<String> = playedSongsFlow.map { songs ->
        val cal = Calendar.getInstance()
        val hourCounts = mutableMapOf<Int, Int>()

        songs.forEach { song ->
            if (song.lastPlayed > 0) {
                cal.timeInMillis = song.lastPlayed
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                hourCounts[hour] = (hourCounts[hour] ?: 0) + song.plays
            }
        }

        val topHour = hourCounts.maxByOrNull { it.value }?.key ?: 13
        val formatted = when {
            topHour == 0 -> "12 AM"
            topHour < 12 -> "$topHour AM"
            topHour == 12 -> "12 PM"
            else -> "${topHour - 12} PM"
        }
        "Most active: $formatted"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Most active: 1 PM"
    )

    fun setTimeRange(range: String) {
        _selectedTimeRange.value = range
    }
}
