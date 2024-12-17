package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Collections
import java.util.LinkedList

/**
 * Concrete implementation of [PlayQueueService] using a [LinkedList] to manage the queue.
 */
class PlayQueueServiceImpl : PlayQueueService() {

	private val _queue = MutableStateFlow<List<Track>>(LinkedList())
	override val queue: StateFlow<List<Track>> = _queue.asStateFlow()

	private val _currentTrackIndex = MutableStateFlow<Int?>(null)
	override val currentTrackIndex: StateFlow<Int?> = _currentTrackIndex.asStateFlow()

	override fun addTrack(track: Track) {
		val updatedQueue = _queue.value.toMutableList()
		updatedQueue.add(track)
		_queue.value = updatedQueue
		if (_currentTrackIndex.value == null) {
			_currentTrackIndex.value = 0
		}
	}

	override fun addTracks(tracks: List<Track>) {
		val updatedQueue = _queue.value.toMutableList()
		updatedQueue.addAll(tracks)
		_queue.value = updatedQueue
		if (_currentTrackIndex.value == null) {
			_currentTrackIndex.value = 0
		}
	}

	override fun addTrackAt(index: Int, track: Track) {
		val updatedQueue = _queue.value.toMutableList()
		if (index in 0..updatedQueue.size) {
			updatedQueue.add(index, track)
			_queue.value = updatedQueue
			if (_currentTrackIndex.value == null) {
				_currentTrackIndex.value = index
			} else if (index <= _currentTrackIndex.value!!) {
				_currentTrackIndex.value = _currentTrackIndex.value!! + 1
			}
		}
	}

	override fun removeTrack(track: Track): Boolean {
		val updatedQueue = _queue.value.toMutableList()
		val removed = updatedQueue.remove(track)
		if (removed) {
			val removedIndex = _queue.value.indexOf(track)
			_queue.value = updatedQueue
			if (removedIndex == _currentTrackIndex.value) {
				_currentTrackIndex.value = null
			} else if (removedIndex < _currentTrackIndex.value!!) {
				_currentTrackIndex.value = _currentTrackIndex.value!! - 1
			}
		}
		return removed
	}

	override fun removeTrackAt(index: Int): Track? {
		val updatedQueue = _queue.value.toMutableList()
		if (index in 0 until updatedQueue.size) {
			val removedTrack = updatedQueue.removeAt(index)
			_queue.value = updatedQueue
			if (index == _currentTrackIndex.value) {
				_currentTrackIndex.value = null
			} else if (index < _currentTrackIndex.value!!) {
				_currentTrackIndex.value = _currentTrackIndex.value!! - 1
			}
			return removedTrack
		}
		return null
	}

	override fun clearQueue() {
		_queue.value = emptyList()
		_currentTrackIndex.value = null
	}

	override fun moveTrack(from: Int, to: Int): Boolean {
		val updatedQueue = _queue.value.toMutableList()
		if (from in 0 until updatedQueue.size && to in 0..updatedQueue.size) {
			Collections.swap(updatedQueue, from, to)
			_queue.value = updatedQueue

			when {
				from == _currentTrackIndex.value -> _currentTrackIndex.value = to
				from < _currentTrackIndex.value!! && to >= _currentTrackIndex.value!! ->
					_currentTrackIndex.value = _currentTrackIndex.value!! - 1

				from > _currentTrackIndex.value!! && to <= _currentTrackIndex.value!! ->
					_currentTrackIndex.value = _currentTrackIndex.value!! + 1
			}

			return true
		}
		return false
	}

	override fun shuffle() {
		val updatedQueue = _queue.value.toMutableList()
		if (_currentTrackIndex.value != null) {
			val currentTrack = updatedQueue.removeAt(_currentTrackIndex.value!!)
			updatedQueue.shuffle()
			updatedQueue.add(0, currentTrack)
			_queue.value = updatedQueue
			_currentTrackIndex.value = 0
		} else {
			updatedQueue.shuffle()
			_queue.value = updatedQueue
		}
	}

	override fun setCurrentTrack(index: Int): Boolean {
		if (index in 0 until _queue.value.size) {
			_currentTrackIndex.value = index
			return true
		}
		return false
	}
}

