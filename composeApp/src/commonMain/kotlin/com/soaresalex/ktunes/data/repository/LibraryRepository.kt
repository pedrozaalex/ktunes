package com.soaresalex.ktunes.data.repository

import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.data.models.Track
import com.soaresalex.ktunes.data.service.MediaService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent

/**
 * Reactive repository for managing media library operations
 * Provides real-time updates through StateFlow
 */
class LibraryRepository(
	private val mediaService: MediaService
) : KoinComponent {
	private val _tracks = MutableStateFlow<List<Track>>(emptyList())
	val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

	private val _albums = MutableStateFlow<List<Album>>(emptyList())
	val albums: StateFlow<List<Album>> = _albums.asStateFlow()

	private val _artists = MutableStateFlow<List<Artist>>(emptyList())
	val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

	suspend fun refreshTracks() {
		try {
			val fetchedTracks = mediaService.getTracks()
			_tracks.value = fetchedTracks
		} catch (e: Exception) { // Consider using a logging framework or error handling mechanism
			_tracks.value = emptyList()
		}
	}

	suspend fun refreshAlbums() {
		try {
			val fetchedAlbums = mediaService.getAlbums()
			_albums.value = fetchedAlbums
		} catch (e: Exception) {
			_albums.value = emptyList()
		}
	}

	suspend fun refreshArtists() {
		try {
			val fetchedArtists = mediaService.getArtists()
			_artists.value = fetchedArtists
		} catch (e: Exception) {
			_artists.value = emptyList()
		}
	}

}