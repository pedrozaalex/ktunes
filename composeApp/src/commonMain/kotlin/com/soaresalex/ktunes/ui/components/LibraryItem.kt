package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.soaresalex.ktunes.ui.screens.library.LibraryViewType
import compose.icons.FeatherIcons
import compose.icons.feathericons.Image

data class LibraryItemStyle(
    val imageShape: androidx.compose.ui.graphics.Shape,
    val imageSize: Dp = 120.dp,
    val contentPadding: PaddingValues = PaddingValues(8.dp),
    val spacing: Dp = 8.dp
)

@Composable
fun LibraryItem(
    primaryText: String,
    secondaryText: String? = null,
    additionalText: String? = null,
    artworkUrl: String? = null,
    viewType: LibraryViewType = LibraryViewType.GRID,
    style: LibraryItemStyle = LibraryItemStyle(MaterialTheme.shapes.medium),
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        when (viewType) {
            LibraryViewType.GRID -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(style.contentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LibraryItemImage(
                        artworkUrl,
                        primaryText,
                        style.imageSize,
                        style.imageShape
                    )
                    Spacer(modifier = Modifier.height(style.spacing))
                    LibraryItemText(
                        primaryText,
                        secondaryText
                    )
                }
            }

            LibraryViewType.LIST -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(style.contentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(style.spacing)
                ) {
                    LibraryItemImage(
                        artworkUrl,
                        primaryText,
                        80.dp,
                        style.imageShape
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LibraryItemText(
                            primaryText,
                            secondaryText
                        )
                        additionalText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            LibraryViewType.TABLE -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LibraryItemImage(
                        artworkUrl = artworkUrl,
                        contentDescription = primaryText,
                        size = 80.dp,
                        shape = MaterialTheme.shapes.small
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(0.4f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = primaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        additionalText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    secondaryText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.4f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryItemImage(
    artworkUrl: String?,
    contentDescription: String,
    size: Dp,
    shape: androidx.compose.ui.graphics.Shape
) {
    AsyncImage(
        model = artworkUrl,
        contentDescription = contentDescription,
        modifier = Modifier.size(size).clip(shape),
        contentScale = ContentScale.Crop,
        placeholder = rememberVectorPainter(FeatherIcons.Image)
    )
}

@Composable
private fun LibraryItemText(
    primaryText: String,
    secondaryText: String?
) {
    Text(
        text = primaryText,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )

    secondaryText?.takeIf { it.isNotBlank() }?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
