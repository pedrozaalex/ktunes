package com.soaresalex.ktunes.data

interface AudioPlugin {
    val name: String
    val description: String
    fun listTracks(): List<Track>
    fun playTrack(track: Track): Boolean
    fun streamTrack(track: Track): ByteArray // Returns audio data
}

data class Track(
    val title: String,
    val artist: String,
    val duration: Int, // Duration in seconds
    val url: String
)