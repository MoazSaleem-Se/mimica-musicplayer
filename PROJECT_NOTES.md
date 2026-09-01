# Project Documentation - Mimica Music Player

Generated on 2026-08-31 based on source analysis of the repository.

---

## 1. Architecture Overview

### Package / Directory Breakdown (`app/src/main/java/com/mimica/musicplayer/`)

- **`data/local/`**:
  - `AppDatabase.kt`: Abstract Room database singleton (`"music_player_database"`), database version 2, registers `AudioEntity`, `PlaylistEntity`, and `PlaylistSongEntity`.
  - `AudioDao.kt`: Room DAO interface for SQLite queries on the `audio` table (`getAllAudio()`, `getAllAudioList()`, `getAudioById()`, `insertAll()`, `insert()`, `delete()`, `clearAll()`).
  - `AudioEntity.kt`: Room `@Entity` representing a scanned audio file (`id`, `title`, `artist`, `album`, `duration`, `filePath`, `albumArtUri`, `albumId`, and computed property `durationFormatted`).
  - `PlaylistEntity.kt`: Room `@Entity` representing a user playlist (`id`, `name`, `createdAt`, `songCount`).
  - `PlaylistSongEntity.kt`: Room `@Entity` cross-reference join table (`playlistId`, `songId`, `position`).
  - `PlaylistDao.kt`: Room DAO interface for CRUD operations on playlists (`getAllPlaylists()`, `getPlaylistById()`, `insertPlaylist()`, `updatePlaylist()`, `deletePlaylist()`, `deletePlaylistById()`).
  - `PlaylistSongDao.kt`: Room DAO interface for managing songs in playlists (`insertSongToPlaylist()`, `removeSongFromPlaylist()`, `getSongsForPlaylist()`, `getSongCount()`, `clearPlaylistSongs()`).
- **`data/repository/`**:
  - `MusicRepository.kt`: Bridge between Room database (`AudioDao`) and device media scanner (`MediaScanner`). Exposes `cachedAudioFlow`, `getCachedAudio()`, and `scanAndCacheMusic()`.
- **`data/scanner/`**:
  - `MediaScanner.kt`: Scans local device audio using `ContentResolver` querying `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` / `VOLUME_EXTERNAL`. Filters tracks with `IS_MUSIC != 0` and `DURATION >= 10000` (10 seconds). Builds album artwork URI via `content://media/external/audio/albumart/<albumId>`.
- **`model/`**:
  - `Song.kt`: Data class model (`id`, `title`, `artist`, `album`, `artworkUrl`, `duration`, `durationMs`, `audioUrl`).
- **`playback/`**:
  - `MusicPlayerService.kt`: `MediaSessionService` (Media3) handling background playback. Creates `ExoPlayer` with `AudioAttributes` (`USAGE_MEDIA`, `AUDIO_CONTENT_TYPE_MUSIC`, `handleAudioFocus = true` to pause on calls and resume after, and `handleAudioBecomingNoisy = true`). Creates a `MediaSession` with an immutable `PendingIntent` launching `MainActivity`.
- **`ui/components/`**:
  - `NowPlayingBottomSheet.kt`: Main active player UI component, gestures, and modal controllers. Contains:
    - `NowPlayingBottomSheet`: Controls `AnimatedVisibility` for the floating mini player and launches `ModalBottomSheet` for the full player.
    - `MiniPlayerContent`: 80.dp persistent bottom bar with Coil artwork thumbnail, title, artist, live progress indicator line, play/pause button, skip next, and **horizontal swipe gestures** (~100dp threshold) to skip next/previous tracks with smooth `Animatable` spring physics.
    - `FullPlayerContent`: Full-screen player with top collapse/menu bar, 68% width artwork card with **vertical swipe-down gesture** (~150dp threshold) on album art to collapse, title/artist, live seek bar slider with timestamps, 5 control buttons (Shuffle, Prev, 72.dp Play/Pause FAB, Next, Repeat), media volume slider, queue info, and favorite toggle.
    - `AddToPlaylistDialog`: Real Room-backed dialog to save `currentSong` to existing playlists or create a new playlist on the fly.
    - `ArtistSongsDialog`: Dialog filtering and listing all tracks by the current song's artist with direct playback.
    - `SleepTimerDialog`: Interactive countdown selection (15/30/45/60 min) with cancel option.
    - `EqualizerDialog`: Built-in 5-band equalizer, Bass Boost, Virtualizer, and presets.
    - `AnimatedScaleIconButton`: Interactive button with press scale spring animation.
