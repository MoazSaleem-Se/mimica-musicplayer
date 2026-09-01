package com.mimica.musicplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mimica.musicplayer.ui.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.collectAsState()

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderPath by remember { mutableStateOf("") }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Notification Settings Entry Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNotificationsClick() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Notifications",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Permission, lock screen, style & headset controls",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Notifications Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Section 1: Playback
            item {
                SettingsSectionCard(
                    title = "Playback",
                    icon = Icons.Default.PlayCircle
                ) {
                    // Crossfade
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Crossfade",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (settings.crossfadeDuration > 0f) "${settings.crossfadeDuration.toInt()} sec" else "Off",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Slider(
                            value = settings.crossfadeDuration,
                            onValueChange = { settingsViewModel.setCrossfadeDuration(it.roundToInt().toFloat()) },
                            valueRange = 0f..12f,
                            steps = 11,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Gapless Playback
                    ListItem(
                        headlineContent = { Text("Gapless Playback") },
                        supportingContent = { Text("Seamless transition between tracks") },
                        leadingContent = {
                            Icon(Icons.Default.AudioFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = settings.gaplessPlayback,
                                onCheckedChange = { settingsViewModel.setGaplessPlayback(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    // Playback Speed
                    var speedDropdownExpanded by remember { mutableStateOf(false) }
                    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

                    ListItem(
                        headlineContent = { Text("Playback Speed") },
                        supportingContent = { Text("Current: ${settings.playbackSpeed}x") },
                        leadingContent = {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Box {
                                OutlinedButton(onClick = { speedDropdownExpanded = true }) {
                                    Text("${settings.playbackSpeed}x")
                                }
                                DropdownMenu(
                                    expanded = speedDropdownExpanded,
                                    onDismissRequest = { speedDropdownExpanded = false }
                                ) {
                                    speedOptions.forEach { speed ->
                                        DropdownMenuItem(
                                            text = { Text("${speed}x") },
                                            onClick = {
                                                settingsViewModel.setPlaybackSpeed(speed)
                                                speedDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    // Volume Normalization
                    ListItem(
                        headlineContent = { Text("Volume Normalization") },
                        supportingContent = { Text("Balance loudness across different songs") },
                        leadingContent = {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = settings.volumeNormalization,
                                onCheckedChange = { settingsViewModel.setVolumeNormalization(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // Section 2: Audio
            item {
                SettingsSectionCard(
                    title = "Audio",
                    icon = Icons.Default.Tune
                ) {
                    // Equalizer Preset
                    var eqDropdownExpanded by remember { mutableStateOf(false) }
                    val eqPresets = listOf("Normal", "Classical", "Dance", "Flat", "Folk", "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock")

                    ListItem(
                        headlineContent = { Text("Equalizer Preset") },
                        supportingContent = { Text("Preset: ${settings.equalizerPreset}") },
                        leadingContent = {
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Box {
                                OutlinedButton(onClick = { eqDropdownExpanded = true }) {
                                    Text(settings.equalizerPreset)
                                }
                                DropdownMenu(
                                    expanded = eqDropdownExpanded,
                                    onDismissRequest = { eqDropdownExpanded = false }
                                ) {
                                    eqPresets.forEach { preset ->
                                        DropdownMenuItem(
                                            text = { Text(preset) },
                                            onClick = {
                                                settingsViewModel.setEqualizerPreset(preset)
                                                eqDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    // Bass Boost
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Bass Boost",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${settings.bassBoostLevel}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Slider(
                            value = settings.bassBoostLevel.toFloat(),
                            onValueChange = { settingsViewModel.setBassBoostLevel(it.roundToInt()) },
                            valueRange = 0f..100f,
                            steps = 9,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Virtualizer
                    ListItem(
                        headlineContent = { Text("Virtualizer") },
                        supportingContent = { Text("3D spatial surround sound effect") },
                        leadingContent = {
                            Icon(Icons.Default.SurroundSound, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = settings.virtualizerEnabled,
                                onCheckedChange = { settingsViewModel.setVirtualizerEnabled(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // Section 3: Appearance
            item {
                SettingsSectionCard(
                    title = "Appearance",
                    icon = Icons.Default.Palette
                ) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    val themes = listOf(
                        "SYSTEM" to "System Default",
                        "LIGHT" to "Light Theme",
                        "DARK" to "Dark Theme"
                    )

                    themes.forEach { (modeKey, modeTitle) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { settingsViewModel.setThemeMode(modeKey) }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.themeMode == modeKey,
                                onClick = { settingsViewModel.setThemeMode(modeKey) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = modeTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic Theming
                    ListItem(
                        headlineContent = { Text("Dynamic Theming") },
                        supportingContent = { Text("Extract palette colors dynamically from album art") },
                        leadingContent = {
                            Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = settings.dynamicTheming,
                                onCheckedChange = { settingsViewModel.setDynamicTheming(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    // Accent Colors
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "Accent Color",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val accentColors = listOf(
                            0xFF6750A4 to "Purple",
                            0xFF3F51B5 to "Indigo",
                            0xFF006C5F to "Teal",
                            0xFF2E7D32 to "Green",
                            0xFFE65100 to "Orange",
                            0xFFC2185B to "Pink",
                            0xFF0288D1 to "Blue"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            accentColors.forEach { (colorHex, _) ->
                                val isSelected = settings.accentColorHex == colorHex
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorHex))
                                        .clickable { settingsViewModel.setAccentColorHex(colorHex) }
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Library
            item {
                SettingsSectionCard(
                    title = "Library",
                    icon = Icons.Default.LibraryMusic
                ) {
                    // Scan on Startup
                    ListItem(
                        headlineContent = { Text("Scan on Startup") },
                        supportingContent = { Text("Auto-scan local storage for new music files when app opens") },
                        leadingContent = {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked = settings.scanOnStartup,
                                onCheckedChange = { settingsViewModel.setScanOnStartup(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    // Excluded Folders
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FolderOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Excluded Folders (${settings.excludedFolders.size})",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            TextButton(onClick = { showAddFolderDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                        }

                        if (settings.excludedFolders.isEmpty()) {
                            Text(
                                text = "No folders excluded. All audio folders are scanned.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                settings.excludedFolders.forEach { folderPath ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = folderPath,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { settingsViewModel.removeExcludedFolder(folderPath) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 5: Playback Queue
            item {
                SettingsSectionCard(
                    title = "Playback Queue",
                    icon = Icons.Default.QueueMusic
                ) {
                    // Remember Position
                    ListItem(
                        headlineContent = { Text("Remember Playback Position") },
                        supportingContent = { Text("Resume songs from where you left off") },
                        trailingContent = {
                            Switch(
                                checked = settings.rememberPosition,
                                onCheckedChange = { settingsViewModel.setRememberPosition(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    // Auto-play Next
                    ListItem(
                        headlineContent = { Text("Auto-play Next") },
                        supportingContent = { Text("Automatically play subsequent tracks in queue") },
                        trailingContent = {
                            Switch(
                                checked = settings.autoPlayNext,
                                onCheckedChange = { settingsViewModel.setAutoPlayNext(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    // Shuffle on Play
                    ListItem(
                        headlineContent = { Text("Shuffle on Play") },
                        supportingContent = { Text("Randomize queue order when launching a playlist") },
                        trailingContent = {
                            Switch(
                                checked = settings.shuffleOnPlay,
                                onCheckedChange = { settingsViewModel.setShuffleOnPlay(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // Section 6: About
            item {
                SettingsSectionCard(
                    title = "About",
                    icon = Icons.Default.Info
                ) {
                    ListItem(
                        headlineContent = { Text("Mimica Music Player") },
                        supportingContent = { Text("Version 1.0.0 • Modern Material 3 & Jetpack Media3") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    ListItem(
                        headlineContent = { Text("Developer") },
                        supportingContent = { Text("Mimica Engineering Team") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    ListItem(
                        headlineContent = { Text("Privacy Policy") },
                        supportingContent = { Text("Read privacy terms & local media access policy") },
                        leadingContent = {
                            Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.clickable { showPrivacyDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }

    // Add Excluded Folder Dialog
    if (showAddFolderDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddFolderDialog = false
                newFolderPath = ""
            },
            title = { Text("Exclude Folder") },
            text = {
                Column {
                    Text(
                        text = "Enter the relative or absolute folder path to exclude from library scanning (e.g., /sdcard/Ringtones, /WhatsApp/Media):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newFolderPath,
                        onValueChange = { newFolderPath = it },
                        placeholder = { Text("/storage/emulated/0/Podcasts") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderPath.isNotBlank()) {
                            settingsViewModel.addExcludedFolder(newFolderPath)
                            showAddFolderDialog = false
                            newFolderPath = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddFolderDialog = false
                        newFolderPath = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "Mimica Music Player values your privacy:\n\n" +
                                "• Local Storage: Media permissions are strictly used to scan and play audio files stored on your device.\n" +
                                "• No Tracking: No telemetry, personal data, or listening habits are collected or shared.\n" +
                                "• Offline First: Audio playback and metadata processing occur entirely on-device without internet transmission.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}
