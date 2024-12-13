package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.StateFlow

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

abstract class LibraryScreen<T> : Screen {
	@Composable
	override fun Content() {
		val screenModel = koinScreenModel<LibraryScreenModel>()
		val data = screenModel.getItems().collectAsState(emptyList()).value
		val isLoading by screenModel.isLoading.collectAsState()

		var selectedViewType by screenModel.settings.observableState(
			"selectedViewType_${this::class.simpleName}", LibraryViewType.GRID.displayName
		)

		Column(modifier = Modifier.fillMaxSize()) {
			// Title Bar
			Row(
				modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = getScreenTitle(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f)
				)

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

			// Content Area
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

	protected abstract fun getScreenTitle(): String

	abstract val getItems: LibraryScreenModel.() -> StateFlow<List<T>>

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