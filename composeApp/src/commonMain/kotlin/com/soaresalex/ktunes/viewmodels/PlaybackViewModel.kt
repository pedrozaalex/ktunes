package com.soaresalex.ktunes.viewmodels

import androidx.lifecycle.ViewModel
import com.soaresalex.ktunes.data.SpotifyApiClient
import kotlinx.coroutines.flow.MutableStateFlow

class PlaybackViewModel(private val spotify: SpotifyApiClient) : ViewModel() {
    private val _currentTrack = MutableStateFlow<SpotifyTrackViewModel?>(null)
    val currentTrack: StateFlow<SpotifyTrackViewModel?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    fun refresh() {
        viewModelScope.launch {
            try {
                val playerState = spotify.getCurrentPlayerState()

                if (playerState == null || playerState.item == null) {
                    _currentTrack.value = null
                    _playbackState.update { it.copy(isPlaying = false) }
                } else {
                    _currentTrack.value = SpotifyTrackViewModel.fromTrackModel(playerState.item)
                    _playbackState.update { it.copy(isPlaying = playerState.isPlaying) }
                }
            } catch (e: Exception) {
                handlePlaybackError(e, "Failed to load current track")
            } finally {
                _playbackState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updatePlaybackStateOptimistically(expectedPlayingState: Boolean) {
        _playbackState.update {
            it.copy(
                isPlaying = expectedPlayingState,
                isLoading = true,
                error = null
            )
        }
    }

    private fun handlePlaybackError(e: Exception, baseMessage: String) {
        _currentTrack.value = null
        _playbackState.update {
            it.copy(
                isPlaying = false,
                isLoading = false,
                error = "$baseMessage: ${e.message}"
            )
        }
    }

    fun previousTrack() = performPlaybackAction(spotify::previousTrack, "Failed to skip to previous track")
    fun nextTrack() = performPlaybackAction(spotify::nextTrack, "Failed to skip to next track")
    fun resume() = performPlaybackAction(spotify::resume, "Failed to resume")
    fun pause() = performPlaybackAction(spotify::pause, "Failed to pause")

    private fun performPlaybackAction(action: suspend () -> Unit, errorMessage: String) {
        viewModelScope.launch {
            try {
                updatePlaybackStateOptimistically(!playbackState.value.isPlaying)
                action()
                refresh()
            } catch (e: Exception) {
                handlePlaybackError(e, errorMessage)
            }
        }
    }
}