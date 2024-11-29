package com.soaresalex.ktunes.viewmodels

import androidx.lifecycle.ViewModel
import com.soaresalex.ktunes.data.SpotifyApiClient
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Represents metadata for a playable audio track
 */
interface AudioTrack {
    val id: String
    val title: String
    val artist: String
    val album: String
    val duration: Long // in milliseconds
    val artworkUrl: String?
}

/**
 * Contract for audio playback services
 */
interface AudioPlaybackService {
    /**
     * Get the current playing state of the audio player
     */
    suspend fun getCurrentPlayerState(): PlayerState?

    /**
     * Control methods for playback
     */
    suspend fun play()
    suspend fun pause()
    suspend fun nextTrack()
    suspend fun previousTrack()
    suspend fun seekTo(positionMs: Long)

    /**
     * Current playback status and track information
     */
    val currentTrack: AudioTrack?
    val isPlaying: Boolean
    val currentPosition: Long
}

/**
 * Represents the current state of the audio player
 */
data class PlayerState(
    val track: AudioTrack?,
    val isPlaying: Boolean,
    val currentPosition: Long
)

/**
 * ViewModel for managing audio playback across different services
 */
class PlaybackViewModel(
    private val servicePluginRegistry: ServicePluginRegistry
) : ViewModel() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var currentService: AudioPlaybackService? = null

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val currentServiceId: String? = null
    )

    // Switch to a specific playback service by its plugin ID
    fun switchService(serviceId: String) {
        val plugin = servicePluginRegistry.getPlugin(serviceId)
        currentService = plugin.createPlaybackService()
        refresh()
    }

    fun refresh() {
        coroutineScope.launch {
            try {
                val service = currentService ?: return@launch

                val playerState = service.getCurrentPlayerState()

                if (playerState?.track == null) {
                    _currentTrack.value = null
                    _playbackState.update {
                        it.copy(
                            isPlaying = false,
                            currentServiceId = currentService?.javaClass?.simpleName
                        )
                    }
                } else {
                    _currentTrack.value = playerState.track
                    _playbackState.update {
                        it.copy(
                            isPlaying = playerState.isPlaying,
                            currentServiceId = currentService?.javaClass?.simpleName
                        )
                    }
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

    fun previousTrack() = performPlaybackAction { it.previousTrack() }
    fun nextTrack() = performPlaybackAction { it.nextTrack() }
    fun resume() = performPlaybackAction { it.play() }
    fun pause() = performPlaybackAction { it.pause() }

    private fun performPlaybackAction(action: suspend (AudioPlaybackService) -> Unit) {
        coroutineScope.launch {
            val service = currentService ?: return@launch
            try {
                updatePlaybackStateOptimistically(!playbackState.value.isPlaying)
                action(service)
                refresh()
            } catch (e: Exception) {
                handlePlaybackError(e, "Playback action failed")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup resources if needed
    }
}
