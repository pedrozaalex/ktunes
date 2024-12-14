package com.soaresalex.ktunes.screenmodels

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.Settings
import com.soaresalex.ktunes.data.models.Track
import com.soaresalex.ktunes.data.repository.LibraryRepository
import com.soaresalex.ktunes.data.service.PlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryScreenModel(
	val repo: LibraryRepository,
	val settings: Settings,
	private val playbackService: PlaybackService
) : ScreenModel {
	private val _isLoading = MutableStateFlow(false)
	val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

	init {
		loadLibrary()
	}

	fun loadLibrary() {
		screenModelScope.launch {
			_isLoading.value = true
			try { // Refresh all library contents
				repo.refreshTracks()
				repo.refreshAlbums()
				repo.refreshArtists()
			} catch (e: Exception) { // Handle or log error
				e.printStackTrace()
			} finally {
				_isLoading.value = false
			}
		}
	}

	fun onPlayTrack(track: Track) {
		screenModelScope.launch {
			playbackService.play(track)
		}
	}

	fun onPauseTrack() {
		screenModelScope.launch {
			playbackService.pause()
		}
	}

	fun onResumeTrack() {
		screenModelScope.launch {
			playbackService.resume()
		}
	}

	fun onStopPlayback() {
		screenModelScope.launch {
			playbackService.stop()
		}
	}
}