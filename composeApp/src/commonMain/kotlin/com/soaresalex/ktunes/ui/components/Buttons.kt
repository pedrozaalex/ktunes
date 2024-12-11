package com.soaresalex.ktunes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.soaresalex.ktunes.theme.LocalThemeIsDark
import com.soaresalex.ktunes.ui.navigation.History
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import org.koin.compose.koinInject
import kotlin.system.exitProcess

private sealed class NavDir {
    data object Back : NavDir()
    data object Forward : NavDir()
}

private data class NavButtonProps(
    val icon: ImageVector, val description: String, val action: () -> Unit, val canNavigate: Boolean
)

@Composable
private fun NavButton(
    direction: NavDir,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val history: History = koinInject()

    // Observe the history index to trigger recomposition
    val currentHistoryIndex by history.currentHistoryIndex.collectAsState()

    val props by remember(direction, currentHistoryIndex) {
        derivedStateOf {
            when (direction) {
                NavDir.Back -> NavButtonProps(
                    icon = FeatherIcons.ChevronLeft,
                    description = "Navigate Back",
                    action = { history.navigateBack() },
                    canNavigate = currentHistoryIndex > 0
                )

                NavDir.Forward -> NavButtonProps(
                    icon = FeatherIcons.ChevronRight,
                    description = "Navigate Forward",
                    action = { history.navigateForward() },
                    canNavigate = currentHistoryIndex < history.navigationHistory.value.size - 1
                )
            }
        }
    }

    FilledIconButton(
        onClick = props.action,
        enabled = props.canNavigate,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor, contentColor),
        modifier = modifier
    ) {
        Icon(
            imageVector = props.icon, contentDescription = props.description
        )
    }
}

@Composable
fun NavigationButtons() {
    NavButton(NavDir.Back)
    NavButton(NavDir.Forward)
}

@Composable
fun ThemeToggleButton() {
    var isDark by LocalThemeIsDark.current

    IconButton(onClick = { isDark = !isDark }) {
        Icon(
            imageVector = when {
                isDark -> FeatherIcons.Sun
                else -> FeatherIcons.Moon
            }, contentDescription = "Toggle Theme"
        )
    }
}

@Composable
fun SettingsButton() = IconButton(onClick = { /* Open settings */ }) {
    Icon(FeatherIcons.Settings, contentDescription = "Settings")
}

@Composable
fun CloseButton() = IconButton(
    onClick = { exitProcess(0) }) {
    Icon(
        Icons.Outlined.Close, "Close"
    )
}

@Composable
fun MenuButton() = IconButton({ }) { Icon(FeatherIcons.Menu, "Menu") }