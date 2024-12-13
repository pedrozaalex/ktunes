package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction for a playback service that handles playing audio tracks.
 */
interface PlaybackService {
	/**
	 * The currently playing track.
	 */
	val currentTrack: StateFlow<Track?>

	/**
	 * Indicates whether a track is currently playing.
	 */
	val isPlaying: StateFlow<Boolean>

	/**
	 * Plays the specified track.
	 *
	 * @param track The track to be played.
	 */
	suspend fun play(track: Track)

	/**
	 * Pauses the currently playing track.
	 */
	suspend fun pause()

	/**
	 * Resumes playback of the currently paused track.
	 */
	suspend fun resume()

	/**
	 * Stops playback and releases any resources.
	 */
	suspend fun stop()

	/**
	 * Seeks to a specific position within the current track.
	 *
	 * @param position The position to seek to, in milliseconds.
	 */
	suspend fun seekTo(position: Long)

	/**
	 * A flow emitting the current playback progress of the track in milliseconds.
	 */
	val progress: StateFlow<Long>

	/**
	 * A flow emitting the current audio level (e.g., for visualization).
	 * The specific format of this data is implementation-dependent.
	 */
	val audioLevel: StateFlow<Float>
}
