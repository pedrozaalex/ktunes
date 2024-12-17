package com.soaresalex.ktunes.data.models

import com.soaresalex.ktunes.Filterable
import kotlinx.serialization.Serializable

@Serializable
data class Track(
	val id: String,
	val fileUri: String? = null,
	val title: String,
	val artist: String,
	val album: String,
	val duration: Long, // Duration in milliseconds
	val albumArtUri: String? = null,
	val trackNumber: Int? = null,
	val year: Int? = null
) : Filterable {
	override fun matchesFilter(query: String): Boolean {
		return listOf(title, artist, album).any { it.contains(query, ignoreCase = true) }
	}
}