package com.soaresalex.ktunes.screenmodels

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.Settings
import com.soaresalex.ktunes.data.repository.LibraryRepository
import com.soaresalex.ktunes.ui.navigation.History
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryScreenModel(
	private val libraryRepository: LibraryRepository,
	val settings: Settings,
	val history: History
) : ScreenModel {
	val tracks = libraryRepository.tracks
	val albums = libraryRepository.albums
	val artists = libraryRepository.artists

	private val _isLoading = MutableStateFlow(false)
	val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

	init {
		loadLibrary()
	}

	fun loadLibrary() {
		screenModelScope.launch {
			_isLoading.value = true
			try { // Refresh all library contents
				libraryRepository.refreshTracks()
				libraryRepository.refreshAlbums()
				libraryRepository.refreshArtists()
			} catch (e: Exception) { // Handle or log error
				e.printStackTrace()
			} finally {
				_isLoading.value = false
			}
		}
	}
}