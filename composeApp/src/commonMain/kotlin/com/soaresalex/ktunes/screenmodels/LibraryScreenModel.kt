package com.soaresalex.ktunes.screenmodels

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.Settings
import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.data.models.Track
import com.soaresalex.ktunes.data.repository.LibraryRepository
import com.soaresalex.ktunes.data.service.PlayQueueService
import com.soaresalex.ktunes.data.service.PlaybackService
import com.soaresalex.ktunes.ui.navigation.History
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.*
import javax.swing.SortOrder

class LibraryScreenModel(
	val settings: Settings,
	val history: History,
	private val libraryRepository: LibraryRepository,
	private val playbackService: PlaybackService,
	private val playQueueService: PlayQueueService
) : ScreenModel {
	enum class TrackSortBy {
		TITLE, ARTIST, ALBUM, DURATION
	}

	enum class AlbumSortBy {
		TITLE, ARTIST, RELEASE_YEAR, TRACK_COUNT
	}

	enum class ArtistSortBy {
		NAME, TRACK_COUNT, ALBUM_COUNT
	}

	// Mutable state for sorting and filtering
	private val _trackSortBy = MutableStateFlow(TrackSortBy.TITLE)
	private val _trackSortOrder = MutableStateFlow(SortOrder.ASCENDING)
	private val _trackNameFilter = MutableStateFlow<String?>(null)

	private val _albumSortBy = MutableStateFlow(AlbumSortBy.TITLE)
	private val _albumSortOrder = MutableStateFlow(SortOrder.ASCENDING)
	private val _albumNameFilter = MutableStateFlow<String?>(null)

	private val _artistSortBy = MutableStateFlow(ArtistSortBy.NAME)
	private val _artistSortOrder = MutableStateFlow(SortOrder.ASCENDING)
	private val _artistNameFilter = MutableStateFlow<String?>(null)

	// Public state flows for sorting and filtering
	val trackSortBy: StateFlow<TrackSortBy> = _trackSortBy.asStateFlow()
	val trackSortOrder: StateFlow<SortOrder> = _trackSortOrder.asStateFlow()
	val albumSortBy: StateFlow<AlbumSortBy> = _albumSortBy.asStateFlow()
	val albumSortOrder: StateFlow<SortOrder> = _albumSortOrder.asStateFlow()
	val artistSortBy: StateFlow<ArtistSortBy> = _artistSortBy.asStateFlow()
	val artistSortOrder: StateFlow<SortOrder> = _artistSortOrder.asStateFlow()

	val filteredSortedTracks = libraryRepository.tracks.combine(_trackNameFilter) { tracks, filter ->
		filter?.let {
			tracks.filter { it.title.contains(filter, ignoreCase = true) }
		} ?: tracks
	}.let {
		combine(it, _trackSortBy, _trackSortOrder) { tracks, sortBy, sortOrder ->
			sortTracks(tracks, sortBy, sortOrder)
		}
	}

	val filteredSortedAlbums = libraryRepository.albums.combine(_albumNameFilter) { albums, filter ->
		filter?.let {
			albums.filter { it.title.contains(filter, ignoreCase = true) }
		} ?: albums
	}.map { albums ->
		sortAlbums(albums, _albumSortBy.value, _albumSortOrder.value)
	}

	val filteredSortedArtists = libraryRepository.artists.combine(_artistNameFilter) { artists, filter ->
		filter?.let {
			artists.filter { it.name.contains(filter, ignoreCase = true) }
		} ?: artists
	}.map { artists ->
		sortArtists(artists, _artistSortBy.value, _artistSortOrder.value)
	}

	private val _isLoading = MutableStateFlow(false)
	val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

	init {
		loadLibrary()
	}

	fun loadLibrary() {
		screenModelScope.launch {
			_isLoading.value = true
			try {
				libraryRepository.refreshTracks()
				libraryRepository.refreshAlbums()
				libraryRepository.refreshArtists()
			} catch (e: Exception) {
				// Consider using a proper logging mechanism instead of printStackTrace
				e.printStackTrace()
			} finally {
				_isLoading.value = false
			}
		}
	}

	// Sorting helper methods
	private fun sortTracks(
		tracks: List<Track>, sortBy: TrackSortBy, sortOrder: SortOrder
	): List<Track> {
		val collator = Collator.getInstance(Locale.getDefault()).apply {
			strength = Collator.PRIMARY // Ignore case and diacritics
		}

		return when (sortBy) {
			TrackSortBy.TITLE -> tracks.sortedWith(compareBy(collator) { it.title })
			TrackSortBy.ARTIST -> tracks.sortedWith(compareBy(collator) { it.artist })
			TrackSortBy.ALBUM -> tracks.sortedWith(compareBy(collator) { it.album })
			TrackSortBy.DURATION -> tracks.sortedBy { it.duration }
		}.let {
			if (sortOrder == SortOrder.DESCENDING) it.reversed() else it
		}
	}

	private fun sortAlbums(
		albums: List<Album>, sortBy: AlbumSortBy, sortOrder: SortOrder
	): List<Album> = when (sortBy) {
		AlbumSortBy.TITLE -> albums.sortedBy { it.title }
		AlbumSortBy.ARTIST -> albums.sortedBy { it.artist }
		AlbumSortBy.RELEASE_YEAR -> albums.sortedBy { it.releaseYear }
		AlbumSortBy.TRACK_COUNT -> albums.sortedBy { it.trackCount }
	}.let {
		if (sortOrder == SortOrder.DESCENDING) it.reversed() else it
	}

	private fun sortArtists(
		artists: List<Artist>, sortBy: ArtistSortBy, sortOrder: SortOrder
	): List<Artist> = when (sortBy) {
		ArtistSortBy.NAME -> artists.sortedBy { it.name }
		ArtistSortBy.TRACK_COUNT -> artists.sortedBy { it.trackCount }
		ArtistSortBy.ALBUM_COUNT -> artists.sortedBy { it.albumCount }
	}.let {
		if (sortOrder == SortOrder.DESCENDING) it.reversed() else it
	}

	// Methods to update sorting and filtering
	fun updateTrackSort(sortBy: TrackSortBy, sortOrder: SortOrder = _trackSortOrder.value) {
		_trackSortBy.value = sortBy
		_trackSortOrder.value = sortOrder
	}

	fun updateAlbumSort(sortBy: AlbumSortBy, sortOrder: SortOrder = _albumSortOrder.value) {
		_albumSortBy.value = sortBy
		_albumSortOrder.value = sortOrder
	}

	fun updateArtistSort(sortBy: ArtistSortBy, sortOrder: SortOrder = _artistSortOrder.value) {
		_artistSortBy.value = sortBy
		_artistSortOrder.value = sortOrder
	}

	fun setTrackNameFilter(filter: String?) {
		_trackNameFilter.value = filter
	}

	fun setAlbumNameFilter(filter: String?) {
		_albumNameFilter.value = filter
	}

	fun setArtistNameFilter(filter: String?) {
		_artistNameFilter.value = filter
	}

	private fun executePlaybackAction(action: suspend PlaybackService.() -> Unit) {
		screenModelScope.launch {
			playbackService.action()
		}
	}

	// Put current tracks into the play queue and start playback
	fun onLibraryTrackClick(clicked: Track) {
		screenModelScope.launch {
			val tracks = filteredSortedTracks.first()

			val index = tracks.indexOf(clicked)

			if (index == -1) {
				throw IllegalArgumentException("Track not found in library")
			}

			executePlaybackAction { playFromTrackList(tracks, index) }
		}
	}

	fun onPause() {
		executePlaybackAction { pause() }
	}

	fun onResume() {
		executePlaybackAction { resume() }
	}

	fun onStop() {
		executePlaybackAction { stop() }
	}
}