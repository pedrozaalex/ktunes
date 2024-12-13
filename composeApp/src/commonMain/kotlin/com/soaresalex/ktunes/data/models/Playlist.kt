package com.soaresalex.ktunes.data.models

import kotlinx.serialization.Serializable

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