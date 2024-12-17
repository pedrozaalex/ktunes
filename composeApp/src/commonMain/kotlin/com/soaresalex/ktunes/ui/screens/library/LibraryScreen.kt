package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.soaresalex.ktunes.data.observableState
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.Grid
import compose.icons.feathericons.Layout
import compose.icons.feathericons.List
import compose.icons.feathericons.Search
import kotlinx.coroutines.flow.Flow

abstract class LibraryScreen<T> : Screen {
	@Composable
	override fun Content() {
		val screenModel = koinScreenModel<LibraryScreenModel>()
		val data = getItems(screenModel).collectAsState(emptyList()).value
		val isLoading by screenModel.isLoading.collectAsState()

		var selectedViewType by screenModel.settings.observableState(
			"selectedViewType_${this::class.simpleName}", LibraryViewType.GRID.displayName
		)

		// State for search and sorting
		var searchQuery by remember { mutableStateOf("") }
		var isSearchExpanded by remember { mutableStateOf(false) }
		var isSortExpanded by remember { mutableStateOf(false) }

		// Define sort options based on the specific library type (to be implemented in subclasses)
		val sortOptions = remember { getSortOptions() }
		var selectedSortOption by remember { mutableStateOf(sortOptions.first()) }
		var isSortAscending by remember { mutableStateOf(true) }

		Column(modifier = Modifier.fillMaxSize()) {
			// Title Bar with View Type, Search, and Sort Controls
			Row(
				modifier = Modifier.padding(16.dp)
					.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				// Screen Title
				Text(
					text = getScreenTitle(),
					style = MaterialTheme.typography.titleLarge,
					modifier = Modifier.weight(1f)
				)

				// Search Button
				IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
					Icon(imageVector = FeatherIcons.Search, contentDescription = "Search")
				}

				// Sort Button
				Box {
					IconButton(onClick = { isSortExpanded = !isSortExpanded }) {
						Icon(
							imageVector = Icons.Default.ArrowDropDown,
							contentDescription = "Sort",
							tint = if (isSortAscending) Color.Black else Color.Red
						)
					}
					DropdownMenu(
						expanded = isSortExpanded,
						onDismissRequest = { isSortExpanded = false }
					) {
						// Sort Options
						sortOptions.forEach { sortOption ->
							DropdownMenuItem(
								text = { Text(sortOption) },
								onClick = {
									selectedSortOption = sortOption
									isSortExpanded = false
									// Call sort method in screen model
									updateSort(screenModel, sortOption, isSortAscending)
								}
							)
						}

						// Sort Order Toggle
						Divider()
						DropdownMenuItem(
							text = {
								Text(if (isSortAscending) "Descending" else "Ascending")
							},
							onClick = {
								isSortAscending = !isSortAscending
								isSortExpanded = false
								// Call sort method in screen model with new order
								updateSort(screenModel, selectedSortOption, isSortAscending)
							}
						)
					}
				}

				// View Type Selector
				SingleChoiceSegmentedButtonRow(Modifier.height(30.dp)) {
					LibraryViewType.entries.forEachIndexed { index, viewType ->
						SegmentedButton(
							shape = SegmentedButtonDefaults.itemShape(
								index = index, count = LibraryViewType.entries.size
							),
							onClick = { selectedViewType = viewType.displayName },
							selected = selectedViewType == viewType.displayName,
						) {
							Icon(
								imageVector = when (viewType) {
									LibraryViewType.GRID -> FeatherIcons.Grid
									LibraryViewType.LIST -> FeatherIcons.List
									LibraryViewType.TABLE -> FeatherIcons.Layout
								},
								contentDescription = viewType.displayName,
							)
						}
					}
				}
			}

			// Search Dropdown
			if (isSearchExpanded) {
				TextField(
					value = searchQuery,
					onValueChange = {
						searchQuery = it
						// Call filter method in screen model
						updateFilter(screenModel, it)
					},
					placeholder = { Text("Search...") },
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp)
				)
			}

			// Existing content rendering logic remains unchanged
			Box(
				modifier = Modifier.fillMaxSize()
			) {
				when {
					isLoading -> {
						CircularProgressIndicator(
							modifier = Modifier.size(50.dp).align(Alignment.Center)
						)
					}

					data.isEmpty() -> {
						Text(
							text = "No items found",
							modifier = Modifier.align(Alignment.Center),
							style = MaterialTheme.typography.bodyLarge
						)
					}

					else -> {
						when (LibraryViewType.fromString(selectedViewType)) {
							LibraryViewType.GRID -> {
								LazyVerticalGrid(
									columns = GridCells.Adaptive(minSize = 150.dp),
									contentPadding = PaddingValues(vertical = 8.dp),
									horizontalArrangement = Arrangement.spacedBy(12.dp),
									verticalArrangement = Arrangement.spacedBy(12.dp)
								) {
									items(data) {
										ItemContainer(it) { GridItemView(it) }
									}
								}
							}

							LibraryViewType.LIST -> {
								LazyColumn {
									items(data) {
										ItemContainer(it) { ListItemView(it) }
									}
								}
							}

							LibraryViewType.TABLE -> {
								LazyColumn {
									items(data) {
										ItemContainer(it) { TableItemView(it) }
									}
								}
							}

							else -> error("Invalid view type: $selectedViewType")
						}
					}
				}
			}
		}
	}

	// Abstract method to provide sort options for each specific library screen
	protected abstract fun getSortOptions(): List<String>

	// Abstract method to update sorting in the screen model
	protected abstract fun updateSort(
		screenModel: LibraryScreenModel,
		sortOption: String,
		isAscending: Boolean
	)

	// Abstract method to update filtering in the screen model
	protected abstract fun updateFilter(
		screenModel: LibraryScreenModel,
		filterQuery: String
	)

	// Existing abstract methods remain unchanged
	protected abstract fun getScreenTitle(): String

	abstract fun getItems(model: LibraryScreenModel): Flow<List<T>>

	@Composable
	protected abstract fun GridItemView(item: T)

	@Composable
	protected abstract fun ListItemView(item: T)

	@Composable
	protected abstract fun TableItemView(item: T)

	abstract val handleClick: LibraryScreenModel.(T) -> Unit

	@Composable
	private fun ItemContainer(item: T, content: @Composable () -> Unit) {
		val screenModel = koinScreenModel<LibraryScreenModel>()
		Surface(
			onClick = {
				screenModel.handleClick(item)
			},
			color = Color.Transparent,
		) { content() }
	}
}

enum class LibraryViewType(val displayName: String) {
	GRID("Grid"), LIST("List"), TABLE("Table");

	companion object {
		fun fromString(name: String): LibraryViewType? {
			return LibraryViewType.entries.find {
				it.displayName.equals(
					name, ignoreCase = true
				)
			}
		}
	}
}
