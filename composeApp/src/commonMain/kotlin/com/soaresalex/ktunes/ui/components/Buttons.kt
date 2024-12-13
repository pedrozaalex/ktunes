package com.soaresalex.ktunes.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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

const val SIZE = 24

@Composable
private fun NavButton(
	direction: NavDir,
	containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
	contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
	val history: History = koinInject()

	// Observe the history index to trigger recomposition
	val currentHistoryIndex by history.currentHistoryIndex.collectAsState()

	val props by remember(
		direction, currentHistoryIndex
	) {
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

	IconButton(
		onClick = props.action,
		modifier = Modifier.size(SIZE.dp),
		enabled = props.canNavigate,
	) {
		Icon(
			imageVector = props.icon,
			contentDescription = props.description,
		)
	}
}

@Composable
fun NavigationControls() {
	NavButton(NavDir.Back)
	Spacer(Modifier.size(4.dp))
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
fun SettingsButton() = IconButton(onClick = { /* Open settings */ }, modifier = Modifier.size(SIZE.dp)) {
	Icon(
		FeatherIcons.Settings, contentDescription = "Settings", Modifier.padding(3.dp)
	)
}

@Composable
fun CloseButton() = FilledIconButton(
	onClick = { exitProcess(0) }, modifier = Modifier.size(20.dp), colors = IconButtonDefaults.filledIconButtonColors(
		containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
		contentColor = MaterialTheme.colorScheme.onSurface
	)
) {
	Icon(
		Icons.Outlined.Close, "Close", Modifier.padding(2.dp)
	)
}

@Composable
fun MenuButton() = IconButton(
	onClick = { /* Open menu */ }, modifier = Modifier.size(SIZE.dp)
) {
	Icon(
		FeatherIcons.Menu, "Menu"
	)
}