- **`ui/navigation/`**:
  - `Screen.kt`: Sealed class defining navigation destinations (`Home`, `Search`, `Library`, `Player`) with title and Material icons.
  - `HomeScreen.kt`: Modern YouTube/Spotify style streaming home screen without filter chips. Features:
    - Dynamic greeting header ("Good morning", "Good afternoon", "Good evening") with Rescan, Notifications, and Settings action buttons.
    - **Quick picks** horizontal carousel with 160dp cards, rounded corners, album artwork, dark gradient overlays, and live play/pause badges.
    - **Keep listening** section featuring 48dp compact cards with quick playback controls.
    - **All Songs (X)** section with `SwipeToDismissBox` (Swipe Left to Add to Playlist), solid `lerp` opaque background, and active play/pause indicators.
    - Shimmer loading state, empty state, and runtime storage/notification permission flow.
  - `SearchScreen.kt`: Real-time search UI over scanned local tracks (filtering title, artist, album) with genre quick tags, category browse cards, and **SwipeToDismissBox** gesture (Swipe Left to Add to Playlist).
  - `LibraryScreen.kt`: Real library browser with category tabs ("All Tracks", "Playlists", "Artists", "Albums"). Provides inline playlist creation, navigation to `PlaylistDetailScreen`, and **SwipeToDismissBox** gesture on tracks.
  - `PlaylistDetailScreen.kt`: Full detail view for playlists showing track list, total runtime, "Play All" button, **SwipeToDismissBox** gestures (Swipe Left to Add to Another Playlist, Swipe Right to Remove from Playlist), and playlist deletion.
- **`ui/theme/`**:
  - `Color.kt`, `Theme.kt`, `Type.kt`: Jetpack Compose Material 3 theme definitions with Material You dynamic color support.
- **`ui/viewmodel/`**:
  - `PlayerViewModel.kt`: Central playback ViewModel connecting to `MusicPlayerService` via Media3 `MediaController`. Manages playback state, queue, position progress tracking, Sleep Timer countdown, and Palette colors. Validates file paths proactively.
  - `PlaylistViewModel.kt`: Manages playlist state, creation, deletion, song addition, song removal, and reactive song flow for playlists.
  - `MusicScanViewModel.kt`: Media scanning ViewModel connecting to `MusicRepository`. Manages `ScanUiState` (`Idle`, `Loading`, `Success`, `Empty`, `PermissionDenied`, `Error`).
- **`utils/`**:
  - `ColorExtractor.kt`: Palette API utility extracting swatches (`darkVibrant`, `darkMuted`, `lightVibrant`, `lightMuted`, `vibrant`) from `Bitmap` on `Dispatchers.Default`, and loading software bitmaps from URI via Coil `ImageLoader(allowHardware = false)` on `Dispatchers.IO`.

---

### Actual Playback Data Flow (From UI Tap to Audio Output)

1. **User Tap / Gesture**: User taps a song in `HomeScreen`, `SearchScreen`, `LibraryScreen`, `PlaylistDetailScreen`, or swipes MiniPlayer / FullPlayer.
2. **Screen Callback**: Screen validates that `audio.filePath` is not empty and invokes `onAudioClick(audio, playlist)`. If `filePath` is blank, displays Toast `"This song is not available offline"`.
3. **Activity Host**: `MainActivity.kt`'s `MusicPlayerApp` receives the callback, re-verifies `filePath.isNotBlank()`, and calls `playerViewModel.play(audio, playlist)`.
4. **`PlayerViewModel`**:
   - Validates `song.filePath.isNotBlank()` and physical file existence.
   - Updates `_currentSong.value` and `_currentPlaylist.value`.
   - Dispatches a coroutine with cancellation to extract Palette colors from `song.albumArtUri` via `ColorExtractor.extractPaletteFromUri()` and updates `_albumPalette`.
   - Checks `mediaController`:
     - If `null`, stores `Pair(song, playlist)` into `pendingPlayRequest`, ensures `initializeController()` is running, and returns early.
     - When `controllerFuture` completes, `initializeController()` retrieves `mediaController`, configures `Player.Listener` (including `onMediaItemTransition`), and calls `play(song, playlist)`.
   - Builds `MediaMetadata` (title, artist, album, artworkUri) and `MediaItem` for the entire active playlist.
   - Calls `mediaController.setMediaItems(mediaItems, startIndex, rememberedPosition)`, `mediaController.prepare()`, and `mediaController.play()`.
   - Sets `_isPlaying.value = true` and launches `startProgressTracker()` coroutine loop (polls `mediaController.currentPosition` every 500ms).
5. **`MusicPlayerService` (Media3)**:
   - Receives command via `MediaSession`.
   - Attached `ExoPlayer` handles audio focus, prepares local media stream, and starts audio output.
   - `ExoPlayer` state changes fire `Player.Listener` callbacks (`onMediaItemTransition`, `onIsPlayingChanged`, `onPlaybackStateChanged`, `onPlayerError`) in `PlayerViewModel`.
