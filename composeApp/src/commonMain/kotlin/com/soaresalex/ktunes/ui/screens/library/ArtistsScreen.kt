package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.runtime.Composable
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import kotlinx.coroutines.flow.StateFlow

object ArtistsScreen : LibraryScreen<Artist>() {
	override fun getScreenTitle() = "Artists"

	override val getItems: LibraryScreenModel.() -> StateFlow<List<Artist>>
		get() = { artists }

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

	override fun handleClick(item: Artist) {
//		TODO("Not yet implemented")
	}
}