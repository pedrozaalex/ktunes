package com.soaresalex.ktunes.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel

// Continuation of BaseLibraryScreen
abstract class BaseLibraryScreen<T> : Screen {
    @Composable
    protected fun LibraryScreenTemplate(
        viewModel: LibraryScreenModel,
        items: List<T>,
        itemContent: @Composable (T) -> Unit,
    ) {
        val isLoading by viewModel.isLoading.collectAsState()

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.weight(1f)
                ) {
                    Text(
                        getScreenTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Row {
                    IconButton(onClick = { /** TODO: Implement full search functionality */ }) {
                        Icon(
                            Icons.Default.Search, contentDescription = "Search"
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize().wrapContentSize()
                    )
                } else if (items.isEmpty()) {
                    Text(
                        text = "No items found",
                        modifier = Modifier.fillMaxSize(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            itemContent(item)
                        }
                    }
                }
            }
        }
    }

    // Abstract method to get screen title
    protected abstract fun getScreenTitle(): String
}