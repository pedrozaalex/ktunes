package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Improved SeekBar component for precise audio playback seeking
 *
 * @param currentProgress Current playback progress in milliseconds
 * @param totalDuration Total duration of the track in milliseconds
 * @param onSeek Callback when user seeks to a new position
 * @param isPlaying Whether the track is currently playing
 */
@Composable
fun SeekBar(
	currentProgress: Long, totalDuration: Long, onSeek: (Long) -> Unit, isPlaying: Boolean
) {
	var isSeeking by remember { mutableStateOf(false) }
	var seekPosition by remember { mutableStateOf(currentProgress.toFloat()) }
	var barWidth by remember { mutableStateOf(0) }

	val progressFraction = if (totalDuration > 0) {
		if (isSeeking) {
			seekPosition / totalDuration.toFloat()
		} else {
			currentProgress.toFloat() / totalDuration
		}
	} else 0f

	val density = LocalDensity.current.density

	Box(
		modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
		.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)).onSizeChanged { barWidth = it.width }
		.pointerInput(Unit) {
			detectTapGestures(
				onTap = { offset ->
					if (!isPlaying) return@detectTapGestures

					val fraction = (offset.x / barWidth).coerceIn(0f, 1f)
					val newPosition = (fraction * totalDuration).toLong()
					onSeek(newPosition)
				})
		}.pointerInput(Unit) {

			detectHorizontalDragGestures(onDragStart = { isSeeking = true }, onDragEnd = {
				if (!isPlaying) return@detectHorizontalDragGestures

				onSeek(seekPosition.toLong())
				isSeeking = false
			}, onHorizontalDrag = { _, dragAmount ->
				if (!isPlaying) return@detectHorizontalDragGestures

				val adjustedDragAmount = dragAmount / density

				val newPosition = (seekPosition + adjustedDragAmount * totalDuration / barWidth).coerceIn(
						0f,
						totalDuration.toFloat()
					)
				seekPosition = newPosition
			})
		}) {
		// Progress indicator
		Box(
			modifier = Modifier.fillMaxWidth(progressFraction).fillMaxHeight()
				.background(MaterialTheme.colorScheme.primary)
		)

		// Preview indicator (hover state)
		// Note: Hover state is typically handled differently in Compose
		// You might need to implement this with a custom overlay or modifier
	}
}