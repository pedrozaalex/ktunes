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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.soaresalex.ktunes.data.observableState
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.Columns
import compose.icons.feathericons.Grid
import compose.icons.feathericons.List

enum class LibraryViewType(val displayName: String) {
    GRID("Grid"), LIST("List"), TABLE("Table");

    companion object {
        fun fromString(name: String): LibraryViewType? {
            return LibraryViewType.entries.find {
                it.displayName.equals(
                    name,
                    ignoreCase = true
                )
            }
        }
    }
}

abstract class BaseLibraryScreen<T> : Screen {
    @Composable
    protected fun LibraryScreenTemplate(
        screenModel: LibraryScreenModel,
        items: List<T>,
        itemContent: @Composable (T, LibraryViewType) -> Unit,
    ) {
        val isLoading by screenModel.isLoading.collectAsState()

        var selectedViewType by screenModel.settings.observableState(
            "selectedViewType_${this::class.simpleName}",
            LibraryViewType.GRID.displayName
        )

        Column(modifier = Modifier.fillMaxSize()) { // Top Section with Screen Title and View Type Selector
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getScreenTitle(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp).weight(1f)
                )

                SingleChoiceSegmentedButtonRow(Modifier.height(32.dp)) {
                    LibraryViewType.entries.forEachIndexed { index, viewType ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = LibraryViewType.entries.size
                            ),
                            onClick = { selectedViewType = viewType.displayName },
                            selected = selectedViewType == viewType.displayName
                        ) {
                            Icon(
                                imageVector = when (viewType) {
                                    LibraryViewType.GRID -> FeatherIcons.Grid
                                    LibraryViewType.LIST -> FeatherIcons.List
                                    LibraryViewType.TABLE -> FeatherIcons.Columns
                                },
                                contentDescription = viewType.displayName
                            )
                        }
                    }
                }
            }

            // Content Area
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp).align(Alignment.Center)
                        )
                    }

                    items.isEmpty() -> {
                        Text(
                            text = "No items found",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    else -> {
                        when (LibraryViewType.fromString(selectedViewType)) {
                            LibraryViewType.GRID, null -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 150.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(items) { item ->
                                        itemContent(item, LibraryViewType.GRID)
                                    }
                                }
                            }

                            LibraryViewType.LIST -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(items) { item ->
                                        itemContent(item, LibraryViewType.LIST)
                                    }
                                }
                            }

                            LibraryViewType.TABLE -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(items) { item ->
                                        itemContent(item, LibraryViewType.TABLE)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Abstract method to get screen title
    protected abstract fun getScreenTitle(): String
}