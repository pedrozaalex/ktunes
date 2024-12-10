package com.soaresalex.ktunes.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.soaresalex.ktunes.theme.AppTheme
import com.soaresalex.ktunes.theme.LocalThemeIsDark
import com.soaresalex.ktunes.viewmodels.LibraryViewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

@Composable
fun WindowScope.TitleBar(
    currentTrack: String = "Not Playing", currentArtist: String = "", onClose: () -> Unit = {}
) = WindowDraggableArea {
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Currently playing track information
        Row(
            modifier = Modifier.weight(1f).padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentTrack, style = MaterialTheme.typography.bodyMedium, maxLines = 1
                )
                if (currentArtist.isNotEmpty()) {
                    Text(
                        text = currentArtist, style = MaterialTheme.typography.bodySmall, maxLines = 1
                    )
                }
            }
        }


        IconButton(
            onClick = onClose, modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = FeatherIcons.X,
                contentDescription = "Close",
            )
        }
    }
}


@Composable
fun App(
    titlebar: @Composable (content: @Composable () -> Unit) -> Unit = { },
) = AppTheme {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    Column(Modifier.padding(4.dp)) {
        titlebar({
            Row(verticalAlignment = Alignment.Top) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(0.5f).defaultMinSize(minWidth = 200.dp),
                )

                Row {
                    ThemeToggleButton()
                    SettingsButton()
                }
            }
        })

        Navigator(MainScreen) { CurrentScreen() }
    }
}

@Composable
fun SettingsButton() = IconButton(onClick = { /* Open settings */ }) {
    Icon(FeatherIcons.Settings, contentDescription = "Settings")
}

enum class LibraryCategory {
    TRACKS, ALBUMS, ARTISTS, PLAYLISTS
}

object MainScreen : Screen {
    @Composable
    override fun Content() {
        var selectedCategory by remember { mutableStateOf(LibraryCategory.TRACKS) }

        Scaffold { paddingValues ->
            Row(Modifier.fillMaxSize().padding(paddingValues)) {
                NavigationSidebar(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                )

                Box(Modifier.weight(1f)) {
                    LibraryContent(
                        category = selectedCategory, viewModel = koinScreenModel()
                    )
                }
            }
        }
    }
}


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

@Composable
fun NavigationSidebar(
    selectedCategory: LibraryCategory, onCategorySelected: (LibraryCategory) -> Unit
) {
    NavigationRail(
        modifier = Modifier.width(80.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NavigationRailItem(
                selected = selectedCategory == LibraryCategory.TRACKS,
                onClick = { onCategorySelected(LibraryCategory.TRACKS) },
                icon = {
                    Icon(FeatherIcons.Music, contentDescription = "Tracks")
                },
                label = {
                    // Use Box with fixed height to prevent layout shifts
                    Box(modifier = Modifier.height(20.dp)) {
                        Text("Tracks")
                    }
                })
            NavigationRailItem(
                selected = selectedCategory == LibraryCategory.ALBUMS,
                onClick = { onCategorySelected(LibraryCategory.ALBUMS) },
                icon = {
                    Icon(FeatherIcons.Disc, contentDescription = "Albums")
                },
                label = {
                    Box(modifier = Modifier.height(20.dp)) {
                        Text("Albums")
                    }
                })
            NavigationRailItem(
                selected = selectedCategory == LibraryCategory.ARTISTS,
                onClick = { onCategorySelected(LibraryCategory.ARTISTS) },
                icon = {
                    Icon(FeatherIcons.Users, contentDescription = "Artists")
                },
                label = {
                    Box(modifier = Modifier.height(20.dp)) {
                        Text("Artists")
                    }
                })
            NavigationRailItem(
                selected = selectedCategory == LibraryCategory.PLAYLISTS,
                onClick = { onCategorySelected(LibraryCategory.PLAYLISTS) },
                icon = {
                    Icon(FeatherIcons.List, contentDescription = "Playlists")
                },
                label = {
                    Box(modifier = Modifier.height(20.dp)) {
                        Text("Playlists")
                    }
                })
        }
    }
}

