package com.soaresalex.ktunes.plugins

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.util.concurrent.TimeUnit
import java.io.IOException

class YouTubeMusicPlugin : AudioSourcePlugin {
    override val pluginId = "youtube-music"
    override val pluginName = "YouTube Music"
    override val version = "1.0.0"
    override val description = "Plugin for fetching music from YouTube Music"

    // Configuration options for the plugin
    override val configOptions = listOf(
        PluginConfigOption(
            key = "api_key",
            type = ConfigOptionType.STRING,
            defaultValue = "",
            description = "YouTube Data API Key"
        ),
        PluginConfigOption(
            key = "region",
            type = ConfigOptionType.STRING,
            defaultValue = "US",
            description = "Region for music search"
        )
    )

    // Ktor client for API requests
    private val client = HttpClient()
    private var apiKey: String = ""
    private var region: String = "US"

    @Serializable
    data class YouTubeSearchResponse(
        val items: List<YouTubeTrack>
    )

    @Serializable
    data class YouTubeTrack(
        val id: String,
        val snippet: Snippet,
        val contentDetails: ContentDetails
    )

    @Serializable
    data class Snippet(
        val title: String,
        val channelTitle: String,
        val thumbnails: Thumbnails
    )

    @Serializable
    data class Thumbnails(
        val high: Thumbnail
    )

    @Serializable
    data class Thumbnail(
        val url: String
    )

    @Serializable
    data class ContentDetails(
        val duration: String
    )

    override fun configure(config: Map<String, Any>): Boolean {
        apiKey = config["api_key"] as? String ?: return false
        region = config["region"] as? String ?: "US"
        return true
    }

    override suspend fun searchTracks(query: String, limit: Int): List<AudioTrack> {
        val response: HttpResponse = client.get("https://youtube.googleapis.com/youtube/v3/search") {
            parameter("part", "snippet")
            parameter("q", "$query music")
            parameter("type", "video")
            parameter("key", apiKey)
            parameter("maxResults", limit)
        }

        val searchResponse = Json.decodeFromString<YouTubeSearchResponse>(response.bodyAsText())

        return searchResponse.items.map { track ->
            AudioTrack(
                id = track.id,
                title = track.snippet.title,
                artist = track.snippet.channelTitle,
                thumbnailUrl = track.snippet.thumbnails.high.url,
                duration = parseDuration(track.contentDetails.duration),
                streamUrl = "https://www.youtube.com/watch?v=${track.id}"
            )
        }
    }

    private fun parseDuration(duration: String): Long {
        // Parse ISO 8601 duration format (e.g., PT3M45S)
        val regex = "PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?".toRegex()
        val matchResult = regex.matchEntire(duration)

        if (matchResult != null) {
            val hours = matchResult.groupValues[1].toLongOrNull() ?: 0
            val minutes = matchResult.groupValues[2].toLongOrNull() ?: 0
            val seconds = matchResult.groupValues[3].toLongOrNull() ?: 0

            return (hours * 3600 + minutes * 60 + seconds) * 1000
        }

        return 0
    }

    override suspend fun getTrackDetails(trackId: String): AudioTrack? {
        val response: HttpResponse = client.get("https://youtube.googleapis.com/youtube/v3/videos") {
            parameter("part", "snippet,contentDetails")
            parameter("id", trackId)
            parameter("key", apiKey)
        }

        val searchResponse = Json.decodeFromString<YouTubeSearchResponse>(response.bodyAsText())

        return searchResponse.items.firstOrNull()?.let { track ->
            AudioTrack(
                id = track.id,
                title = track.snippet.title,
                artist = track.snippet.channelTitle,
                thumbnailUrl = track.snippet.thumbnails.high.url,
                duration = parseDuration(track.contentDetails.duration),
                streamUrl = "https://www.youtube.com/watch?v=${track.id}"
            )
        }
    }

    override suspend fun streamAudio(trackId: String): Flow<ByteArray> = flow {
        try {
            // Ensure track exists before attempting to stream
            val trackDetails = getTrackDetails(trackId)
                ?: throw IllegalArgumentException("Track not found")

            // Stream audio directly from yt-dlp's stdout
            streamAudioFromYtDlp(trackId)
        } catch (e: Exception) {
            emit("Error streaming audio: ${e.message}".toByteArray())
        }
    }

    /**
     * Stream audio directly from yt-dlp's stdout
     */
    private fun streamAudioFromYtDlp(trackId: String) = flow {
        withContext(Dispatchers.IO) {
            val processBuilder = ProcessBuilder(
                "yt-dlp",
                "-x",                      // Extract audio
                "--audio-format", "m4a",   // Specify M4A format
                "-o", "-",                 // Output to stdout
                trackId
            )

            try {
                val process = processBuilder.start()

                // Use BufferedInputStream for efficient reading
                BufferedInputStream(process.inputStream).use { inputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    // Read and emit audio chunks
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        emit(buffer.copyOf(bytesRead))
                    }
                }

                // Wait for process to complete with timeout
                val completed = process.waitFor(2, TimeUnit.MINUTES)

                if (!completed) {
                    process.destroyForcibly()
                    throw IOException("Audio download timed out")
                }

                // Check for download errors
                val exitCode = process.exitValue()
                if (exitCode != 0) {
                    val errorOutput = process.errorStream.bufferedReader().use { it.readText() }
                    throw IOException("yt-dlp download failed with exit code $exitCode: $errorOutput")
                }
            } catch (e: Exception) {
                throw IOException("Failed to stream audio: ${e.message}")
            }
        }
    }

    override suspend fun getPlaybackControls(): PlaybackControls {
        return object : PlaybackControls {
            private var isPlaying = false
            private var currentPosition: Long = 0
            private var duration: Long = 0

            override suspend fun play() {
                isPlaying = true
            }

            override suspend fun pause() {
                isPlaying = false
            }

            override suspend fun stop() {
                isPlaying = false
                currentPosition = 0
            }

            override suspend fun seek(positionMs: Long) {
                currentPosition = positionMs
            }

            override suspend fun getPlaybackState(): PlaybackState {
                return PlaybackState(
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration
                )
            }
        }
    }

    override suspend fun cleanup() {
        client.close()
    }
}