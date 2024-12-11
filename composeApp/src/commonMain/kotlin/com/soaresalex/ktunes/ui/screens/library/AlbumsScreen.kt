package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.koin.koinScreenModel
import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import com.soaresalex.ktunes.ui.components.LibraryItem
import com.soaresalex.ktunes.ui.screens.details.AlbumDetailScreen

object AlbumsScreen : BaseLibraryScreen<Album>() {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<LibraryScreenModel>()
        val albums by screenModel.albums.collectAsState()

        LibraryScreenTemplate(
            viewModel = screenModel,
            items = albums,
            itemContent = { album ->
                LibraryItem(
                    primaryText = album.title,
                    secondaryText = album.artist,
                    artworkUrl = album.coverArtUri,
                    modifier = Modifier.clickable(onClick = {
                        screenModel.history.navigateTo(AlbumDetailScreen(album))
                    })
                )
            })
    }

    override fun getScreenTitle() = "Albums"
}
