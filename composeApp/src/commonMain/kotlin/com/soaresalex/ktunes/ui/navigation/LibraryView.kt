package com.soaresalex.ktunes.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.soaresalex.ktunes.viewmodels.LibraryViewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.Disc
import compose.icons.feathericons.List
import compose.icons.feathericons.Music
import compose.icons.feathericons.Users

sealed class LibraryView(
    val title: String, val icon: ImageVector
) : Tab {
    object TracksView : LibraryView("Tracks", FeatherIcons.Music) {
        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 0u, title = title, icon = rememberVectorPainter(icon)
            )

        @Composable
        override fun Content() {
            val viewModel = koinScreenModel<LibraryViewModel>()
            TracksList(viewModel)
        }
    }

    object AlbumsView : LibraryView("Albums", FeatherIcons.Disc) {
        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 1u, title = title, icon = rememberVectorPainter(icon)
            )

        @Composable
        override fun Content() {
            val viewModel = koinScreenModel<LibraryViewModel>()
            AlbumsList(viewModel)
        }
    }

    object ArtistsView : LibraryView("Artists", FeatherIcons.Users) {
        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 2u, title = title, icon = rememberVectorPainter(icon)
            )

        @Composable
        override fun Content() {
            val viewModel = koinScreenModel<LibraryViewModel>()
            ArtistsList(viewModel)
        }
    }

    object PlaylistsView : LibraryView("Playlists", FeatherIcons.List) {
        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 3u, title = title, icon = rememberVectorPainter(icon)
            )

        @Composable
        override fun Content() {
            val viewModel = koinScreenModel<LibraryViewModel>()
            PlaylistsList(viewModel)
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
    fun TrackItem(title: String, artist: String) {
        Box {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
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
        Box {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
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
        Box {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
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
        Box {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.secondaryContainer)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = title, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
