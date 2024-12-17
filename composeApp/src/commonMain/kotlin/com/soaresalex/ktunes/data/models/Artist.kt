package com.soaresalex.ktunes.data.models

import com.soaresalex.ktunes.interfaces.Searchable
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
	val id: String,
	val name: String,
	val photoUri: String? = null,
	val albumCount: Int = 0,
	val trackCount: Int = 0
) : Searchable() {
	override fun getFilterableFields(): List<String> {
		return listOf(name)
	}

	override fun getSortables(): List<Pair<String, Comparable<*>>> {
		return listOf(
			"Name" to name,
			"# of albums" to albumCount,
			"# of tracks" to trackCount
		)
	}
}