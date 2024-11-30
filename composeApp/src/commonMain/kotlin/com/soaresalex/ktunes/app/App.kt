package com.soaresalex.ktunes.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.*
import com.soaresalex.ktunes.plugins.AudioServicePlugin
import com.soaresalex.ktunes.plugins.KTunesPluginManager
import com.soaresalex.ktunes.theme.AppTheme
import com.soaresalex.ktunes.theme.ErrorContainerLight
import com.soaresalex.ktunes.theme.LocalThemeIsDark
import com.soaresalex.ktunes.ui.screens.PlaybackScreen
import com.soaresalex.ktunes.viewmodels.AudioPlaybackService
import com.soaresalex.ktunes.viewmodels.AudioTrack
import com.soaresalex.ktunes.viewmodels.PlayerState
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import org.jetbrains.skiko.currentSystemTheme
import org.koin.compose.getKoin
import java.awt.Button


@Composable
fun ThemeToggleButton() {
    var isDark by LocalThemeIsDark.current

    IconButton(onClick = { isDark = !isDark }) {
        Icon(
            imageVector = when {
                isDark -> FeatherIcons.Sun
                else -> FeatherIcons.Moon
            }, contentDescription = "Toggle Theme"
        )
    }
}


object LibraryTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val title = "Library"
            val icon = rememberVectorPainter(FeatherIcons.List)

            return remember {
                TabOptions(
                    index = 1u, title = title, icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        LibraryScreen()
    }
}

object PluginManagementTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val title = "Plugins"
            val icon = rememberVectorPainter(FeatherIcons.Zap)

            return remember {
                TabOptions(
                    index = 2u, title = title, icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        PluginManagementScreen()
    }
}

object PlaybackTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val title = "Playback"
            val icon = rememberVectorPainter(FeatherIcons.Play)

            return remember {
                TabOptions(
                    index = 0u, title = title, icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        PlaybackScreen(
            viewModel = getKoin().get(),
        )
    }
}

@Composable
fun LibraryScreen() {
    val pluginManager: KTunesPluginManager = getKoin().get()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Music Library",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Placeholder for library content
        // In a real implementation, you would call methods on the plugins to retrieve library contents

        // TODO: Implement actual library retrieval and display
        // Example could include:
        // - List of tracks
        // - Grid of albums
        // - Filtering and searching capabilities
    }
}

@Composable
fun RowScope.TabItem(tab: Tab) {
    val navigator = LocalTabNavigator.current

    NavigationBarItem(selected = navigator.current == tab, onClick = { navigator.current = tab }, label = {
        Text(
            text = tab.options.title
        )
    }, icon = {
        tab.options.icon?.let {
            Icon(
                painter = it,
                contentDescription = tab.options.title,
            )
        }
    })
}

@Composable
fun App() {
    AppTheme {
        TabNavigator(LibraryTab) {
            Scaffold(topBar = {
                NavigationBar() {
                    TabItem(PlaybackTab)
                    TabItem(LibraryTab)
                    TabItem(PluginManagementTab)
                }
            }, content = { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    CurrentTab()
                }
            }, bottomBar = {

            })
        }
    }
}

// Additional UI components that might need to be implemented
@Composable
fun PluginListItem(
    plugin: AudioServicePlugin,
    onConfigureClick: () -> Unit = {},
    onUninstallClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = plugin.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = plugin.version,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row {
                IconButton(onClick = onConfigureClick) {
                    Icon(
                        imageVector = FeatherIcons.Edit2,
                        contentDescription = "Configure ${plugin.name}"
                    )
                }

                IconButton(onClick = onUninstallClick) {
                    Icon(
                        imageVector = FeatherIcons.Trash,
                        contentDescription = "Uninstall ${plugin.name}"
                    )
                }
            }
        }
    }
}

@Composable
fun PluginConfigDialog(
    plugin: AudioServicePlugin, onDismiss: () -> Unit, onConfigSave: (Map<String, String>) -> Unit
) {
    val configState = remember { mutableStateMapOf<String, String>() }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Configure ${plugin.name}") }, text = {
        Column {
            plugin.getConfigs().forEach { option ->
                TextField(
                    value = configState.getOrDefault(option.key, ""),
                    onValueChange = { configState[option.key] = it },
                    label = { Text(option.key) })
            }
        }
    }, confirmButton = {
        FilledTonalButton(
            onClick = {
                onConfigSave(configState.toMap())
                onDismiss()
            }) {
            Text("Save")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}

@Composable
fun PluginUninstallDialog(
    plugin: AudioServicePlugin, onDismiss: () -> Unit, onUninstall: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Uninstall ${plugin.name}") }, text = {
        Text("Are you sure you want to uninstall ${plugin.name}?")
    }, confirmButton = {
        ElevatedButton(
            colors = ButtonDefaults.elevatedButtonColors(
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
            onClick = {
                onUninstall()
                onDismiss()
            }) {
            Text("Uninstall")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}

class MockAudioServicePlugin : AudioServicePlugin {
    override val id: String = "mock"
    override val name: String = "Mock Plugin"
    override val version: String = "1.0.0"

    override fun getConfigs(): Map<String, String> = mapOf("Mock Config" to "Value")

    override fun applyConfigs(configs: Map<String, String>) {
        // Apply configuration changes
    }

    override fun createAudioPlaybackService(): AudioPlaybackService {
        return object : AudioPlaybackService {
            override suspend fun getCurrentPlayerState(): PlayerState? = null


            override suspend fun play() {

            }

            override suspend fun pause() {

            }

            override suspend fun nextTrack() {

            }

            override suspend fun previousTrack() {

            }

            override suspend fun seekTo(positionMs: Long) {

            }

            override val currentTrack: AudioTrack?
                get() = null
            override val isPlaying: Boolean
                get() = false
            override val currentPosition: Long
                get() = 0
        }
    }
}

@Composable
fun PluginManagementScreen() {
    val pluginManager: KTunesPluginManager = getKoin().get()
    val availablePlugins = listOf(MockAudioServicePlugin())
    var selectedPlugin by remember { mutableStateOf<AudioServicePlugin?>(null) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showUninstallDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Plugin Management",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(availablePlugins.size) { idx ->
                PluginListItem(
                    plugin = availablePlugins[idx],
                    onConfigureClick = {
                        selectedPlugin = availablePlugins[idx]
                        showConfigDialog = true
                    },
                    onUninstallClick = {
                        selectedPlugin = availablePlugins[idx]
                        showUninstallDialog = true
                    }
                )
            }
        }

        Button(
            onClick = {}, modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Discover Plugins")
        }

        selectedPlugin?.let {
            when {
                showConfigDialog -> {
                    PluginConfigDialog(
                        plugin = it,
                        onDismiss = { showConfigDialog = false },
                        onConfigSave = { config ->
                            it.applyConfigs(config)
                            showConfigDialog = false
                        }
                    )
                }

                showUninstallDialog -> {
                    PluginUninstallDialog(
                        plugin = it,
                        onDismiss = { showUninstallDialog = false },
                        onUninstall = {
                            // TODO: Actually uninstall plugin
                            showUninstallDialog = false
                        }
                    )
                }
            }
        }
    }
}