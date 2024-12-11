package com.soaresalex.ktunes.ui.screens.library


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.koin.koinScreenModel
import com.soaresalex.ktunes.data.models.Track
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import com.soaresalex.ktunes.ui.components.LibraryItem
import com.soaresalex.ktunes.ui.screens.details.TrackDetailScreen

object TracksScreen : BaseLibraryScreen<Track>() {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<LibraryScreenModel>()
        val tracks by screenModel.tracks.collectAsState()

        LibraryScreenTemplate(
            screenModel = screenModel,
            items = tracks,
            itemContent = { track, viewType ->
                LibraryItem(
                    primaryText = track.title,
                    secondaryText = track.artist,
                    artworkUrl = track.albumArtUri,
                    viewType = viewType,
                    onClick = {
                        screenModel.history.navigateTo(TrackDetailScreen(track))
                    })
            })
    }

    override fun getScreenTitle() = "Tracks"
}