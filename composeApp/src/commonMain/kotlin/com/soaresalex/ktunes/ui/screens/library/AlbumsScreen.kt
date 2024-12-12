package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.koin.koinScreenModel
import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import com.soaresalex.ktunes.ui.components.LibraryItem
import com.soaresalex.ktunes.ui.components.LibraryItemStyle
import com.soaresalex.ktunes.ui.screens.details.AlbumDetailScreen

object AlbumsScreen : BaseLibraryScreen<Album>() {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<LibraryScreenModel>()
        val albums by screenModel.albums.collectAsState()

        LibraryScreenTemplate(
            screenModel,
            albums,
        ) { album, viewType ->
            LibraryItem(
                primaryText = album.title,
                secondaryText = album.artist,
                artworkUrl = album.coverArtUri,
                viewType = viewType,
                style = LibraryItemStyle(CircleShape),
                onClick = {
                    screenModel.history.navigateTo(AlbumDetailScreen(album))
                })
        }
    }

    override fun getScreenTitle() = "Albums"
}
