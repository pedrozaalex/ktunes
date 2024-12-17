package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.soaresalex.ktunes.data.models.Track
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.Music
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TracksScreen : LibraryScreen<Track>() {
	override fun getSortOptions(): List<String> = listOf("Title", "Artist", "Album", "Duration")


	override fun updateSort(
		screenModel: LibraryScreenModel, sortOption: String, isAscending: Boolean
	) {
		val sortBy = when (sortOption) {
			"Title" -> LibraryScreenModel.TrackSortBy.TITLE
			"Artist" -> LibraryScreenModel.TrackSortBy.ARTIST
			"Album" -> LibraryScreenModel.TrackSortBy.ALBUM
			"Duration" -> LibraryScreenModel.TrackSortBy.DURATION
			else -> LibraryScreenModel.TrackSortBy.TITLE
		}
		val sortOrder = if (isAscending) LibraryScreenModel.SortOrder.ASCENDING
		else LibraryScreenModel.SortOrder.DESCENDING

		screenModel.updateTrackSort(sortBy, sortOrder)
	}

	override fun updateFilter(
		screenModel: LibraryScreenModel, filterQuery: String
	) {
		screenModel.setTrackNameFilter(filterQuery)
	}

	override fun getScreenTitle() = "Tracks"

	override val getItems = LibraryScreenModel::filteredTracks

	@Composable
	override fun GridItemView(item: Track) {
		Column(
			modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
		) {
			TrackImage(item.albumArtUri, item.title, MaterialTheme.shapes.medium, 120.dp)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis
			)
			Text(
				item.artist,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}

	@Composable
	override fun ListItemView(item: Track) {
		Row(Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
			TrackImage(item.albumArtUri, item.title, MaterialTheme.shapes.medium, 80.dp)
			Spacer(Modifier.width(8.dp))
			Column(Modifier.weight(1f)) {
				Text(
					item.title,
					style = MaterialTheme.typography.bodyMedium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					item.artist,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}

	@Composable
	override fun TableItemView(item: Track) {
		Row(
			Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			TrackImage(item.albumArtUri, item.title, MaterialTheme.shapes.small, 80.dp)
			Spacer(Modifier.width(8.dp))
			Text(
				item.title,
				Modifier.weight(1f),
				style = MaterialTheme.typography.bodyMedium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				item.artist,
				Modifier.weight(1f),
				MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.bodySmall,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				textAlign = TextAlign.End
			)
		}
	}

	override val handleClick = LibraryScreenModel::onLibraryTrackClick

	override fun getSelectedSortOption(): StateFlow<String> {
		// Assuming the first option is the default
		val defaultSortOption = getSortOptions().firstOrNull() ?: ""
		val selectedSortOption = MutableStateFlow(defaultSortOption)
		// Update selectedSortOption based on the actual selection in the screen model
		// This is a placeholder; you should replace it with the actual logic to get the selected sort option
		return selectedSortOption
	}

	override fun getSelectedSortOrder(): StateFlow<Boolean> {
		val isAscending = MutableStateFlow(true)
		// Update isAscending based on the actual selection in the screen model
		// This is a placeholder; you should replace it with the actual logic to get the selected sort order
		return isAscending
	}

	@Composable
	private fun TrackImage(url: String?, contentDescription: String?, shape: Shape, size: Dp) {
		when (url) {
			null -> {
				Icon(
					imageVector = FeatherIcons.Music,
					contentDescription = contentDescription,
					modifier = Modifier.size(size)
				)
			}

			else -> {
				AsyncImage(
					url,
					contentDescription,
					Modifier.size(size).clip(shape),
					contentScale = ContentScale.Crop,
					placeholder = rememberVectorPainter(FeatherIcons.Music),
					fallback = rememberVectorPainter(FeatherIcons.Music),
					error = rememberVectorPainter(FeatherIcons.Music),
				)
			}
		}
	}
}