package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.runtime.Composable
import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import kotlinx.coroutines.flow.StateFlow

object AlbumsScreen : LibraryScreen<Album>() {
	override fun getScreenTitle() = "Albums"

	override val getItems: LibraryScreenModel.() -> StateFlow<List<Album>>
		get() = { repo.albums }

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
