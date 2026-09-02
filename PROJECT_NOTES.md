# Project Documentation - Mimica Music Player

**Architecture & Implementation Reference Guide**

---

## 1. Architecture Overview

```
com.mimica.musicplayer/
├── data/
│   ├── local/                     # Room Database & DAO Layer (Schema Version 4)
│   │   ├── AppDatabase.kt         # Room database singleton with migrations (MIGRATION_2_3, MIGRATION_3_4)
│   │   ├── AudioEntity.kt         # Scanned track entity with custom metadata & format fields
│   │   ├── AudioDao.kt            # Room DAO for local audio tracks & custom metadata
│   │   ├── PlaylistEntity.kt      # Playlist entity with custom cover artwork URI
│   │   ├── PlaylistDao.kt         # Room DAO for playlist CRUD operations
│   │   ├── PlaylistSongEntity.kt  # Join table for playlist-track associations
│   │   └── PlaylistSongDao.kt     # Room DAO for playlist track queries
│   ├── preferences/
│   │   └── SettingsDataStore.kt   # Persistent Jetpack DataStore preferences for all app settings
│   ├── repository/
│   │   └── MusicRepository.kt     # Coordinates Room caching, scanner execution, & excluded folder filtering
│   └── scanner/
│       └── MediaScanner.kt        # Two-tier MediaStore scanner (Path-based + Content-based dedup)
├── playback/
│   └── MusicPlayerService.kt      # Media3 MediaSessionService with ExoPlayer & AudioEffects pipeline
├── ui/
│   ├── components/
│   │   ├── AnimatedScaleIconButton.kt # Spring-scale interactive button
│   │   ├── EditMetadataDialog.kt  # Dialogs for editing song metadata & playlist artwork
│   │   └── NowPlayingBottomSheet.kt # Mini player & modular Full Player composables
│   ├── navigation/
│   │   └── Screen.kt              # Sealed class defining app routes & bottom bar items
│   ├── screens/
│   │   ├── HomeScreen.kt          # Greeting header, Quick Picks, Keep Listening, All Songs
│   │   ├── SearchScreen.kt        # Real-time search with format badges & metadata editing
│   │   ├── LibraryScreen.kt       # Tabs (Tracks, Playlists, Artists, Albums) & custom artwork
│   │   ├── PlaylistDetailScreen.kt # Playlist details, tracklist, and custom cover editor
│   │   ├── StatsScreen.kt         # Comprehensive listening analytics & charts
│   │   ├── SettingsScreen.kt      # Full user preferences & audio effect configuration
│   │   ├── NotificationSettingsScreen.kt # Notification permissions & style options
│   │   └── NotificationScreen.kt  # Notification preview & test notification trigger
│   ├── theme/
│   │   ├── Color.kt               # Theme palette & color swatches
│   │   ├── Theme.kt               # Material 3 dark/light theme wrapper with dynamic colors
│   │   └── Type.kt                # Typography styles
│   └── viewmodel/
│       ├── PlayerViewModel.kt     # MediaController connection, playback state, queue, & audio effects
│       ├── PlaylistViewModel.kt   # Playlist CRUD and song association management
│       ├── MusicScanViewModel.kt  # Media scanning state machine
│       ├── SettingsViewModel.kt   # DataStore settings bridge
│       ├── StatsViewModel.kt      # Listening statistics aggregation by time window
│       └── NotificationSettingsViewModel.kt # Notification settings bridge
└── utils/
    └── ColorExtractor.kt          # Palette API background swatch extractor
```

---

## 2. Core Subsystems

### 2.1 Media Scanning & Two-Tier Deduplication (`MediaScanner.kt`)
- **Query Projection**: Queries `_ID`, `TITLE`, `ARTIST`, `ALBUM`, `DURATION`, `_DATA`, `ALBUM_ID`, `MIME_TYPE`, `_SIZE` from `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` / `VOLUME_EXTERNAL`.
- **Pre-Filtering**: Filters out non-music items (`IS_MUSIC != 0`) and files shorter than 10 seconds (`DURATION >= 10000`). Checks against active excluded folders.
- **Pass 1 (Path-Based Deduplication)**: Collapses identical file paths (`filePath.lowercase()`), retaining the entry with the highest MediaStore ID.
- **Pass 2 (Content-Based Deduplication)**:
  - Groups tracks by normalized key: `"${title.lowercase().trim()}|${artist.lowercase().trim()}|$duration|$size"`.
  - Disambiguates using exact track duration and byte-exact file size.
  - Employs priority scoring (`selectPreferredEntry`):
    1. Canonical storage paths (`/storage/emulated/...`) preferred over legacy mount aliases (`/sdcard/`, `/mnt/...`).
    2. Dedicated audio directories (`/Music/`, `/Audio/`) preferred over generic storage (`/Documents/`, `/Download/`).
    3. Highest MediaStore ID used as final tie-breaker.
- **Format Extraction**: Resolves clean format badges (`MP3`, `FLAC`, `WAV`, `OGG`, `M4A`, `AAC`, `OPUS`, `AMR`, `MIDI`) via MIME type and file extension fallback.

---

### 2.2 Room Database Layer (Version 4)
- **`AudioEntity`**:
  - `id: Long` (Primary Key)
  - `title: String`, `artist: String`, `album: String`, `duration: Long`, `filePath: String`
  - `albumArtUri: String?`, `albumId: Long`
  - `plays: Int`, `lastPlayed: Long`, `totalTime: Long` (Added in v3 for Stats)
  - `customArtworkUri: String?`, `customArtistName: String?`, `fileFormat: String` (Added in v4 for Metadata Editing)
  - Computed properties: `displayArtist`, `displayArtworkUri`, `durationFormatted`.
