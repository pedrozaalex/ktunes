package com.soaresalex.ktunes.data.service

import co.touchlab.kermit.Logger
import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.freedesktop.gstreamer.*
import org.freedesktop.gstreamer.elements.PlayBin
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * Implementation of [PlaybackService] using GStreamer's [PlayBin] element for audio playback.
 */
class GstreamerPlaybackService : PlaybackService {
	private val coroutineContext: CoroutineContext = Dispatchers.IO

	private val _currentTrack = MutableStateFlow<Track?>(null)
	override val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

	private val _isPlaying = MutableStateFlow(false)
	override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

	private val _progress = MutableStateFlow(0L)
	override val progress: StateFlow<Long> = _progress.asStateFlow()

	private val _audioLevel = MutableStateFlow(0f)
	override val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

	private val job = Job()
	private val scope = CoroutineScope(coroutineContext + job)

	private var playbin: PlayBin

	init {
		Gst.init("audio_player")

		playbin = PlayBin("playbin")

		playbin.bus.connect(object : Bus.EOS {
			override fun endOfStream(source: GstObject?) {
				scope.launch {
					_isPlaying.value = false
					_currentTrack.value = null
				}
			}
		})

		playbin.bus.connect(object : Bus.ERROR {
			override fun errorMessage(source: org.freedesktop.gstreamer.GstObject, code: Int, message: String) {
				Logger.e { "Error received from element $source: $code - $message" }
				scope.launch {
					_isPlaying.value = false
					_currentTrack.value = null
				}
			}
		})

		// Periodically update progress and audio level - adjust the interval as needed
		scope.launch {
			while (isActive) {
				if (_isPlaying.value) {
					_progress.value = playbin.queryPosition(TimeUnit.MILLISECONDS)
					// Placeholder for audio level extraction - replace with actual implementation
					// You might need a separate element in the pipeline to analyze audio levels
					// For now, we just emit a random float between 0 and 1
					_audioLevel.value = Math.random().toFloat()
				}
				delay(100)
			}
		}
	}

	override suspend fun play(track: Track) = withContext(coroutineContext) {
		if (_isPlaying.value && currentTrack.value == track) {
			resume()
			return@withContext
		}

		stop()

		_currentTrack.value = track
		val file = track.fileUri?.let { File(it) }
		if (file != null && file.exists()) {
			playbin.setURI(file.toURI())
			playbin.play()
			_isPlaying.value = true
		} else {
			Logger.e { "File does not exist: ${file?.absolutePath}" }
			_currentTrack.value = null
		}
	}

	override suspend fun pause() = withContext(coroutineContext) {
		if (playbin.state == State.PLAYING) {
			playbin.pause()
			_isPlaying.value = false
		}
	}

	override suspend fun resume() = withContext(coroutineContext) {
		if (playbin.state == State.PAUSED) {
			playbin.play()
			_isPlaying.value = true
		}
	}

	override suspend fun stop() = withContext(coroutineContext) {
		playbin.stop()
		_isPlaying.value = false
		_currentTrack.value = null
		_progress.value = 0
	}

	override suspend fun seekTo(position: Long) = withContext(coroutineContext) {
		if (playbin.state == State.PLAYING || playbin.state == State.PAUSED) {
			playbin.seek(ClockTime.fromMillis(position))
			_progress.value = position
		}
	}

	fun dispose() {
		playbin.dispose()
		job.cancel()
	}
}