6. **UI Recomposition**:
   - `NowPlayingBottomSheet` collects `currentSong`, `isPlaying`, `currentPosition`, `duration`, and `albumPalette`.
   - `AnimatedVisibility(visible = currentSong != null)` renders `MiniPlayerContent` (80.dp peek).
   - Tapping the mini player sets `isExpanded = true`, rendering `ModalBottomSheet` with `FullPlayerContent` and animated Palette gradient theming.

---

## 2. Key Classes and Actual Responsibilities

- **`MainActivity`**:
  - Initializes edge-to-edge Compose content with `AppTheme`.
  - Instantiates `PlayerViewModel by viewModels()`.
  - Sets up `Scaffold` with `BottomNavigationBar` (routes: `home`, `search`, `library`), `NavHost`, and floating `NowPlayingBottomSheet` aligned at `BottomCenter`.
- **`MusicPlayerService`**:
  - Extends `MediaSessionService`.
  - Creates and owns the background `ExoPlayer` instance configured with `AudioAttributes` (`handleAudioFocus = true`) and becoming-noisy handling.
  - Manages `MediaSession` lifecycle and service destruction (`stopSelf` on task removal when idle or paused).
- **`PlayerViewModel`**:
  - Connects to `MusicPlayerService` via `MediaController.Builder` asynchronously.
  - Holds and exposes playback state flows (`currentSong`, `isPlaying`, `currentPosition`, `duration`, `isShuffle`, `isRepeat`, `playbackError`, `currentPlaylist`, `albumPalette`, `sleepTimerMinutes`, `uiState`).
  - Implements playback actions: `play()`, `pause()`, `resume()`, `togglePlayPause()`, `seekTo()`, `skipToNext()`, `skipToPrevious()`, `toggleShuffle()`, `toggleRepeat()`, `toggleFavorite()`, `setSleepTimer()`, `cancelSleepTimer()`, `updatePalette()`.
  - Validates `song.filePath.isNotBlank()` before playback dispatch.
  - Tracks playback progress via polling coroutine (`delay(500)`).
- **`PlaylistViewModel`**:
  - Manages Room-persisted playlists via `PlaylistDao` and `PlaylistSongDao`.
  - Exposes `playlists: StateFlow<List<PlaylistEntity>>`.
  - Provides `createPlaylist()`, `deletePlaylist()`, `addSongToPlaylist()`, `removeSongFromPlaylist()`, and `getSongsForPlaylist()`.
- **`MusicScanViewModel`**:
  - Manages local storage scanning state via `ScanUiState` (`Idle`, `Loading`, `Success`, `Empty`, `PermissionDenied`, `Error`).
  - Auto-loads cached songs from `MusicRepository.getCachedAudio()` on initialization.
  - Exposes `scanMusic()`, `onPermissionGranted()`, and `onPermissionDenied()`.
- **`MediaScanner`**:
  - Queries Android `MediaStore.Audio.Media` using `ContentResolver` on `Dispatchers.IO`.
  - Filters music files (`IS_MUSIC != 0`, `DURATION >= 10000`).
  - Constructs `AudioEntity` objects with metadata and content URI for album art.
- **`MusicRepository`**:
  - Coordinates Room database caching (`AudioDao`) and local device scanning (`MediaScanner`).
  - Clears and rewrites cached audio entities upon successful scan.
- **`AppDatabase` / `AudioDao` / `PlaylistDao` / `PlaylistSongDao`**:
  - `AppDatabase`: Room database instance (`version = 2`, `fallbackToDestructiveMigration`).
  - `AudioDao`: Interface for audio track caching.
  - `PlaylistDao`: Interface for playlist CRUD queries.
  - `PlaylistSongDao`: Interface for playlist track cross-reference queries.

---

## 3. State Management Approach

### Exposed StateFlows:
- **`PlayerViewModel`**: `currentSong`, `isPlaying`, `currentPosition`, `duration`, `isShuffle`, `isRepeat`, `playbackError`, `currentPlaylist`, `albumPalette`, `sleepTimerMinutes`, `uiState`.
- **`PlaylistViewModel`**: `playlists: StateFlow<List<PlaylistEntity>>`.
- **`MusicScanViewModel`**: `scanState: StateFlow<ScanUiState>`.

---

## 4. Known Limitations / Incomplete Parts

- **Online Streaming (YouTube / Cloud API)**: Search categories and online mock queries are set up as UI placeholders; online streaming is not yet connected to a remote audio streaming backend.
- **Network Dependencies**: `Retrofit` and `OkHttp` are included in `build.gradle.kts` / `libs.versions.toml`, but no network API interfaces or HTTP services are implemented.
- **Header Actions in `HomeScreen`**: Notifications and Settings `IconButton` components have empty click lambdas (`{}`).

