package com.soaresalex.ktunes.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceAtMost
import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Advanced SeekBar component for precise audio playback seeking with hover and RTL support
 *
 * @param currentProgress Current playback progress in milliseconds
 * @param track
 * @param onSeek Callback when user seeks to a new position
 * @param scope CoroutineScope for handling asynchronous seek operations
 * @param useRemainingTime Flag to toggle between elapsed and remaining time display
 */
@Composable
fun SeekBar(
	currentProgress: Long,
	track: Track,
	onSeek: (Long) -> Unit,
	scope: CoroutineScope = rememberCoroutineScope(),
	modifier: Modifier = Modifier,
	barHeight: Dp = 9.dp,
	borderRadius: Dp = 6.dp,
	activeColor: Color = MaterialTheme.colorScheme.primary,
	backgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
	useRemainingTime: Boolean = false
) {
	var isSeeking by remember { mutableStateOf(false) }
	var seekPosition by remember { mutableStateOf(currentProgress.toFloat()) }
	var hoverPosition by remember { mutableStateOf<Float?>(null) }

	val interactionSource = remember { MutableInteractionSource() }
	val isHovered by interactionSource.collectIsHoveredAsState()

	val totalDuration = remember { track.duration }

	val animatedProgressFraction by animateFloatAsState(
		targetValue = if (totalDuration > 0) currentProgress.toFloat() / totalDuration else 0f,
		label = "Progress Animation", animationSpec = tween(100)
	)

	val progressFraction = if (totalDuration > 0) {
		val baseFraction = if (isSeeking) {
			seekPosition / totalDuration.toFloat()
		} else {
			animatedProgressFraction
		}
		baseFraction
	} else 0f

	// Time display logic with remaining time support
	val formatTime: (Long, Boolean) -> String = { millis, remaining ->
		val effectiveMillis = if (remaining) totalDuration - millis else millis
		val totalSeconds = effectiveMillis / 1000
		val minutes = totalSeconds / 60
		val seconds = totalSeconds % 60
		"%02d:%02d".format(minutes, seconds)
	}

	// Determine displayed position for hover and current states
	val displayedPosition = if (isHovered) hoverPosition?.let {
		(it * totalDuration).toLong()
	} ?: currentProgress else currentProgress

	val density = LocalDensity.current.density

	val seekBarModifier =
		modifier.fillMaxWidth().height(barHeight).clip(RoundedCornerShape(borderRadius)).hoverable(interactionSource)
			.pointerInput(Unit) {
				awaitPointerEventScope {
					while (true) {
						val event = awaitPointerEvent()
						val position = event.changes.firstOrNull()

						position?.let { pointerInput ->
							// Calculate hover/seek fraction
							val localSize = size
							val localOffset = pointerInput.position
							val fraction = (localOffset.x / localSize.width).coerceIn(0f, 1f)

							// Handle hover state
							if (isHovered) {
								hoverPosition = fraction
							}

							// Handle seeking
							if (event.type.toString() == "Press") {
								isSeeking = true
								seekPosition = fraction * totalDuration
							}

							// Handle drag
							if (event.type.toString() == "Move" && isSeeking) {
								val dragAmount = pointerInput.positionChange().x / density
								val dragFraction = dragAmount / size.width

								seekPosition =
									(seekPosition + dragFraction * totalDuration).coerceIn(0f, totalDuration.toFloat())
							}

							// Handle release
							if (event.type.toString() == "Release" && isSeeking) {
								scope.launch {
									onSeek(seekPosition.toLong())
									isSeeking = false
									hoverPosition = null
								}
							}
						}
					}
				}
			}

	Column {
		Box(modifier = seekBarModifier) {
			val mod = Modifier.clip(MaterialTheme.shapes.large)

			// Background bar
			Box(
				mod.fillMaxWidth().fillMaxHeight().background(backgroundColor)
			)

			if (isHovered && hoverPosition != null) hoverPosition?.let { hoverFraction ->
				// Progress bar
				Box(
					mod.fillMaxWidth(
						progressFraction.fastCoerceAtMost(hoverFraction)
					).fillMaxHeight().background(activeColor)
				)

				// Hover state overlay
				Box(
					mod.fillMaxWidth(
						hoverFraction.fastCoerceAtLeast(progressFraction)
					).fillMaxHeight().background(activeColor.copy(alpha = 0.45f))
				)
			} else {
				// Progress bar
				Box(mod.fillMaxWidth(progressFraction).fillMaxHeight().background(activeColor))
			}
		}

		// Time labels
		Row(
			modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				text = formatTime(displayedPosition, useRemainingTime),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
			Text(
				text = formatTime(totalDuration, false),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
	}
}