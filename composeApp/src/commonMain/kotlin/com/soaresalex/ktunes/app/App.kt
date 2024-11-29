package com.soaresalex.ktunes.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import co.touchlab.kermit.Logger
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.russhwolf.settings.Settings
import com.soaresalex.ktunes.data.*
import com.soaresalex.ktunes.plugins.AudioSourcePlugin
import com.soaresalex.ktunes.plugins.ConfigOptionType
import com.soaresalex.ktunes.plugins.PluginManager
import com.soaresalex.ktunes.theme.AppTheme
import com.soaresalex.ktunes.theme.LocalThemeIsDark
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class AuthenticationFlow(
    private val spotifyAuth: SpotifyAuthClient,
    private val authRedirectServer: AuthRedirectServer
) : KoinComponent {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Checking)
    val authState = _authState.asStateFlow()

    init {
        // Check authentication status immediately
        checkAuthenticationStatus()

        // Set up reactive authentication callback
        authRedirectServer.onAuthenticationResult = { result ->
            when (result) {
                is SpotifyAuthClient.AuthResult.Success -> {
                    _authState.update { AuthState.Authenticated }
                }

                is SpotifyAuthClient.AuthResult.Failure -> {
                    _authState.update { AuthState.Error(result.errorMessage) }
                }
            }

            authRedirectServer.stopListening()
        }
    }

    private fun checkAuthenticationStatus() {
        _authState.update {
            if (spotifyAuth.isAuthenticated()) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    suspend fun initiateAuthentication() {
        authRedirectServer.startListening()
        val authUrl = spotifyAuth.generateAuthorizationUrl()
        _authState.update { AuthState.AuthorizationInProgress(authUrl) }
    }

    fun resetToUnauthenticated() {
        _authState.update { AuthState.Unauthenticated }
    }

    sealed class AuthState {
        object Checking : AuthState() // New state to indicate initial authentication check
        object Unauthenticated : AuthState()
        data class AuthorizationInProgress(val authUrl: String) : AuthState()
        object Authenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

data class SpotifyTrackViewModel(
    val name: String,
    val artists: List<String>,
    val albumImageUrl: String?,
    val duration: Int,
    val isExplicit: Boolean
) {
    companion object {
        fun fromTrackModel(track: SpotifyTrackModel): SpotifyTrackViewModel {
            return SpotifyTrackViewModel(
                name = track.name,
                artists = track.artists.map { it.name },
                albumImageUrl = track.album.images.firstOrNull()?.url,
                duration = track.durationMs,
                isExplicit = track.explicit
            )
        }
    }
}

class SpotifyPlaybackScreenModel(
    private val spotify: SpotifyApiClient
) : ScreenModel {
    private val _currentTrack = MutableStateFlow<SpotifyTrackViewModel?>(null)
    val currentTrack = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState = _playbackState.asStateFlow()

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    fun refresh() {
        screenModelScope.launch {
            try {
                val playerState = spotify.getCurrentPlayerState()

                if (playerState == null || playerState.item == null) {
                    Logger.i { "No track is currently playing." }
                    _currentTrack.value = null
                    _playbackState.update { it.copy(isPlaying = false) }
                } else {
                    _currentTrack.value = SpotifyTrackViewModel.fromTrackModel(playerState.item)
                    _playbackState.update { it.copy(isPlaying = playerState.isPlaying) }
                }
            } catch (e: Exception) {
                Logger.e("Failed to load current track", e)
                _currentTrack.value = null
                _playbackState.update {
                    it.copy(
                        isPlaying = false,
                        error = "Failed to load track: ${e.message}"
                    )
                }
            }

            _playbackState.update { it.copy(isLoading = false) }
        }
    }

    private fun updatePlaybackStateOptimistically(expectedPlayingState: Boolean) {
        _playbackState.update {
            it.copy(
                isPlaying = expectedPlayingState,
                isLoading = true,
                error = null
            )
        }
    }

    fun previousTrack() {
        updatePlaybackStateOptimistically(playbackState.value.isPlaying)
        screenModelScope.launch {
            try {
                spotify.previousTrack()
                refresh()
            } catch (e: Exception) {
                _playbackState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to skip to previous track: ${e.message}"
                    )
                }
            }
        }
    }

    fun nextTrack() {
        updatePlaybackStateOptimistically(playbackState.value.isPlaying)
        screenModelScope.launch {
            try {
                spotify.nextTrack()
                refresh()
            } catch (e: Exception) {
                _playbackState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to skip to next track: ${e.message}"
                    )
                }
            }
        }
    }

    fun resume() {
        updatePlaybackStateOptimistically(true)
        screenModelScope.launch {
            try {
                spotify.resume()
                refresh()
            } catch (e: Exception) {
                _playbackState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to resume: ${e.message}"
                    )
                }
            }
        }
    }

    fun pause() {
        updatePlaybackStateOptimistically(false)
        screenModelScope.launch {
            try {
                spotify.pause()
                refresh()
            } catch (e: Exception) {
                _playbackState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to pause: ${e.message}"
                    )
                }
            }
        }
    }
}

