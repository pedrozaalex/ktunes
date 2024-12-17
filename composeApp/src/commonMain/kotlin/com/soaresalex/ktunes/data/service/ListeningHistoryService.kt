package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstract class defining the behavior of a listening history service.
 * This service is responsible for tracking recently played tracks,
 * allowing retrieval and management of the listening history.
 */
abstract class ListeningHistoryService {

	/**
	 * A [kotlinx.coroutines.flow.StateFlow] emitting the current state of the listening history.
	 * The list represents the recently played tracks, ordered from most
	 * recent to least recent.
	 */
	abstract val history: StateFlow<List<Track>>

	/**
	 * The maximum number of tracks to store in the listening history.
	 */
	abstract val capacity: Int

	/**
	 * Adds a track to the listening history. If adding the track causes
	 * the history to exceed its capacity, the oldest track will be removed.
	 *
	 * @param track The track to be added to the history.
	 */
	abstract fun addTrackToHistory(track: Track)

	/**
	 * Retrieves the listening history.
	 *
	 * @return A list of recently played tracks, ordered from most recent
	 *         to least recent.
	 */
	abstract fun getHistory(): List<Track>

	/**
	 * Clears the entire listening history.
	 */
	abstract fun clearHistory()
}