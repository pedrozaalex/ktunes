package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

data class LibraryItemStyle(
    val imageSize: Dp,
    val showSecondaryText: Boolean,
    val primaryTextStyle: TextStyle,
    val secondaryTextStyle: TextStyle
)

@Composable
fun LibraryItem(
    primaryText: String,
    secondaryText: String? = null,
    artworkUrl: String? = null,
    style: LibraryItemStyle = LibraryItemStyle(
        120.dp,
        true,
        MaterialTheme.typography.bodyMedium,
        MaterialTheme.typography.bodySmall
    ),
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(style.imageSize)
            ) {
                AsyncImage(
                    model = artworkUrl,
                    contentDescription = primaryText,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(Icons.Filled.Star)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = primaryText,
                style = style.primaryTextStyle,
                maxLines = 1
            )
            if (style.showSecondaryText && !secondaryText.isNullOrBlank()) {
                Text(
                    text = secondaryText,
                    style = style.secondaryTextStyle,
                    maxLines = 1
                )
            }
        }
    }
}