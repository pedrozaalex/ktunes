package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.soaresalex.ktunes.data.models.Track
import com.soaresalex.ktunes.data.service.PlaybackService
import compose.icons.FeatherIcons
import compose.icons.feathericons.Pause
import compose.icons.feathericons.Play
import compose.icons.feathericons.SkipBack
import compose.icons.feathericons.SkipForward
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PlaybackControls() {
	val playbackService: PlaybackService = koinInject()

	val currentTrack by playbackService.currentTrack.collectAsState()
	val isPlaying by playbackService.isPlaying.collectAsState()
	val progress by playbackService.progress.collectAsState()
	val audioLevel by playbackService.audioLevel.collectAsState()

	val scope = rememberCoroutineScope()
	val handlePlay: () -> Unit = { scope.launch { playbackService.resume() } }
	val handlePause: () -> Unit = { scope.launch { playbackService.pause() } }
	val handleSeek: (Long) -> Unit = { position -> scope.launch { playbackService.seekTo(position) } }

	Row(
		modifier = Modifier.padding(horizontal = 16.dp).width(500.dp), verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			TrackMetadata(currentTrack)

			Row {
				IconButton(onClick = {/*TODO*/ }) { Icon(FeatherIcons.SkipBack, "Previous Track") }

				if (isPlaying) {
					FilledIconButton(onClick = handlePause) { Icon(FeatherIcons.Pause, "Pause") }
				} else {
					FilledIconButton(onClick = handlePlay) { Icon(FeatherIcons.Play, "Play") }
				}

				IconButton(onClick = {/*TODO*/ }) { Icon(FeatherIcons.SkipForward, "Next Track") }
			}
		}

		Spacer(Modifier.width(16.dp))

		SeekBar(progress, currentTrack?.duration ?: 0, handleSeek, isPlaying)
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
		onClick = onClick,
		modifier = modifier.padding(4.dp),
	) {
		Icon(
			imageVector = icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onSurface
		)
	}
}

/**
 * Displays current track metadata
 */
@Composable
private fun TrackMetadata(track: Track?) {
	Box(Modifier.width(200.dp)) {
		track?.let {
			Row(horizontalArrangement = Arrangement.End) {
				AsyncImage(
					model = track.albumArtUri,
					contentDescription = track.album,
					modifier = Modifier.fillMaxHeight().clip(MaterialTheme.shapes.extraSmall)
				)

				Spacer(Modifier.size(8.dp))

				Column(modifier = Modifier.fillMaxHeight()) {
					Text(
						text = it.title,
						style = MaterialTheme.typography.labelMedium,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
					Text(
						text = it.artist,
						style = MaterialTheme.typography.labelSmall,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
					)
				}
			}
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
