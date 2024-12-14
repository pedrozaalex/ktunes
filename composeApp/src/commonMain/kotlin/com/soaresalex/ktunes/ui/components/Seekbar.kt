package com.soaresalex.ktunes.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SeekBar(
	currentProgress: Long,
	totalDuration: Long,
	onSeek: (Long) -> Unit,
	isPlaying: Boolean,
	scope: CoroutineScope = rememberCoroutineScope(),
	modifier: Modifier = Modifier,
	barHeight: Dp = 9.dp,
	borderRadius: Dp = 6.dp,
	activeColor: Color = MaterialTheme.colorScheme.primary,
	backgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
	useRemainingTime: Boolean = false
) {
	var seekPosition by remember { mutableStateOf(currentProgress.toFloat()) }

	// Simplified progress calculation
	val progressFraction = calculateProgressFraction(currentProgress, totalDuration)

	// Simplified time formatting
	val timeFormatter = remember { TimeFormatter() }

	Column {
		// Seek bar with simplified gesture handling
		Box(
			modifier = modifier
				.fillMaxWidth()
				.height(barHeight)
				.clip(RoundedCornerShape(borderRadius))
				.pointerInput(totalDuration) {
					detectTapGestures(
						onTap = { offset ->
							if (isPlaying) {
								val newPosition = (offset.x / size.width * totalDuration).toLong()
								scope.launch {
									onSeek(newPosition)
								}
							}
						}
					)
				}
		) {
			// Background bar
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
					.background(backgroundColor)
			)

			// Progress bar
			Box(
				modifier = Modifier
					.fillMaxWidth(progressFraction)
					.fillMaxHeight()
					.background(activeColor)
			)
		}

		// Time labels
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				text = timeFormatter.format(currentProgress, useRemainingTime, totalDuration),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
			Text(
				text = timeFormatter.format(totalDuration, false),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
	}
}

// Simplified time formatting utility
private class TimeFormatter {
	fun format(millis: Long, isRemaining: Boolean, total: Long? = null): String {
		val effectiveMillis = if (isRemaining && total != null) total - millis else millis
		val totalSeconds = effectiveMillis / 1000
		val minutes = totalSeconds / 60
		val seconds = totalSeconds % 60
		return "%02d:%02d".format(minutes, seconds)
	}
}

// Pure function for progress fraction calculation
private fun calculateProgressFraction(current: Long, total: Long): Float {
	return if (total > 0) current.toFloat() / total else 0f
}