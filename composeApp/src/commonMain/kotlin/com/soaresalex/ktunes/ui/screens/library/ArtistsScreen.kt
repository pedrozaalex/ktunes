package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import com.soaresalex.ktunes.ui.components.LibraryItem
import com.soaresalex.ktunes.ui.screens.details.ArtistDetailScreen

object ArtistsScreen : BaseLibraryScreen<Artist>() {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<LibraryScreenModel>()
        val artists by viewModel.artists.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LibraryScreenTemplate(
            viewModel = viewModel, items = artists, itemContent = { artist ->
                LibraryItem(
                    primaryText = artist.name,
                    secondaryText = "${artist.albumCount} Albums",
                    modifier = Modifier.clickable(onClick = {
                        navigator.push(ArtistDetailScreen(artist))
                    })
                )
            })
    }

    override fun getScreenTitle() = "Artists"
}