package com.soaresalex.ktunes.data.service

import co.touchlab.kermit.Logger
import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.freedesktop.gstreamer.Bus
import org.freedesktop.gstreamer.Gst
import org.freedesktop.gstreamer.GstObject
import org.freedesktop.gstreamer.State
import org.freedesktop.gstreamer.elements.PlayBin
import java.io.File
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

	private val job = Job()
	private val scope = CoroutineScope(coroutineContext + job)

	private var playbin: PlayBin = PlayBin("audio_player")

	init {
		Gst.init("audio_player")
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
	}

	fun dispose() {
		playbin.dispose()
		job.cancel()
	}
}