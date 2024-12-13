package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soaresalex.ktunes.data.models.Track
import compose.icons.FeatherIcons
import compose.icons.feathericons.PauseCircle
import compose.icons.feathericons.PlayCircle
import compose.icons.feathericons.StopCircle

@Composable
fun PlaybackControls(
	currentTrack: Track?,
	isPlaying: Boolean,
	onPlayPauseToggle: () -> Unit,
	onStop: () -> Unit
) {
	Row(verticalAlignment = Alignment.CenterVertically) {
		IconButton(onClick = onPlayPauseToggle) {
			Icon(
				imageVector = if (isPlaying) FeatherIcons.PauseCircle else FeatherIcons.PlayCircle,
				contentDescription = if (isPlaying) "Pause" else "Play",
				modifier = Modifier.size(24.dp)
			)
		}

		IconButton(onClick = onStop) {
			Icon(
				imageVector = FeatherIcons.StopCircle,
				contentDescription = "Stop",
				modifier = Modifier.size(24.dp)
			)
		}

		Spacer(Modifier.width(8.dp))

		// Display current track information
		currentTrack?.let {
			Text(text = "${it.title} - ${it.artist}")
		}
	}
}