// Screens
object UnauthenticatedScreen : Screen {
    @Composable
    override fun Content() {
        val authFlow: AuthenticationFlow = getKoin().get()
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Connect to Spotify",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedButton(
                onClick = {
                    scope.launch { authFlow.initiateAuthentication() }
                }
            ) {
                Text("Authorize Spotify")
            }
        }
    }
}

object AuthorizationProgressScreen : Screen {
    @Composable
    override fun Content() {
        val authFlow: AuthenticationFlow = getKoin().get()
        val authState by authFlow.authState.collectAsState()
        val uriHandler = LocalUriHandler.current

        when (val state = authState) {
            is AuthenticationFlow.AuthState.AuthorizationInProgress -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Text(
                        text = "Spotify Authorization",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ElevatedButton(
                        onClick = { uriHandler.openUri(state.authUrl) }
                    ) {
                        Text("Open Authorization Page")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { authFlow.resetToUnauthenticated() }
                    ) {
                        Text("Cancel Authorization")
                    }
                }
            }

            else -> {}
        }
    }
}

object PlaybackScreen : Screen {
    @Composable
    override fun Content() {
        val playbackModel: SpotifyPlaybackScreenModel = koinScreenModel()
        val currentTrack by playbackModel.currentTrack.collectAsState()
        val playbackState by playbackModel.playbackState.collectAsState()

        @Composable
        fun PlaybackControlButton(
            contentDescription: String,
            icon: ImageVector,
            onClick: () -> Unit,
        ) {
            IconButton(
                onClick = onClick,
                enabled = !playbackState.isLoading
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            }
        }

        @Composable
        fun PreviousTrackButton() {
            PlaybackControlButton("Previous", FeatherIcons.SkipBack) {
                playbackModel.previousTrack()
            }
        }

        @Composable
        fun PlayPauseButton() {
            if (playbackState.isPlaying) {
                PlaybackControlButton("Pause", FeatherIcons.Pause) {
                    playbackModel.pause()
                }
            } else {
                PlaybackControlButton("Play", FeatherIcons.Play) {
                    playbackModel.resume()
                }
            }
        }

        @Composable
        fun NextTrackButton() {
            PlaybackControlButton("Next", FeatherIcons.SkipForward) {
                playbackModel.nextTrack()
            }
        }

        // Automatically try to load track if none is playing
        LaunchedEffect(Unit) {
            if (currentTrack == null) {
                playbackModel.refresh()
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Error Handling
            playbackState.error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            currentTrack?.let { track ->
                TrackDisplay(track)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PreviousTrackButton()

                    PlayPauseButton()

                    NextTrackButton()
                }
            } ?: Text("No track currently playing")
        }
    }
}


object LoadingScreen : Screen {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TrackDisplay(track: SpotifyTrackViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Album Artwork
        track.albumImageUrl?.let { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album Artwork for ${track.name}",
                contentScale = ContentScale.Crop,
                imageLoader = ImageLoader.Builder(LocalPlatformContext.current).build(),
                modifier = Modifier.size(200.dp)
            )
        }

