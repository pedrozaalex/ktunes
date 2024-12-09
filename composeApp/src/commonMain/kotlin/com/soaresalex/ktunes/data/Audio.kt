package com.soaresalex.ktunes.data

import com.soaresalex.ktunes.viewmodels.AlbumViewModel
import com.soaresalex.ktunes.viewmodels.ArtistViewModel
import com.soaresalex.ktunes.viewmodels.PlaylistViewModel
import com.soaresalex.ktunes.viewmodels.TrackViewModel
import kotlin.text.toLong

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
)

data class Album(
    val title: String,
    val artist: String,
    val tracks: List<Track>,
)

data class Artist(
    val name: String,
    val albums: List<Album>,
)

data class Playlist(
    val name: String,
    val creator: String,
    val description: String?,
    val tracks: List<Track>,
)