package com.soaresalex.ktunes.data.models

import com.soaresalex.ktunes.Filterable
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
	val id: String,
	val name: String,
	val photoUri: String? = null,
	val albumCount: Int = 0,
	val trackCount: Int = 0
) : Filterable {
	override fun matchesFilter(query: String): Boolean {
		return name.contains(query, ignoreCase = true)
	}
}