package com.soaresalex.ktunes.ui.screens.library


import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.koin.koinScreenModel
import com.soaresalex.ktunes.data.models.Track
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import com.soaresalex.ktunes.ui.components.LibraryItem
import com.soaresalex.ktunes.ui.navigation.History
import com.soaresalex.ktunes.ui.screens.details.TrackDetailScreen
import org.koin.compose.koinInject

object TracksScreen : BaseLibraryScreen<Track>() {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<LibraryScreenModel>()
        val tracks by viewModel.tracks.collectAsState()
        val history: History = koinInject()

        LibraryScreenTemplate(
            viewModel = viewModel,
            items = tracks,
            itemContent = { track ->
                LibraryItem(
                    primaryText = track.title,
                    secondaryText = track.artist,
                    artworkUrl = track.albumArtUri,
                    modifier = Modifier.clickable(onClick = {
                        history.navigateTo(TrackDetailScreen(track))
                    })
                )
            })
    }

    override fun getScreenTitle() = "Tracks"
}