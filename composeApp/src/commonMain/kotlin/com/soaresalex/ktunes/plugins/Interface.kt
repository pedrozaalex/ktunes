package com.soaresalex.ktunes.plugins

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Represents a configuration option for a plugin
 */
@Serializable
data class PluginConfigOption(
    val key: String,
    val type: ConfigOptionType,
    val defaultValue: String,
    val description: String
)

/**
 * Enum for different configuration option types
 */
enum class ConfigOptionType {
    STRING,
    INTEGER,
    BOOLEAN,
    FLOAT,
}

/**
 * Represents metadata about an audio track
 */
@Serializable
data class AudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val duration: Long, // Duration in milliseconds
    val thumbnailUrl: String? = null,
    val streamUrl: String
)

/**
 * Core interface that all audio source plugins must implement
 */
interface AudioSourcePlugin {
    /**
     * Unique identifier for the plugin
     */
    val pluginId: String

    /**
     * Human-readable name of the plugin
     */
    val pluginName: String

    /**
     * Version of the plugin
     */
    val version: String

    /**
     * Description of the plugin's functionality
     */
    val description: String

    /**
     * List of configuration options this plugin supports
     */
    val configOptions: List<PluginConfigOption>

    /**
     * Update plugin configuration
     * @param config Map of configuration key-value pairs
     * @return Boolean indicating if configuration was successful
     */
    fun configure(config: Map<String, Any>): Boolean

    /**
     * Search for tracks based on a query
     * @param query Search terms
     * @param limit Maximum number of tracks to return
     * @return List of matching AudioTracks
     */
    suspend fun searchTracks(query: String, limit: Int = 50): List<AudioTrack>

    /**
     * Get track details by ID
     * @param trackId Unique identifier of the track
     * @return AudioTrack details
     */
    suspend fun getTrackDetails(trackId: String): AudioTrack?

    /**
     * Stream audio data for a specific track
     * @param trackId Unique identifier of the track
     * @return Flow of ByteArray representing audio chunks
     */
    suspend fun streamAudio(trackId: String): Flow<ByteArray>

    /**
     * Get playback controls for the current track
     */
    suspend fun getPlaybackControls(): PlaybackControls

    /**
     * Perform any necessary cleanup when the plugin is unloaded
     */
    suspend fun cleanup()
}

/**
 * Represents playback controls for an audio source
 */
interface PlaybackControls {
    /**
     * Play the current track
     */
    suspend fun play()

    /**
     * Pause the current track
     */
    suspend fun pause()

    /**
     * Stop playback
     */
    suspend fun stop()

    /**
     * Seek to a specific position in the track
     * @param positionMs Position in milliseconds
     */
    suspend fun seek(positionMs: Long)

    /**
     * Get current playback state
     */
    suspend fun getPlaybackState(): PlaybackState
}

/**
 * Represents the current state of playback
 */
@Serializable
data class PlaybackState(
    val isPlaying: Boolean,
    val currentPosition: Long,
    val duration: Long
)