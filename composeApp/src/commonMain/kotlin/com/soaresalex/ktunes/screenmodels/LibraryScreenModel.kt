package com.soaresalex.ktunes.screenmodels

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.data.models.Track
import com.soaresalex.ktunes.data.repository.LibraryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryScreenModel(
    private val libraryRepository: LibraryRepository
) : ScreenModel {
    // Expose repository's StateFlows directly
    val tracks: StateFlow<List<Track>> = libraryRepository.tracks
    val albums: StateFlow<List<Album>> = libraryRepository.albums
    val artists: StateFlow<List<Artist>> = libraryRepository.artists

    // Loading state with combined loading logic
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        screenModelScope.launch {
            _isLoading.value = true
            try {
                // Refresh all library contents
                libraryRepository.refreshTracks()
                libraryRepository.refreshAlbums()
                libraryRepository.refreshArtists()
            } catch (e: Exception) {
                // Handle or log error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Advanced search functionality using combine
    fun searchLibrary(query: String): StateFlow<SearchResult> {
        return combine(
            tracks, albums, artists
        ) { trackList, albumList, artistList ->
            SearchResult(tracks = trackList.filter {
                it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
            }, albums = albumList.filter {
                it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
            }, artists = artistList.filter {
                it.name.contains(query, ignoreCase = true)
            })
        }.stateIn(
            scope = screenModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = SearchResult()
        )
    }

    // Wrapper for search results
    data class SearchResult(
        val tracks: List<Track> = emptyList(),
        val albums: List<Album> = emptyList(),
        val artists: List<Artist> = emptyList()
    )
}