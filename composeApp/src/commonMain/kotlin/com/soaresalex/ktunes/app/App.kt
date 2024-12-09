package com.soaresalex.ktunes.app

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
fun App() {
    AppTheme {
        Navigator(MainScreen) {
            CurrentScreen()
        }
    }
}

enum class LibraryCategory {
    TRACKS, ALBUMS, ARTISTS, PLAYLISTS
}

object MainScreen : Screen {
    @Composable
    override fun Content() {
        var selectedCategory by remember { mutableStateOf(LibraryCategory.TRACKS) }
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
        var isNavigationExpanded by remember { mutableStateOf(false) }

        Scaffold(topBar = {
            TopAppBar(navigationIcon = {
                IconButton(onClick = { isNavigationExpanded = !isNavigationExpanded }) {
                    Icon(FeatherIcons.Menu, contentDescription = "Navigation")
                }
            }, title = {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(0.5f),
                )
            }, actions = {
                ThemeToggleButton()
                IconButton(onClick = { /* Open settings */ }) {
                    Icon(FeatherIcons.Settings, contentDescription = "Settings")
                }
            })
        }, content = { paddingValues ->
            Row(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                NavigationSidebar(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    isExpanded = isNavigationExpanded
                )

                Column(
                    modifier = Modifier.weight(1f).padding(16.dp)
                ) {
                    LibraryContent(
                        category = selectedCategory, viewModel = koinScreenModel()
                    )
                }
            }
        }, bottomBar = {
            BottomPlaybackBar()
        })
    }
}


@Composable
fun TopAppBar(
    navigationIcon: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center // Added to center title
        ) {
            Box(modifier = Modifier.width(48.dp)) {
                navigationIcon()
            }

            Box(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp).fillMaxWidth(0.5f), // Constrained width
                contentAlignment = Alignment.Center
            ) {
                title()
            }

            Box(modifier = Modifier.width(IntrinsicSize.Min)) {
                Row {
                    actions()
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
    selectedCategory: LibraryCategory, onCategorySelected: (LibraryCategory) -> Unit, isExpanded: Boolean = false
) {
    val animatedWidth by animateDpAsState(
        targetValue = if (isExpanded) 120.dp else 80.dp, animationSpec = tween(durationMillis = 150)
    )

    NavigationRail(
        modifier = Modifier.width(animatedWidth)
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
                        this@Column.AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Text("Tracks")
                        }
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
                        this@Column.AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Text("Albums")
                        }
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
                        this@Column.AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Text("Artists")
                        }
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
                        this@Column.AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Text("Playlists")
                        }
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
        modifier = modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp))
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
                modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center
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
) {
    when (category) {
        LibraryCategory.TRACKS -> TracksList(viewModel)
        LibraryCategory.ALBUMS -> AlbumsList(viewModel)
        LibraryCategory.ARTISTS -> ArtistsList(viewModel)
        LibraryCategory.PLAYLISTS -> PlaylistsList(viewModel)
    }
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