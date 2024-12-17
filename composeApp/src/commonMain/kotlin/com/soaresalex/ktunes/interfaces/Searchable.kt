package com.soaresalex.ktunes.interfaces

/**
 * An abstract class that provides both filtering and sorting capabilities for data items.
 * Implementing classes should define their filterable fields and sortable properties.
 */
abstract class Searchable {
	/**
	 * Returns a list of fields that can be used for filtering.
	 * @return List of string fields to be used in filtering
	 */
	abstract fun getFilterableFields(): List<String>

	/**
	 * Returns a list of sortable properties as name-value pairs.
	 * @return List of Pairs where the first element is the property name and the second is its comparable value
	 */
	abstract fun getSortables(): List<Pair<String, Comparable<*>>>

	/**
	 * Determines whether this item matches the given filter.
	 * @param filter The filter string to match against
	 * @return true if the item matches the filter, false otherwise
	 */
	fun matchesFilter(filter: String?): Boolean {
		if (filter.isNullOrBlank()) return true
		return getFilterableFields().any { it.contains(filter, ignoreCase = true) }
	}

	/**
	 * Retrieves the sort value for a given sort key.
	 * @param sortBy The key to retrieve the sort value for
	 * @return The comparable value for sorting, or an empty string if not found
	 */
	fun getSortValue(sortBy: String): Comparable<*> {
		return getSortables().firstOrNull { it.first == sortBy }?.second ?: ""
	}
}

/**
 * Extension function to filter a list of Searchable items.
 * @param filter The filter string to apply
 * @return Filtered list of items
 */
fun <T : Searchable> List<T>.filterItems(filter: String?): List<T> {
	return if (filter.isNullOrBlank()) {
		this
	} else {
		filter { it.matchesFilter(filter) }
	}
}

/**
 * Extension function to sort a list of Searchable items.
 * @param sortBy The property to sort by
 * @param sortOrder The order of sorting (ascending or descending)
 * @return Sorted list of items
 */
fun <T : Searchable> List<T>.sortItems(
	sortBy: String,
	sortOrder: SortOrder = SortOrder.ASCENDING
): List<T> {
	return when (sortOrder) {
		SortOrder.ASCENDING -> sortedWith(compareBy { item ->
			@Suppress("UNCHECKED_CAST")
			(item.getSortValue(sortBy) as Comparable<Any>)
		})
		SortOrder.DESCENDING -> sortedWith(compareByDescending { item ->
			@Suppress("UNCHECKED_CAST")
			(item.getSortValue(sortBy) as Comparable<Any>)
		})
	}
}

// Enum for sort order remains the same
enum class SortOrder {
	ASCENDING,
	DESCENDING
}