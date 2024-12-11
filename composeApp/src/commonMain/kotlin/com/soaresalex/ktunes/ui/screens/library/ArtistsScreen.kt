package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.koin.koinScreenModel
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import com.soaresalex.ktunes.ui.components.LibraryItem
import com.soaresalex.ktunes.ui.screens.details.ArtistDetailScreen

object ArtistsScreen : BaseLibraryScreen<Artist>() {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<LibraryScreenModel>()
        val artists by screenModel.artists.collectAsState()

        LibraryScreenTemplate(
            screenModel = screenModel,
            items = artists,
            itemContent = { artist, viewType ->
                LibraryItem(
                    primaryText = artist.name,
                    secondaryText = "${artist.albumCount} Albums",
                    viewType = viewType,
                    onClick = {
                        screenModel.history.navigateTo(ArtistDetailScreen(artist))
                    })
            })
    }

    override fun getScreenTitle() = "Artists"
}