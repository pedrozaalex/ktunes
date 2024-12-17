package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.runtime.Composable
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

object ArtistsScreen : LibraryScreen<Artist>() {
	override fun getSortOptions(): List<String> {
//		TODO("Not yet implemented")
		return emptyList()
	}

	override fun updateSort(
		screenModel: LibraryScreenModel,
		sortOption: String,
		isAscending: Boolean
	) {
//		TODO("Not yet implemented")
	}

	override fun updateFilter(
		screenModel: LibraryScreenModel,
		filterQuery: String
	) {
//		TODO("Not yet implemented")
	}

	override fun getScreenTitle() = "Artists"

	override val getItems = LibraryScreenModel::filteredArtists

	@Composable
	override fun GridItemView(item: Artist) {
//		TODO("Not yet implemented")
	}

	@Composable
	override fun ListItemView(item: Artist) {
//		TODO("Not yet implemented")
	}

	@Composable
	override fun TableItemView(item: Artist) {
//		TODO("Not yet implemented")
	}

	override val handleClick: LibraryScreenModel.(Artist) -> Unit
		get() = {
//			TODO("Not yet implemented")
		}

	override fun getSelectedSortOption(): StateFlow<String> {
		TODO("Not yet implemented")
	}

	override fun getSelectedSortOrder(): StateFlow<Boolean> {
		TODO("Not yet implemented")
	}
}