---

## 5. Dependencies Actually in Use

Verified from `gradle/libs.versions.toml` and `app/build.gradle.kts`:

| Dependency / Plugin | Version | Artifact Identifier |
|---|---|---|
| Android Gradle Plugin | `8.5.2` | `com.android.application` |
| Kotlin | `2.0.21` | `org.jetbrains.kotlin.android`, `org.jetbrains.kotlin.plugin.compose` |
| KSP (Kotlin Symbol Processing) | `2.0.21-1.0.28` | `com.google.devtools.ksp` |
| AndroidX Core KTX | `1.13.1` | `androidx.core:core-ktx` |
| AndroidX Lifecycle | `2.8.7` | `lifecycle-runtime-ktx`, `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose` |
| AndroidX Activity Compose | `1.9.3` | `androidx.activity:activity-compose` |
| Jetpack Compose BOM | `2024.10.00` | `androidx.compose:compose-bom` (`ui`, `ui-graphics`, `ui-tooling-preview`, `ui-tooling`, `material3`, `material-icons-extended`) |
| Navigation Compose | `2.8.3` | `androidx.navigation:navigation-compose` |
| Media3 (ExoPlayer & Session) | `1.4.0` | `media3-exoplayer`, `media3-session`, `media3-ui`, `media3-common` |
| Room | `2.6.1` | `room-runtime`, `room-ktx`, `room-compiler` |
| Retrofit | `2.11.0` | `retrofit`, `converter-gson` |
| OkHttp | `4.12.0` | `okhttp`, `logging-interceptor` |
| Kotlinx Coroutines | `1.8.0` | `kotlinx-coroutines-core`, `kotlinx-coroutines-android` |
| Coil | `2.7.0` | `io.coil-kt:coil-compose` |
| Palette API | `1.0.0` | `androidx.palette:palette-ktx` |
| JUnit / AndroidX Test / Espresso | `4.13.2` / `1.2.1` / `3.6.1` | `junit`, `androidx.test.ext:junit`, `androidx.test.espresso:espresso-core` |

---

### 2. Comprehensive Settings Page & DataStore Integration
- **`data/preferences/SettingsDataStore.kt`**: Persistent key-value storage using Jetpack DataStore Preferences managing crossfade duration, gapless playback, playback speed, volume normalization, equalizer preset, bass boost, virtualizer, theme mode (System/Light/Dark), dynamic theming, accent colors, startup library scan, excluded folders, queue settings, notification style, lock screen controls, and headset auto-play/pause.
- **`ui/viewmodel/SettingsViewModel.kt` & `NotificationSettingsViewModel.kt`**: `AndroidViewModel`s exposing `StateFlow<UserSettings>` and updater methods.
- **`ui/screens/SettingsScreen.kt`, `NotificationSettingsScreen.kt`, `NotificationScreen.kt` & `StatsScreen.kt`**: Material 3 settings, stats, and status screens with Scaffold TopAppBar, categorized section cards, permission status checks, switches, dropdowns, listening time, bar charts, artist breakdown percentages, and test notification dispatch.
- **`ui/viewmodel/StatsViewModel.kt` & `PlayerViewModel.kt`**: Real-time stats aggregation across time intervals ("Continuous", "1 week", "1 month", "3 months") and automatic play count/listening time increments upon track playback.
- **Room Database Migration (v3)**: Added `plays`, `lastPlayed`, and `totalTime` columns to `audio` table with `MIGRATION_2_3`.
- **Navigation Integration**: Linked via `Screen.Settings`, `Screen.NotificationSettings`, `Screen.Notifications`, and `Screen.Stats` routes, header settings and stats icons in `HomeScreen`, and NavHost in `MainActivity`.

---

## 6. Recent Fixes & Features

### 1. Gesture Support Across Music Player
- **Mini Player Horizontal Swipe**: Added horizontal drag gestures with ~100dp threshold to skip tracks (Swipe Left $\rightarrow$ Next, Swipe Right $\rightarrow$ Previous) with spring physics and non-blocking tap-to-expand.
- **Full Player Swipe-Down to Collapse**: Added vertical drag gestures with ~150dp threshold on Album Art card to collapse player smoothly without conflicting with bottom sheet content.
- **Song List Swipe Actions**: Integrated `SwipeToDismissBox` across `HomeScreen`, `SearchScreen`, `LibraryScreen`, and `PlaylistDetailScreen`:
  - Swipe Left (All Screens): Opens "Add to Playlist" dialog.
  - Swipe Right (PlaylistDetailScreen): Removes track from the playlist.
