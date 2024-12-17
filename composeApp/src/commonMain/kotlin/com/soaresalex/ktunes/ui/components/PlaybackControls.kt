package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.Bitmap
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.toBitmap
import com.soaresalex.ktunes.data.models.Track
import com.soaresalex.ktunes.data.service.PlayQueueService
import com.soaresalex.ktunes.data.service.PlaybackService
import compose.icons.FeatherIcons
import compose.icons.feathericons.Pause
import compose.icons.feathericons.Play
import compose.icons.feathericons.SkipBack
import compose.icons.feathericons.SkipForward
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.max
import kotlin.math.min

@Composable
fun PlaybackControls() {
	val playbackService: PlaybackService = koinInject()
	val playQueueService: PlayQueueService = koinInject()

	val currentTrack by playbackService.currentTrack.collectAsState()
	val isPlaying by playbackService.isPlaying.collectAsState()
	val progress by playbackService.progress.collectAsState()
	val audioLevel by playbackService.audioLevel.collectAsState()
	val currentQueue by playQueueService.queue.collectAsState()
	val currentTrackIndex by playQueueService.currentTrackIndex.collectAsState()

	val scope = rememberCoroutineScope()
	val handlePlay: () -> Unit = { scope.launch { playbackService.resume() } }
	val handlePause: () -> Unit = { scope.launch { playbackService.pause() } }
	val handleSeek: (Long) -> Unit = { position -> scope.launch { playbackService.seekTo(position) } }
	val handleNext: () -> Unit = {
		scope.launch {
			playbackService.playNext()
		}
	}
	val handlePrevious: () -> Unit = {
		scope.launch {
			playbackService.playPrevious()
		}
	}

	currentTrack?.let { track ->
		Row(
			modifier = Modifier.padding(horizontal = 16.dp).width(500.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				TrackMetadata(track)

				Row {
					IconButton(onClick = handlePrevious) { Icon(FeatherIcons.SkipBack, "Previous Track") }

					when {
						isPlaying -> Triple(handlePause, FeatherIcons.Pause, "Pause")
						else -> Triple(handlePlay, FeatherIcons.Play, "Play")
					}.let { (onClick, icon, desc) ->
						FilledIconButton(onClick) { Icon(icon, desc) }
					}

					IconButton(onClick = handleNext) { Icon(FeatherIcons.SkipForward, "Next Track") }
				}
			}

			Spacer(Modifier.width(16.dp))

			SeekBar(progress, track, handleSeek)
		}
	}
}

/**
 * Displays current track metadata
 */
@Composable
private fun TrackMetadata(track: Track) {
	val tertiaryColor = MaterialTheme.colorScheme.tertiary
	val onTertiary = MaterialTheme.colorScheme.onTertiary

	class DominantColorExtractor {
		/**
		 * Extracts the dominant color from a bitmap using a color quantization approach.
		 *
		 * @param bitmap The input bitmap to analyze
		 * @param sampleSize Reduction factor for processing (higher = faster, less accurate)
		 * @return The dominant color as an androidx.compose.ui.graphics.Color
		 */
		fun extract(
			bitmap: Bitmap, sampleSize: Int = 10
		): Color {
			// Color bucket to aggregate similar colors
			val colorBuckets = mutableMapOf<Int, Int>()

			// Iterate through pixels
			for (x in 0 until bitmap.width step sampleSize) {
				for (y in 0 until bitmap.height step sampleSize) {
					// Skip fully transparent pixels
					if (bitmap.getAlphaf(x, y) == 0f) continue

					val pixelColor = bitmap.getColor(x, y)

					// Group similar colors together
					val quantizedColor = quantizeColor(pixelColor)
					colorBuckets[quantizedColor] = (colorBuckets[quantizedColor] ?: 0) + 1
				}
			}

			// Find the most frequent color
			return colorBuckets.maxByOrNull { it.value }?.key?.let { Color(it) } ?: tertiaryColor
		}

		/**
		 * Quantizes a color to reduce similar colors to a single representative color.
		 * This helps group visually similar colors together.
		 *
		 * @param color The original color to quantize
		 * @return A quantized color representation
		 */
		private fun quantizeColor(color: Int): Int {
			val red = Color(color).red
			val green = Color(color).green
			val blue = Color(color).blue

			// Reduce color precision to group similar colors
			val quantizedRed = (red / 32) * 32
			val quantizedGreen = (green / 32) * 32
			val quantizedBlue = (blue / 32) * 32

			return Color(quantizedRed, quantizedGreen, quantizedBlue).toArgb()
		}
	}

	class ContrastingColorGenerator {
		/**
		 * Generate a contrasting color that meets WCAG 2 AA standards
		 * @param baseColor The original color to generate a contrast against
		 * @param isLargeText Whether the text is considered large (helps determine contrast requirements)
		 * @return A color that provides sufficient contrast
		 */
		fun generate(baseColor: Color, isLargeText: Boolean = false): Color {
			// WCAG 2 contrast ratio requirements
			val minContrastRatio = if (isLargeText) 3.0 else 4.5

			// Try light and dark colors
			val lightColors = listOf(
				Color.White, Color.Black, Color(0xFFF0F0F0),  // Light gray
				Color(0xFF000000),  // Pure black
				Color(0xFFFFFFFF)   // Pure white
			)

			// Find the first color that meets contrast requirements
			return lightColors.first { contrastingColor ->
				calculateContrastRatio(baseColor, contrastingColor) >= minContrastRatio
			}
		}

		/**
		 * Calculate relative luminance of a color according to WCAG 2 standards
		 * @param color The color to calculate luminance for
		 * @return Relative luminance value between 0 and 1
		 */
		private fun getLuminance(color: Color): Double {
			val r = color.red
			val g = color.green
			val b = color.blue

			val rSRGB = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
			val gSRGB = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
			val bSRGB = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

			return 0.2126 * rSRGB + 0.7152 * gSRGB + 0.0722 * bSRGB
		}

		/**
		 * Calculate contrast ratio between two colors
		 * @param color1 First color
		 * @param color2 Second color
		 * @return Contrast ratio as a double
		 */
		private fun calculateContrastRatio(color1: Color, color2: Color): Double {
			val lum1 = getLuminance(color1)
			val lum2 = getLuminance(color2)

			val lighter = max(lum1, lum2)
			val darker = min(lum1, lum2)

			return (lighter + 0.05) / (darker + 0.05)
		}
	}

	var bgColor by remember { mutableStateOf(tertiaryColor) }
	var contentColor by remember { mutableStateOf(onTertiary) }

	Box(
		Modifier.background(bgColor, MaterialTheme.shapes.extraSmall).clip(MaterialTheme.shapes.extraSmall)
	) {
		CompositionLocalProvider(
			LocalContentColor provides contentColor
		) {
			Row(
				Modifier.width(180.dp),
				horizontalArrangement = Arrangement.End,
				verticalAlignment = Alignment.CenterVertically
			) {
				AsyncImage(
					model = track.albumArtUri,
					contentDescription = track.album,
					modifier = Modifier.fillMaxHeight(),
					onState = { state ->
						if (state is AsyncImagePainter.State.Success) {
							val bm = state.result.image.toBitmap()
							bgColor = DominantColorExtractor().extract(bm)
							contentColor = ContrastingColorGenerator().generate(bgColor)
						}
					})

				Spacer(Modifier.size(8.dp))

				Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.Center) {
					Text(
						text = track.title,
						style = MaterialTheme.typography.labelMedium,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
					Text(
						text = track.artist,
						style = MaterialTheme.typography.labelSmall,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
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