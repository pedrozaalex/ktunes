package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.CoroutineScope
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

		SeekBar(
			playbackService = playbackService, scope = scope
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

/**
 * Playback progress slider
 */
@Composable
fun SeekBar(
	playbackService: PlaybackService,
	scope: CoroutineScope,
) {
	val progress by playbackService.progress.collectAsState()
	val duration by remember { derivedStateOf { playbackService.currentTrack.value?.duration ?: 0 } }
	val isPlaying by playbackService.isPlaying.collectAsState()

	var isSeeking by remember { mutableStateOf(false) }
	var seekPosition by remember { mutableStateOf(0f) }

	fun getProgressFraction(): Float {
		return if (duration > 0) progress.toFloat() / duration else 0f
	}

	fun getSeekPositionFraction(): Float {
		return if (duration > 0) seekPosition / duration else 0f
	}

	Box(
		modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
		.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)) // Background color
		.pointerInput(Unit) {
			detectTapGestures(
				onPress = { _ -> isSeeking = true },
				onTap = { offset ->
					if (!isPlaying) return@detectTapGestures

					val fraction = (offset.x / size.width).coerceIn(0f, 1f)
					val newPosition = (fraction * duration).toLong()
					scope.launch { playbackService.seekTo(newPosition) }
					isSeeking = false
				},
			)
		}.pointerInput(Unit) {
			detectHorizontalDragGestures(onDragStart = { isSeeking = true }, onDragEnd = {
				if (!isPlaying) return@detectHorizontalDragGestures

				scope.launch { playbackService.seekTo(seekPosition.toLong()) }
				isSeeking = false
			}, onHorizontalDrag = { _, dragAmount ->
				if (!isPlaying) return@detectHorizontalDragGestures

				val newPosition = (seekPosition + dragAmount * duration / size.width).coerceIn(0f, duration.toFloat())
				seekPosition = newPosition
			})
		}) {
		// Filled part of the seek bar
		Box(
			modifier = Modifier.fillMaxWidth(if (isSeeking) getSeekPositionFraction() else getProgressFraction())
				.fillMaxHeight().background(MaterialTheme.colorScheme.primary)
		)
	}
}