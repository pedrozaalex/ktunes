package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction for a playback service that handles playing audio tracks.
 */
abstract class PlaybackService(
	protected open val playQueueService: PlayQueueService
) {
	/**
	 * Plays a song that is already in the queue.
	 *
	 * @param index The index of the queued track to play.
	 */
	abstract suspend fun playQueued(index: Int)

	/**
	 * Plays the specified track in the context of a list of tracks.
	 */
	open suspend fun playFromTrackList(
		context: List<Track> = emptyList(), index: Int = 0, description: String = "your library"
	) {
		playQueueService.clearQueue()
		playQueueService.addTracks(context)
		playQueueService.setCurrentTrack(index)
	}

	suspend fun playNext() {
		val nextIndex = (playQueueService.currentTrackIndex.value ?: -1) + 1
		if (nextIndex < playQueueService.queue.value.size) {
			playQueueService.setCurrentTrack(nextIndex)
			playQueued(nextIndex)
		} else {
			stop()
		}
	}

	suspend fun playPrevious() {
		if (progress.value > SEEK_BACK_THRESHOLD) {
			seekTo(0)
			return
		}

		val previousIndex = (playQueueService.currentTrackIndex.value ?: 1) - 1
		if (previousIndex >= 0) {
			playQueueService.setCurrentTrack(previousIndex)
			playQueued(previousIndex)
		}
	}

	/**
	 * Pauses the currently playing track.
	 */
	abstract suspend fun pause()

	/**
	 * Resumes playback of the currently paused track.
	 */
	abstract suspend fun resume()

	/**
	 * Stops playback and releases any resources.
	 */
	abstract suspend fun stop()

	/**
	 * Seeks to a specific position within the current track.
	 *
	 * @param position The position to seek to, in milliseconds.
	 */
	abstract suspend fun seekTo(position: Long)

	/**
	 * The currently playing track.
	 */
	abstract val currentTrack: StateFlow<Track?>

	/**
	 * Indicates whether a track is currently playing.
	 */
	abstract val isPlaying: StateFlow<Boolean>

	/**
	 * A flow emitting the current playback progress of the track in milliseconds.
	 */
	abstract val progress: StateFlow<Long>

	/**
	 * A flow emitting the current audio level (e.g., for visualization).
	 * The specific format of this data is implementation-dependent.
	 */
	abstract val audioLevel: StateFlow<Float>

	companion object {
		const val SEEK_BACK_THRESHOLD = 5000
	}
}
