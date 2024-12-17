package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.soaresalex.ktunes.ui.screens.library.LibraryViewType.entries
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import kotlinx.coroutines.flow.Flow

abstract class LibraryScreen<T> : Screen {
	@Composable
	override fun Content() {
		val screenModel = koinScreenModel<LibraryScreenModel>()
		val data by screenModel.getItems().collectAsState(emptyList())
		val isLoading by screenModel.isLoading.collectAsState()

		var selectedViewType by screenModel.settings.observableState(
			"selectedViewType_${this::class.simpleName}", LibraryViewType.GRID.displayName
		)

		// State for filtering and sorting
		var filterText by remember { mutableStateOf("") }
		var isFilterExpanded by remember { mutableStateOf(false) }
		var isSortExpanded by remember { mutableStateOf(false) }
		val sortOptions = getSortOptions()
		var selectedSortOption by remember { mutableStateOf(sortOptions.firstOrNull()) }
		var isSortAscending by remember { mutableStateOf(true) }

		Column(modifier = Modifier.fillMaxSize()) {
			// Enhanced Title Bar with Filtering and Sorting
			ElevatedCard(
				modifier = Modifier.fillMaxWidth(),
				shape = RoundedCornerShape(8.dp),
				elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
			) {
				Column(
					modifier = Modifier.padding(16.dp)
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						// Screen Title
						Text(
							text = getScreenTitle(),
							style = MaterialTheme.typography.headlineSmall,
							modifier = Modifier.weight(1f)
						)

						Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
							OutlinedButton(
								onClick = { isFilterExpanded = !isFilterExpanded },
								modifier = Modifier.height(30.dp),
								contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
							) {
								Icon(
									imageVector = FeatherIcons.Search,
									contentDescription = "Search ${getScreenTitle()}"
								)
							}

							Box {
								OutlinedButton(
									onClick = { isSortExpanded = !isSortExpanded },
									modifier = Modifier.height(30.dp),
									contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
								) {
									Text(selectedSortOption ?: "Sort By", style = MaterialTheme.typography.bodySmall)
									Spacer(Modifier.size(ButtonDefaults.IconSpacing))
									Icon(
										imageVector = if (isSortAscending) FeatherIcons.ArrowUp else FeatherIcons.ArrowDown,
										contentDescription = if (isSortAscending) "Ascending" else "Descending"
									)
								}
								DropdownMenu(
									expanded = isSortExpanded,
									onDismissRequest = { isSortExpanded = false },
									tonalElevation = 16.dp,
									shadowElevation = 8.dp,
								) {
									sortOptions.forEach { option ->
										DropdownMenuItem(
											text = { Text(option) },
											onClick = {
												selectedSortOption = option
												updateSort(screenModel, option, isSortAscending)
											},
											trailingIcon = {
												if (selectedSortOption == option) {
													IconButton(onClick = {
														isSortAscending = !isSortAscending
														updateSort(screenModel, option, isSortAscending)
													}, Modifier.size(30.dp)) {
														Icon(
															imageVector = if (isSortAscending) FeatherIcons.ArrowUp else FeatherIcons.ArrowDown,
															contentDescription = if (isSortAscending) "Ascending" else "Descending"
														)
													}
												}
											}
										)
									}
								}
							}

							SingleChoiceSegmentedButtonRow(
								modifier = Modifier.height(30.dp)
							) {
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
					}

					// Filter UI
					AnimatedVisibility(
						visible = isFilterExpanded,
						enter = expandVertically(animationSpec = tween(durationMillis = 300)) + fadeIn(
							animationSpec = tween(durationMillis = 300)
						),
						exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) + fadeOut(
							animationSpec = tween(durationMillis = 300)
						)
					) {
						Column {
							Spacer(modifier = Modifier.height(8.dp))
							OutlinedTextField(
								value = filterText,
								onValueChange = {
									filterText = it
									updateFilter(screenModel, it)
								},
								modifier = Modifier.fillMaxWidth(),
								placeholder = { Text("Search ${getScreenTitle()}") },
								leadingIcon = {
									Icon(
										imageVector = FeatherIcons.Search, contentDescription = "Search"
									)
								},
								trailingIcon = {
									if (filterText.isNotEmpty()) {
										IconButton(onClick = {
											filterText = ""
											updateFilter(screenModel, "")
										}) {
											Icon(
												imageVector = FeatherIcons.XCircle, contentDescription = "Clear"
											)
										}
									}
								},
								shape = RoundedCornerShape(8.dp)
							)
						}
					}
				}
			}

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
									verticalArrangement = Arrangement.spacedBy(12.dp),
									horizontalArrangement = Arrangement.spacedBy(12.dp)
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

	// Abstract methods for sorting and filtering
	protected open fun getSortOptions(): List<String> = emptyList()
	protected abstract fun updateSort(
		screenModel: LibraryScreenModel, sortOption: String, isAscending: Boolean
	)

	protected abstract fun updateFilter(
		screenModel: LibraryScreenModel, filterQuery: String
	)

	// Abstract methods for content rendering
	protected abstract fun getScreenTitle(): String
	abstract val getItems: LibraryScreenModel.() -> Flow<List<T>>

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
			return entries.find {
				it.displayName.equals(name, ignoreCase = true)
			}
		}
	}
}