@Composable
fun SearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    maxLines: Int = 1,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isDark by LocalThemeIsDark.current

    // Dynamic icon color calculation
    val iconColor = remember(isFocused, isHovered, isDark) {
        when {
            isFocused -> contentColor
            isHovered -> contentColor.copy(alpha = 0.8f)
            else -> contentColor.copy(alpha = 0.6f)
        }
    }

    // Hover and focus state background calculation
    val dynamicBackgroundColor = remember(isHovered, isFocused, isDark) {
        when {
            isFocused -> backgroundColor.copy(alpha = 0.9f)
            isHovered -> backgroundColor.copy(alpha = 0.85f)
            else -> backgroundColor
        }
    }

    // Animated border color and width
    val borderAnimation by animateFloatAsState(
        targetValue = if (isFocused) 2f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
    )

    val borderColor = remember(isFocused, isDark) {
        if (isFocused) contentColor.copy(alpha = 0.5f) else Color.Transparent
    }

    Box(
        modifier = modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(28.dp))
            .background(dynamicBackgroundColor).border(
                width = borderAnimation.dp, color = borderColor, shape = RoundedCornerShape(28.dp)
            ).hoverable(interactionSource).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart
    ) {
        // Improved Layout: Row for Icon and TextField
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Icon with Dynamic Color
            Box(
                modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FeatherIcons.Search, contentDescription = "Search", tint = iconColor
                )
            }

            // Text Field Container
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                if (query.text.isEmpty() && !isFocused) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = {
                        onQueryChange(it)
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged {
                        isFocused = it.isFocused
                    },
                    textStyle = textStyle.copy(color = contentColor),
                    cursorBrush = SolidColor(contentColor),
                    singleLine = maxLines == 1,
                    maxLines = maxLines,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch(query.text)
                            keyboardController?.hide()
                        }),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart
                        ) {
                            innerTextField()
                        }
                    })
            }

            // Clear Button
            AnimatedVisibility(
                visible = query.text.isNotEmpty(), enter = fadeIn(), exit = fadeOut()
            ) {
                IconButton({ onQueryChange(TextFieldValue("")) }) {
                    Icon(
                        imageVector = FeatherIcons.X,
                        contentDescription = "Clear",
                        tint = contentColor.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}


@Composable
fun LibraryContent(
    category: LibraryCategory, viewModel: LibraryViewModel
) = when (category) {
    LibraryCategory.TRACKS -> TracksList(viewModel)
    LibraryCategory.ALBUMS -> AlbumsList(viewModel)
    LibraryCategory.ARTISTS -> ArtistsList(viewModel)
    LibraryCategory.PLAYLISTS -> PlaylistsList(viewModel)
}

@Composable
fun TracksList(viewModel: LibraryViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TODO: Replace with actual tracks from viewModel
        items(10) { index ->
            TrackItem(title = "Track $index", artist = "Artist $index")
        }
    }
}

@Composable
fun AlbumsList(viewModel: LibraryViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TODO: Replace with actual albums from viewModel
        items(10) { index ->
            AlbumItem(title = "Album $index", artist = "Artist $index")
        }
    }
}

@Composable
fun ArtistsList(viewModel: LibraryViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TODO: Replace with actual artists from viewModel
        items(10) { index ->
            ArtistItem(name = "Artist $index")
        }
    }
}

@Composable
fun PlaylistsList(viewModel: LibraryViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TODO: Replace with actual playlists from viewModel
        items(10) { index ->
            PlaylistItem(title = "Playlist $index")
        }
    }
}

@Composable
fun BottomPlaybackBar() {

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant, content = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album artwork or placeholder
                Box(
                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.secondary)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Track info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Track Title", style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Artist Name", style = MaterialTheme.typography.bodySmall
                    )
                }

                // Playback controls
                Row {
                    IconButton(onClick = { /* Previous track */ }) {
                        Icon(FeatherIcons.SkipBack, contentDescription = "Previous")
                    }
                    IconButton(onClick = { /* Play/Pause */ }) {
                        Icon(FeatherIcons.Play, contentDescription = "Play")
                    }
                    IconButton(onClick = { /* Next track */ }) {
                        Icon(FeatherIcons.SkipForward, contentDescription = "Next")
                    }
                }
            }
        })
}

// Placeholder item components
@Composable
fun TrackItem(title: String, artist: String) {
    Card {
        Column(
            modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = artist, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AlbumItem(title: String, artist: String) {
    Card {
        Column(
            modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = artist, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ArtistItem(name: String) {
    Card {
        Column(
            modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PlaylistItem(title: String) {
    Card {
        Column(
            modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}