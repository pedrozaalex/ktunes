package com.soaresalex.ktunes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.soaresalex.ktunes.theme.LocalThemeIsDark
import compose.icons.FeatherIcons
import compose.icons.feathericons.Search
import compose.icons.feathericons.X

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    maxLines: Int = 1,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isDark by LocalThemeIsDark.current
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val onQueryChange: (TextFieldValue) -> Unit = { query = it }

    // Dynamic icon color calculation
    val iconColor = remember(isFocused, isHovered, isDark) {
        when {
            isFocused -> contentColor
            isHovered -> contentColor.copy(alpha = 0.8f)
            else -> contentColor.copy(alpha = 0.6f)
        }
    }

    // Hover and focus state background calculation
    val dynamicBackgroundColor = remember(isHovered, isFocused, isDark) {
        when {
            isFocused -> backgroundColor.copy(alpha = 0.9f)
            isHovered -> backgroundColor.copy(alpha = 0.85f)
            else -> backgroundColor
        }
    }

    // Animated border color and width
    val borderAnimation by animateFloatAsState(
        targetValue = if (isFocused) 2f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
    )

    val borderColor = remember(isFocused, isDark) {
        if (isFocused) contentColor.copy(alpha = 0.5f) else Color.Transparent
    }

    Box(
        modifier = modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(28.dp))
            .background(dynamicBackgroundColor).border(
                width = borderAnimation.dp, color = borderColor, shape = RoundedCornerShape(28.dp)
            ).hoverable(interactionSource).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart
    ) {
        // Improved Layout: Row for Icon and TextField
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Icon with Dynamic Color
            Box(
                modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FeatherIcons.Search, contentDescription = "Search", tint = iconColor
                )
            }

            // Text Field Container
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                if (query.text.isEmpty() && !isFocused) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged {
                        isFocused = it.isFocused
                    },
                    textStyle = textStyle.copy(color = contentColor),
                    cursorBrush = SolidColor(contentColor),
                    singleLine = maxLines == 1,
                    maxLines = maxLines,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = {}),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart
                        ) {
                            innerTextField()
                        }
                    })
            }

            // Clear Button
            AnimatedVisibility(
                visible = query.text.isNotEmpty(), enter = fadeIn(), exit = fadeOut()
            ) {
                IconButton({ onQueryChange(TextFieldValue("")) }) {
                    Icon(
                        imageVector = FeatherIcons.X,
                        contentDescription = "Clear",
                        tint = contentColor.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}