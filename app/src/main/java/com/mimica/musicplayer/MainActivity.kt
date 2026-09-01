package com.mimica.musicplayer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mimica.musicplayer.data.local.AudioEntity
import com.mimica.musicplayer.ui.components.NowPlayingBottomSheet
import com.mimica.musicplayer.ui.navigation.Screen
import com.mimica.musicplayer.ui.screens.HomeScreen
import com.mimica.musicplayer.ui.screens.LibraryScreen
import com.mimica.musicplayer.ui.screens.SearchScreen
import com.mimica.musicplayer.ui.theme.AppTheme
import com.mimica.musicplayer.ui.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                MusicPlayerApp(
                    navController = navController,
                    playerViewModel = playerViewModel
                )
            }
        }
    }
}

@Composable
fun MusicPlayerApp(
    navController: NavHostController = rememberNavController(),
    playerViewModel: PlayerViewModel
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        // Base content with NavHost and Bottom Navigation
        Scaffold(
            topBar = {},
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Screens Navigation Host
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            playerViewModel = playerViewModel,
                            onAudioClick = { audio, playlist ->
                                if (audio.filePath.isBlank()) {
                                    Toast.makeText(context, "This song is not available offline", Toast.LENGTH_SHORT).show()
                                } else {
                                    playerViewModel.play(audio, playlist)
                                }
                            }
                        )
                    }
                    composable(Screen.Search.route) {
                        SearchScreen(
                            playerViewModel = playerViewModel,
                            onAudioClick = { audio, playlist ->
                                if (audio.filePath.isBlank()) {
                                    Toast.makeText(context, "This song is not available offline", Toast.LENGTH_SHORT).show()
                                } else {
                                    playerViewModel.play(audio, playlist)
                                }
                            }
                        )
                    }
                    composable(Screen.Library.route) {
                        LibraryScreen(
                            playerViewModel = playerViewModel,
                            onAudioClick = { audio, playlist ->
                                if (audio.filePath.isBlank()) {
                                    Toast.makeText(context, "This song is not available offline", Toast.LENGTH_SHORT).show()
                                } else {
                                    playerViewModel.play(audio, playlist)
                                }
                            }
                        )
                    }
                }

                // Material 3 NowPlayingBottomSheet floating above the BottomNavigationBar
                NowPlayingBottomSheet(
                    playerViewModel = playerViewModel,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Screen.bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = {
                    if (currentRoute != screen.route) {
                        onNavigate(screen.route)
                    }
                }
            )
        }
    }
}