        // Track Information
        Text(
            text = track.name,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
        Text(
            text = track.artists.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium
        )

        if (track.isExplicit) {
            Text(
                text = "Explicit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun ThemeToggleButton() {
    var isDark by LocalThemeIsDark.current

//    FilledIconToggleButton(checked = isDark, onCheckedChange = { isDark = it }) {
//        Icon(
//            imageVector =
//                if (isDark) FeatherIcons.Sun
//                else FeatherIcons.Moon,
//            contentDescription = "Toggle Theme"
//        )
//    }
    IconButton(onClick = { isDark = !isDark }) {
        Icon(
            imageVector =
                if (isDark) FeatherIcons.Sun
                else FeatherIcons.Moon,
            contentDescription = "Toggle Theme"
        )
    }
}


object LibraryTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Library"
            val icon = rememberVectorPainter(FeatherIcons.List)

            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon
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
        @Composable
        get() {
            val title = "Plugins"
            val icon = rememberVectorPainter(FeatherIcons.Zap)

            return remember {
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon
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
        @Composable
        get() {
            val title = "Playback"
            val icon = rememberVectorPainter(FeatherIcons.Play)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val playbackModel: SpotifyPlaybackScreenModel = koinScreenModel()
        val currentTrack by playbackModel.currentTrack.collectAsState()
        val playbackState by playbackModel.playbackState.collectAsState()

        @Composable
        fun PlaybackControlButton(
            contentDescription: String,
            icon: ImageVector,
            onClick: () -> Unit,
        ) {
            IconButton(
                onClick = onClick,
                enabled = !playbackState.isLoading
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            }
        }

        @Composable
        fun PreviousTrackButton() {
            PlaybackControlButton("Previous", FeatherIcons.SkipBack) {
                playbackModel.previousTrack()
            }
        }

        @Composable
        fun PlayPauseButton() {
            if (playbackState.isPlaying) {
                PlaybackControlButton("Pause", FeatherIcons.Pause) {
                    playbackModel.pause()
                }
            } else {
                PlaybackControlButton("Play", FeatherIcons.Play) {
                    playbackModel.resume()
                }
            }
        }

        @Composable
        fun NextTrackButton() {
            PlaybackControlButton("Next", FeatherIcons.SkipForward) {
                playbackModel.nextTrack()
            }
        }

        // Automatically try to load track if none is playing
        LaunchedEffect(Unit) {
            if (currentTrack == null) {
                playbackModel.refresh()
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Error Handling
            playbackState.error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            currentTrack?.let { track ->
                TrackDisplay(track)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PreviousTrackButton()

                    PlayPauseButton()

                    NextTrackButton()
                }
            } ?: Text("No track currently playing")
        }
    }
}

@Composable
fun LibraryScreen() {
    val pluginManager: PluginManager = getKoin().get()

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
fun PluginManagementScreen() {
    val pluginManager: PluginManager = getKoin().get()
    val availablePlugins = pluginManager.getAvailablePlugins()
    var selectedPlugin by remember { mutableStateOf<AudioSourcePlugin?>(null) }
    var showConfigDialog by remember { mutableStateOf(false) }

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
                )
            }
        }

        Button(
            onClick = {
//                screenModelScope.launch {
//                    // Discover GitHub plugins
//                    pluginManager.discoverAndInstallGitHubPlugins()
//                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Discover Plugins")
        }

        if (showConfigDialog && selectedPlugin != null) {
            PluginConfigDialog(
                plugin = selectedPlugin!!,
                onDismiss = { showConfigDialog = false },
                onConfigSave = { config ->
                    pluginManager.configurePlugin(selectedPlugin!!.pluginId, config)
                    showConfigDialog = false
                }
            )
        }
    }
}

@Composable
fun RowScope.TabItem(tab: Tab) {
    val navigator = LocalTabNavigator.current

    NavigationBarItem(
        selected = navigator.current == tab,
        onClick = { navigator.current = tab },
        label = {
            Text(
                text = tab.options.title
            )
        },
        icon = {
            tab.options.icon?.let {
                Icon(
                    painter = it,
                    contentDescription = tab.options.title,
                )
            }
        }
    )
}

@Composable
fun App() {
    AppTheme {
        TabNavigator(LibraryTab) {
//            Scaffold(
//                topBar = {
//                    TopAppBar(
//                        title = { },
//                        actions = { ThemeToggleButton() }
//                    )
//                },
//                content = { CurrentTab() },
//                bottomBar = {
//                    PrimaryTabRow(selectedTabIndex = 1)
//                    {
//                        PlaybackTab
//                        LibraryTab
//                        PluginManagementTab
//                    }
//                }
//            )
            Scaffold(
                content = { paddingValues ->
                    Column(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        CurrentTab()
                    }
                },
                bottomBar = {
                    NavigationBar()
                    {
                        TabItem(PlaybackTab)
                        TabItem(LibraryTab)
                        TabItem(PluginManagementTab)
                    }
                }
            )
        }
    }
}

// Additional UI components that might need to be implemented
@Composable
fun PluginListItem(
    plugin: AudioSourcePlugin,
    onConfigureClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = plugin.pluginId,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onConfigureClick) {
                    Icon(
                        imageVector = FeatherIcons.Settings,
                        contentDescription = "Configure Plugin"
                    )
                    Text("Configure")
                }
            }
        }
    }
}

@Composable
fun PluginConfigDialog(
    plugin: AudioSourcePlugin,
    onDismiss: () -> Unit,
    onConfigSave: (Map<String, Any>) -> Unit
) {
    val configState = remember { mutableStateMapOf<String, Any>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure ${plugin.pluginId}") },
        text = {
            Column {
                plugin.configOptions.forEach { option ->
                    when (option.type) {
                        ConfigOptionType.STRING -> {
                            TextField(
                                value = configState.getOrDefault(option.key, "") as String,
                                onValueChange = { configState[option.key] = it },
                                label = { Text(option.key) }
                            )
                        }

                        ConfigOptionType.INTEGER -> {
                            TextField(
                                value = configState.getOrDefault(option.key, 0).toString(),
                                onValueChange = { configState[option.key] = it.toIntOrNull() ?: 0 },
                                label = { Text(option.key) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                            )
                        }

                        ConfigOptionType.BOOLEAN -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(option.key)
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = configState.getOrDefault(option.key, false) as Boolean,
                                    onCheckedChange = { configState[option.key] = it }
                                )
                            }
                        }

                        ConfigOptionType.FLOAT -> {
                            TextField(
                                value = configState.getOrDefault(option.key, 0f).toString(),
                                onValueChange = { configState[option.key] = it.toFloatOrNull() ?: 0f },
                                label = { Text(option.key) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfigSave(configState.toMap())
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

val appModule = module {
    singleOf(::Settings)
    singleOf(::OAuthStateService)
    singleOf(::SpotifyAuthClient)
    singleOf(::SpotifyApiClient)
    singleOf(::AuthRedirectServer)
    singleOf(::AuthenticationFlow)
    singleOf(::PluginManager)

    factory { SpotifyPlaybackScreenModel(get()) }
}

fun initialize() {
    startKoin {
        modules(appModule)
    }
}