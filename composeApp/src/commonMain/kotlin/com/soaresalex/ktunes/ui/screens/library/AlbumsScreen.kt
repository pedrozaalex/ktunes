package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.runtime.Composable
import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel

object AlbumsScreen : LibraryScreen<Album>() {
	override fun getSortOptions(): List<String> {
//		TODO("Not yet implemented")
		return emptyList()
	}

	override fun updateSort(
		screenModel: LibraryScreenModel, sortOption: String, isAscending: Boolean
	) {
//		TODO("Not yet implemented")
	}

	override fun updateFilter(
		screenModel: LibraryScreenModel, filterQuery: String
	) {
//		TODO("Not yet implemented")
	}

	override fun getScreenTitle() = "Albums"

	override val getItems = LibraryScreenModel::filteredSortedAlbums

	@Composable
	override fun GridItemView(item: Album) {
//		TODO("Not yet implemented")
	}

	@Composable
	override fun ListItemView(item: Album) {
//		TODO("Not yet implemented")
	}

	@Composable
	override fun TableItemView(item: Album) {
//		TODO("Not yet implemented")
	}

	override val handleClick: LibraryScreenModel.(Album) -> Unit
		get() = {
//			TODO("Not yet implemented")
		}
}
