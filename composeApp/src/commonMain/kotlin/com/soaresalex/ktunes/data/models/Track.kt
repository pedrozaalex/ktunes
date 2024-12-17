package com.soaresalex.ktunes.data.models

import com.soaresalex.ktunes.interfaces.Searchable
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
) : Searchable() {


	override fun getFilterableFields(): List<String> {
		return listOf(title, artist, album)
	}

	override fun getSortables(): List<Pair<String, Comparable<*>>> {
		return listOf(
			"Title" to title,
			"Artist" to artist,
			"Album" to album,
			"Duration" to duration,
			"#" to (trackNumber ?: 0),
			"Year" to (year ?: 0)
		)
	}
}