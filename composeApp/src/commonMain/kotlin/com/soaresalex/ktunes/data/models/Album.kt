package com.soaresalex.ktunes.data.models

import com.soaresalex.ktunes.Filterable
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
) : Filterable {
	override fun matchesFilter(query: String): Boolean {
		return listOf(title, artist).any { it.contains(query, ignoreCase = true) }
	}
}