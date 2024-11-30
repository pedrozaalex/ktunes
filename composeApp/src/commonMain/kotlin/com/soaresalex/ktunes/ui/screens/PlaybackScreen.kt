package com.soaresalex.ktunes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.soaresalex.ktunes.viewmodels.AudioTrack
import com.soaresalex.ktunes.viewmodels.PlaybackViewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.Pause
import compose.icons.feathericons.Play
import compose.icons.feathericons.SkipBack
import compose.icons.feathericons.SkipForward

@Composable
fun PlaybackScreen(viewModel: PlaybackViewModel) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    // Automatically try to load track if none is playing
    LaunchedEffect(Unit) {
        if (currentTrack == null) {
            viewModel.refresh()
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

            PlaybackControls(
                isLoading = playbackState.isLoading,
                isPlaying = playbackState.isPlaying,
                onPrevious = viewModel::previousTrack,
                onPlayPause = {
                    if (playbackState.isPlaying) viewModel.pause()
                    else viewModel.resume()
                },
                onNext = viewModel::nextTrack
            )
        } ?: Text("No track currently playing")
    }
}

@Composable
fun TrackDisplay(track: AudioTrack) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        track.artworkUrl?.let { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current).data(imageUrl).crossfade(true).build(),
                contentDescription = "Album Artwork for ${track.title}",
                contentScale = ContentScale.Crop,
                imageLoader = ImageLoader.Builder(LocalPlatformContext.current).build(),
                modifier = Modifier.size(200.dp)
            )
        }

        Text(
            text = track.title, style = MaterialTheme.typography.titleLarge, maxLines = 1
        )
        Text(
            text = track.artist, style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PlaybackControls(
    isLoading: Boolean, isPlaying: Boolean, onPrevious: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        PlaybackControlButton(
            icon = FeatherIcons.SkipBack, contentDescription = "Previous", onClick = onPrevious, enabled = !isLoading
        )

        PlaybackControlButton(
            icon = if (isPlaying) FeatherIcons.Pause else FeatherIcons.Play,
            contentDescription = if (isPlaying) "Pause" else "Play",
            onClick = onPlayPause,
            enabled = !isLoading
        )

        PlaybackControlButton(
            icon = FeatherIcons.SkipForward, contentDescription = "Next", onClick = onNext, enabled = !isLoading
        )
    }
}

@Composable
fun PlaybackControlButton(
    icon: ImageVector, contentDescription: String, onClick: () -> Unit, enabled: Boolean = true
) {
    IconButton(
        onClick = onClick, enabled = enabled
    ) {
        Icon(
            imageVector = icon, contentDescription = contentDescription
        )
    }
}