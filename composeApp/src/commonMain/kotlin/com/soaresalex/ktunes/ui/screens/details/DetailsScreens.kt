package com.soaresalex.ktunes.ui.screens.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.data.models.Track

// Track Detail Screen
data class TrackDetailScreen(val track: Track) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Track Details") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Artist: ${track.artist}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Album: ${track.album}",
                    style = MaterialTheme.typography.bodyLarge
                )
                track.year?.let { year ->
                    Text(
                        text = "Year: $year",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // TODO: Add more track details, play controls, etc.
            }
        }
    }
}

// Album Detail Screen
data class AlbumDetailScreen(val album: Album) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Album Details") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Artist: ${album.artist}",
                    style = MaterialTheme.typography.bodyLarge
                )
                album.releaseYear?.let { year ->
                    Text(
                        text = "Year: $year",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text(
                    text = "Tracks: ${album.trackCount}",
                    style = MaterialTheme.typography.bodyLarge
                )
                // TODO: Add track list, album art, etc.
            }
        }
    }
}

// Artist Detail Screen
data class ArtistDetailScreen(val artist: Artist) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Artist Details") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Albums: ${artist.albumCount}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Tracks: ${artist.trackCount}",
                    style = MaterialTheme.typography.bodyLarge
                )
                // TODO: Add album list, artist photo, etc.
            }
        }
    }
}