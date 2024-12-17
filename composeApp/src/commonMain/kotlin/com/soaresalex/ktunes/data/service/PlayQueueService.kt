package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstract class defining the behavior of a queue service.
 * This service manages the order of tracks to be played, providing
 * functionality for adding, removing, and manipulating the playback queue.
 */
abstract class PlayQueueService {

	/**
	 * A [StateFlow] emitting the current state of the playback queue.
	 * The list represents the ordered tracks in the queue.
	 */
	abstract val queue: StateFlow<List<Track>>

	/**
	 * A [StateFlow] emitting the index of the currently playing track
	 * within the queue. A null value indicates that no track is
	 * currently playing or the queue is empty.
	 */
	abstract val currentTrackIndex: StateFlow<Int?>

	/**
	 * Adds a single track to the end of the queue.
	 *
	 * @param track The track to be added.
	 */
	abstract fun addTrack(track: Track)

	/**
	 * Adds a list of tracks to the end of the queue.
	 *
	 * @param tracks The list of tracks to be added.
	 */
	abstract fun addTracks(tracks: List<Track>)

	/**
	 * Inserts a track at a specific position in the queue.
	 *
	 * @param index The position at which to insert the track.
	 * @param track The track to be inserted.
	 */
	abstract fun addTrackAt(index: Int, track: Track)

	/**
	 * Removes a specific track from the queue.
	 *
	 * @param track The track to be removed.
	 * @return True if the track was found and removed, false otherwise.
	 */
	abstract fun removeTrack(track: Track): Boolean

	/**
	 * Removes the track at the specified index from the queue.
	 *
	 * @param index The index of the track to be removed.
	 * @return The removed track, or null if the index is out of bounds.
	 */
	abstract fun removeTrackAt(index: Int): Track?

	/**
	 * Clears the entire playback queue.
	 */
	abstract fun clearQueue()

	/**
	 * Moves a track from one position to another within the queue.
	 *
	 * @param from The initial index of the track to be moved.
	 * @param to The target index to which the track should be moved.
	 * @return True if the track was successfully moved, false otherwise.
	 */
	abstract fun moveTrack(from: Int, to: Int): Boolean

	/**
	 * Shuffles the order of tracks in the queue randomly.
	 */
	abstract fun shuffle()

	/**
	 * Sets the currently playing track by its index in the queue.
	 *
	 * @param index The index of the track to be set as the current track.
	 * @return True if the index is valid and the current track was set,
	 *         false otherwise.
	 */
	abstract fun setCurrentTrack(index: Int): Boolean
}