- **`PlaylistEntity`**:
  - `id: Long` (Primary Key), `name: String`, `createdAt: Long`, `songCount: Int`
  - `customArtworkUri: String?` (Added in v4)
  - Computed property: `displayArtworkUri`.
- **`PlaylistSongEntity`**:
  - `playlistId: Long`, `songId: Long`, `position: Int` (Composite Primary Key).
- **Metadata Persistence**: Scans preserve custom user-edited metadata (`customArtistName`, `customArtworkUri`) by querying existing values prior to database cache replacement.

---

### 2.3 Audio Playback & Media3 Integration
- **`MusicPlayerService`**:
  - Extends Media3 `MediaSessionService`.
  - Configures `ExoPlayer` with `AudioAttributes(USAGE_MEDIA, AUDIO_CONTENT_TYPE_MUSIC)`, automatic audio focus handling, and noisy intent handling.
  - Dynamically attaches and manages Android `AudioEffect` instances (`Equalizer`, `BassBoost`, `Virtualizer`, `LoudnessEnhancer`) bound to the active `audioSessionId`.
  - Registers `HeadsetReceiver` for wired and Bluetooth connection/disconnection auto-play and auto-pause.
- **`PlayerViewModel`**:
  - Asynchronously connects to `MusicPlayerService` via `MediaController.Builder`.
  - Automatically syncs existing player state upon Activity re-creation (restores active track, playback status, and playlist queue).
  - Drives live progress polling (every 500ms), crossfade volume ramp approximations, playback speed adjustments, sleep timer countdown, and Palette color extraction.
  - Increments play counts and listening duration on song transitions for stats tracking.

---

### 2.4 UI Architecture & Jetpack Compose Components
- **`NowPlayingBottomSheet.kt`**:
  - **Modular Full Player Composables**: Decomposed into `FullPlayerTopBar`, `FullPlayerAlbumArt`, `FullPlayerSongInfo`, `FullPlayerSeekBar`, `FullPlayerControls`, and `FullPlayerVolumeAndUpNext` to adhere strictly to Dalvik/ART 256-register limits.
  - **Dominant-Axis Gesture Handling**: Album Art card cleanly handles both horizontal drag gestures (swipe left for Next, swipe right for Previous) and vertical drag down (swipe down to collapse).
  - **Mini Player**: Floating bottom bar with 80dp height, persistent Coil thumbnail, live progress bar, play/pause toggle, skip next, and horizontal swipe gestures.
  - **Adaptive Palette Theming**: Smoothly animates gradient backgrounds and text colors to match album artwork tones.
- **`EditMetadataDialog.kt`**:
  - Allows editing song title/artist and picking custom artwork from gallery.
  - Safely copies picked images to internal app storage (`context.filesDir/artwork_<id>_<timestamp>.jpg`) to prevent URI permission revocation across device reboots.
- **`HomeScreen.kt`**:
  - Greeting header with time-based greetings, dynamic Rescan button, Stats icon, and Settings icon.
  - **Quick Picks Carousel**: 180dp cards with `rememberSnapFlingBehavior` and indexed staggered animations.
  - **All Songs List**: Formatted with solid, pre-blended `lerp` surface backgrounds, format badges, metadata edit pencils, and `SwipeToDismissBox` (Swipe Left to Add to Playlist).
- **`StatsScreen.kt`**:
  - Visual listening analytics across selectable time filters ("Continuous", "1 week", "1 month", "3 months").
  - Displays total listening time, total plays, unique tracks, unique artists, favorite artist, favorite track, listening pattern bar charts, and top track rankings.

---

## 3. Technology Stack & Dependencies

| Component | Library / Version | Purpose |
|---|---|---|
| Language | Kotlin `2.0.21` | Application language |
| UI Framework | Jetpack Compose BOM `2024.10.00` | Declarative UI, Material 3, Animations |
| Media Framework | AndroidX Media3 `1.4.0` | ExoPlayer, MediaSession, MediaController |
| Local Database | Room `2.6.1` + KSP | SQLite ORM, Migrations, Reactive DAO flows |
| Preferences | Jetpack DataStore Preferences `1.1.1` | Asynchronous key-value settings storage |
| Image Loading | Coil `2.7.0` | Asynchronous image loading & hardware bitmap bypass |
| Palette Extraction | AndroidX Palette `1.0.0` | Dynamic color extraction from artwork |
| Navigation | Navigation Compose `2.8.3` | Single-activity screen navigation |

---

## 4. Key Workflows

### 4.1 Playback Workflow
1. User taps a track on any screen.
2. `MainActivity` forwards request to `PlayerViewModel.play(song, playlist)`.
3. `PlayerViewModel` verifies physical file existence and builds Media3 `MediaItem` list.
4. `MediaController` issues `setMediaItems`, `prepare`, and `play` to `MusicPlayerService`.
5. Service handles audio focus, routes through active `AudioEffect`s, and begins ExoPlayer output.
6. `PlayerViewModel` starts progress tracking and background Palette color extraction.
7. Mini player animates in; expanding opens full-screen player with adaptive gradient.

### 4.2 Metadata Editing Workflow
1. User clicks the Edit (Pencil) icon on a track row or selects "Edit Song Details" in the full player menu.
2. `EditSongMetadataDialog` opens with current artist name and album art preview.
3. User modifies artist name and/or picks a new image via `rememberLauncherForActivityResult(PickVisualMedia)`.
4. The picked image is copied to app internal files (`/files/artwork_<id>_<timestamp>.jpg`).
5. `PlayerViewModel.updateSongCustomMetadata()` persists custom attributes to Room DB.
6. The UI reactively updates across all screens (`HomeScreen`, `SearchScreen`, `LibraryScreen`, `NowPlayingBottomSheet`).
