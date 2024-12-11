package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    style: LibraryItemStyle = LibraryItemStyle(
        120.dp,
        true,
        MaterialTheme.typography.bodyMedium,
        MaterialTheme.typography.bodySmall
    ),
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(style.imageSize)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
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