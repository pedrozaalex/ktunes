package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.soaresalex.ktunes.data.models.Track
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

/**
 * Comprehensive playback controls component for music player.
 *
 * @param currentTrack Currently playing track
 * @param isPlaying Indicates if music is currently playing
 * @param progress Current playback progress in milliseconds
 * @param onPlayPauseToggle Callback for play/pause action
 * @param onStop Callback for stop action
 * @param onSeek Callback for seeking to a specific position
 * @param onPreviousTrack Callback for previous track navigation
 * @param onNextTrack Callback for next track navigation
 * @param audioLevel Current audio level for visualization
 */
@Composable
fun PlaybackControls(
	currentTrack: Track?,
	isPlaying: Boolean,
	progress: Long,
	onPlayPauseToggle: () -> Unit,
	onStop: () -> Unit,
	onSeek: (Long) -> Unit,
	onPreviousTrack: () -> Unit = {},
	onNextTrack: () -> Unit = {},
	audioLevel: Float = 0f
) {
	Column(
		modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		// Track Information and Controls Row
		Row(
			verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			// Playback Navigation Controls
			NavigationControls(
				isPlaying = isPlaying,
				onPlayPauseToggle = onPlayPauseToggle,
				onStop = onStop,
				onPreviousTrack = onPreviousTrack,
				onNextTrack = onNextTrack
			)

			// Track Metadata
			TrackMetadata(currentTrack)

			// Audio Level Visualization
			AudioLevelIndicator(audioLevel)
		}

		// Progress Slider
		currentTrack?.let { track ->
			PlaybackProgressSlider(
				progress = progress, maxProgress = track.duration, onSeek = onSeek
			)
		}
	}
}

/**
 * Composable for navigation and playback control buttons
 */
@Composable
private fun NavigationControls(
	isPlaying: Boolean,
	onPlayPauseToggle: () -> Unit,
	onStop: () -> Unit,
	onPreviousTrack: () -> Unit,
	onNextTrack: () -> Unit
) {
	Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
		// Previous Track Button
		MediaControlButton(
			icon = FeatherIcons.SkipBack, contentDescription = "Previous Track", onClick = onPreviousTrack
		)

		// Play/Pause Button
		MediaControlButton(
			icon = if (isPlaying) FeatherIcons.PauseCircle else FeatherIcons.PlayCircle,
			contentDescription = if (isPlaying) "Pause" else "Play",
			modifier = Modifier.size(48.dp),
			onClick = onPlayPauseToggle
		)

		// Stop Button
		MediaControlButton(
			icon = FeatherIcons.StopCircle, contentDescription = "Stop", onClick = onStop
		)

		// Next Track Button
		MediaControlButton(
			icon = FeatherIcons.SkipForward, contentDescription = "Next Track", onClick = onNextTrack
		)
	}
}

/**
 * Reusable media control button with consistent styling
 */
@Composable
private fun MediaControlButton(
	icon: ImageVector, contentDescription: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}
) {
	IconButton(
		onClick = onClick, modifier = modifier.size(40.dp)
	) {
		Icon(
			imageVector = icon,
			contentDescription = contentDescription,
			modifier = Modifier.fillMaxSize(),
			tint = MaterialTheme.colorScheme.onSurface
		)
	}
}

/**
 * Displays current track metadata
 */
@Composable
private fun TrackMetadata(track: Track?) {
	track?.let {
		Column(
			modifier = Modifier.width(200.dp), verticalArrangement = Arrangement.spacedBy(2.dp)
		) {
			Text(
				text = it.title,
				style = MaterialTheme.typography.bodyMedium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = it.artist,
				style = MaterialTheme.typography.bodySmall,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
	}
}

/**
 * Audio level visualization component
 */
@Composable
private fun AudioLevelIndicator(level: Float) {
	Box(
		modifier = Modifier.height(24.dp).width(60.dp).clip(RoundedCornerShape(4.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant)
	) {
		Box(
			modifier = Modifier.fillMaxHeight().fillMaxWidth(level.coerceIn(0f, 1f))
				.background(MaterialTheme.colorScheme.primary)
		)
	}
}

/**
 * Playback progress slider
 */
@Composable
private fun PlaybackProgressSlider(
	progress: Long, maxProgress: Long, onSeek: (Long) -> Unit
) {
	Slider(
		value = progress.toFloat(),
		onValueChange = { newProgress -> onSeek(newProgress.toLong()) },
		valueRange = 0f..maxProgress.toFloat(),
		modifier = Modifier.fillMaxWidth(),
		colors = SliderDefaults.colors(
			activeTrackColor = MaterialTheme.colorScheme.primary,
			inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
		)
	)
}