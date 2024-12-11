package com.soaresalex.ktunes.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // Duration in milliseconds
    val albumArtUri: String? = null,
    val fileUri: String,
    val trackNumber: Int? = null,
    val year: Int? = null
)

@Serializable
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val releaseYear: Int? = null,
    val coverArtUri: String? = null,
    val trackCount: Int = 0,
    val totalDuration: Long = 0
)

@Serializable
data class Artist(
    val id: String,
    val name: String,
    val photoUri: String? = null,
    val albumCount: Int = 0,
    val trackCount: Int = 0
)

@Serializable
data class Playlist(
    val id: String,
    val title: String,
    val description: String? = null,
    val createdAt: Long,
    val trackCount: Int = 0,
    val coverArtUri: String? = null,
    val tracks: List<Track> = emptyList()
)