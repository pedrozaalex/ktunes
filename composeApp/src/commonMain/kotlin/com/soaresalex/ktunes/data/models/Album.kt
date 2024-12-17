package com.soaresalex.ktunes.data.models

import com.soaresalex.ktunes.interfaces.Searchable
import kotlinx.serialization.Serializable

@Serializable
data class Album(
	val id: String,
	val title: String,
	val artist: String,
	val releaseYear: Int? = null,
	val coverArtUri: String? = null,
	val trackCount: Int = 0,
	val totalDuration: Long = 0
) : Searchable() {
	override fun getFilterableFields(): List<String> {
		return listOf(title, artist)
	}

	override fun getSortables(): List<Pair<String, Comparable<*>>> {
		return listOf(
			"Title" to title,
			"Artist" to artist,
			"Year" to (releaseYear ?: 0),
			"# of tracks" to trackCount,
			"Total duration" to totalDuration
		)
	}
}