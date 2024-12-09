package com.soaresalex.ktunes.viewmodels

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.soaresalex.ktunes.data.Album
import com.soaresalex.ktunes.data.Artist
import com.soaresalex.ktunes.data.Playlist
import com.soaresalex.ktunes.data.Track
import com.soaresalex.ktunes.repositories.LibraryRepository
import com.soaresalex.ktunes.repositories.LocalFilesystemLibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data classes to represent library entities
data class TrackViewModel(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val artworkUrl: String? = null,
    val fileUrl: String
)

fun Track.toViewModel(): TrackViewModel {
    return TrackViewModel(
        id = title.hashCode().toString(),
        title = title,
        artist = artist,
        album = "Album",
        duration = 0,
        artworkUrl = null,
        fileUrl = "file://$title.mp3"
    )
}

data class AlbumViewModel(
    val id: String,
    val title: String,
    val artist: String,
    val releaseYear: Int,
    val artworkUrl: String? = null,
    val tracks: List<TrackViewModel>
)

fun Album.toViewModel(): AlbumViewModel {
    return AlbumViewModel(
        id = "$artist-$title",
        title = title,
        artist = artist,
        releaseYear = 2021,
        artworkUrl = null,
        tracks = tracks.map { it.toViewModel() }
    )
}

data class ArtistViewModel(
    val id: String, val name: String, val artworkUrl: String? = null, val albums: List<AlbumViewModel>
)

fun Artist.toViewModel(): ArtistViewModel {
    return ArtistViewModel(
        id = name.hashCode().toString(),
        name = name,
        artworkUrl = null,
        albums = albums.map { it.toViewModel() }
    )
}

data class PlaylistViewModel(
    val id: String,
    val name: String,
    val creator: String,
    val description: String? = null,
    val artworkUrl: String? = null,
    val tracks: List<TrackViewModel>
)

fun Playlist.toViewModel(): PlaylistViewModel {
    return PlaylistViewModel(
        id = name.hashCode().toString(),
        name = name,
        creator = creator,
        description = description,
        artworkUrl = null,
        tracks = tracks.map { it.toViewModel() }
    )
}

class LibraryViewModel() : ScreenModel {
    companion object RepositoryProvider {
        private var repository: LibraryRepository = LocalFilesystemLibraryRepository()

        fun provide(): LibraryRepository = repository
        fun update(new: LibraryRepository) {
            repository = new
        }
    }

    // Existing state flows...
    private val _tracks = MutableStateFlow<List<TrackViewModel>>(emptyList())
    val tracks: StateFlow<List<TrackViewModel>> = _tracks.asStateFlow()

    private val _albums = MutableStateFlow<List<AlbumViewModel>>(emptyList())
    val albums: StateFlow<List<AlbumViewModel>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<ArtistViewModel>>(emptyList())
    val artists: StateFlow<List<ArtistViewModel>> = _artists.asStateFlow()

    private val _playlists = MutableStateFlow<List<PlaylistViewModel>>(emptyList())
    val playlists: StateFlow<List<PlaylistViewModel>> = _playlists.asStateFlow()

    // Loading and error states...
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Track the current repository
    private val _currentRepository = MutableStateFlow<LibraryRepository?>(null)
    val currentRepository: StateFlow<LibraryRepository?> = _currentRepository.asStateFlow()

    init {
        // Initial repository setup
        val repository = provide()
        _currentRepository.value = repository
        refreshLibrary()

        // Observe repository changes
        observeRepositoryChanges()
    }

    // Observe repository changes
    private fun observeRepositoryChanges() {
        screenModelScope.launch {
            _currentRepository.collect { newRepository ->
                if (newRepository != _currentRepository.value) {
                    _currentRepository.value = newRepository
                    refreshLibrary()
                }
            }
        }
    }

    // Refresh library method remains the same as in previous implementation
    fun refreshLibrary() {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val repository = currentRepository.value ?: return@launch
                _tracks.value = repository.getAllTracks().map(Track::toViewModel)
                _albums.value = repository.getAllAlbums().map(Album::toViewModel)
                _artists.value = repository.getAllArtists().map(Artist::toViewModel)
                _playlists.value = repository.getAllPlaylists().map(Playlist::toViewModel)
            } catch (e: Exception) {
                _error.value = "Failed to load library: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Comprehensive library search
    private fun searchLibrary(query: String) {
        if (query.isBlank()) {
            refreshLibrary()
            return
        }

        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val repository = currentRepository.value ?: return@launch
            try {
                _tracks.value = repository.searchTracks(query).map(Track::toViewModel)
                _albums.value = repository.searchAlbums(query).map(Album::toViewModel)
                _artists.value = repository.searchArtists(query).map(Artist::toViewModel)
                _playlists.value = repository.searchPlaylists(query).map(Playlist::toViewModel)
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Get a track by ID
    fun getTrackById(id: String): TrackViewModel? = tracks.value.find { it.id == id }

    // Create a new playlist
    fun createPlaylist(name: String, description: String? = null) {
        screenModelScope.launch {
            // Implementation depends on your specific repository
            // Typically involves calling a method on the repository to persist the playlist
        }
    }

    // Add track to playlist
    fun addTrackToPlaylist(trackId: String, playlistId: String) {
        screenModelScope.launch {
            // Implementation depends on your specific repository
        }
    }

    // Remove track from playlist
    fun removeTrackFromPlaylist(trackId: String, playlistId: String) {
        screenModelScope.launch {
            // Implementation depends on your specific repository
        }
    }

    // Sorting methods
    fun sortTracksByTitle() {
        _tracks.value = tracks.value.sortedBy { it.title }
    }

    fun sortAlbumsByReleaseYear() {
        _albums.value = albums.value.sortedByDescending { it.releaseYear }
    }

    fun sortArtistsByName() {
        _artists.value = artists.value.sortedBy { it.name }
    }

    // Provide filtered lists
    fun getRecentTracks(limit: Int = 10): List<TrackViewModel> {
        // Implement logic to get recent tracks, e.g., by last played date
        return tracks.value.take(limit)
    }

    fun getFavoriteTracks(limit: Int = 10): List<TrackViewModel> {
        // Implement logic to get favorite tracks
        return tracks.value.take(limit)
    }
}
