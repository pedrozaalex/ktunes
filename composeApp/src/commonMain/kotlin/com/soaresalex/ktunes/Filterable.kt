package com.soaresalex.ktunes

interface Filterable {
	fun matchesFilter(query: String): Boolean
}