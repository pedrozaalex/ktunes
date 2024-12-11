package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import com.soaresalex.ktunes.ui.components.LibraryItem
import com.soaresalex.ktunes.ui.screens.details.AlbumDetailScreen

object AlbumsScreen : BaseLibraryScreen<Album>() {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<LibraryScreenModel>()
        val albums by screenModel.albums.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LibraryScreenTemplate(
            viewModel = screenModel, items = albums, itemContent = { album ->
                LibraryItem(
                    primaryText = album.title, secondaryText = album.artist, modifier = Modifier.clickable(onClick = {
                        navigator.push(AlbumDetailScreen(album))
                    })
                )
            })
    }

    override fun getScreenTitle() = "Albums"
}
