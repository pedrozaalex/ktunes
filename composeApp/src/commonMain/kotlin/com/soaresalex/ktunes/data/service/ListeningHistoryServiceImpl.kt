package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList

/**
 * Concrete implementation of [ListeningHistoryService] using a [java.util.LinkedList] to manage the history.
 */
class ListeningHistoryServiceImpl(override val capacity: Int) : ListeningHistoryService() {

	private val _history = MutableStateFlow<List<Track>>(LinkedList())
	override val history: StateFlow<List<Track>> = _history.asStateFlow()

	init {
		require(capacity > 0) { "Capacity must be greater than 0" }
	}

	override fun addTrackToHistory(track: Track) {
		val updatedHistory = _history.value.toMutableList()
		updatedHistory.remove(track)
		updatedHistory.add(0, track)
		if (updatedHistory.size > capacity) {
			updatedHistory.removeLast()
		}
		_history.value = updatedHistory
	}

	override fun getHistory(): List<Track> {
		return _history.value
	}

	override fun clearHistory() {
		_history.value = emptyList()
	}
}