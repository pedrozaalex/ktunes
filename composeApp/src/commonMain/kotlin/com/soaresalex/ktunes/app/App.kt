package com.soaresalex.ktunes.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.Settings
import com.soaresalex.ktunes.data.observableState
import com.soaresalex.ktunes.theme.AppTheme
import com.soaresalex.ktunes.ui.components.CloseButton
import com.soaresalex.ktunes.ui.components.NavigationControls
import com.soaresalex.ktunes.ui.components.SearchBar
import com.soaresalex.ktunes.ui.components.SettingsButton
import com.soaresalex.ktunes.ui.navigation.History
import com.soaresalex.ktunes.ui.screens.library.AlbumsScreen
import com.soaresalex.ktunes.ui.screens.library.ArtistsScreen
import com.soaresalex.ktunes.ui.screens.library.TracksScreen
import compose.icons.FeatherIcons
import compose.icons.feathericons.Disc
import compose.icons.feathericons.Music
import compose.icons.feathericons.Users
import org.koin.compose.koinInject

@Composable
fun TitleBar() = Row(
	Modifier.height(40.dp).fillMaxWidth().padding(horizontal = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically
) {
	Row {
		NavigationControls()
	}

	Row(verticalAlignment = Alignment.CenterVertically) {
		SettingsButton()
		Spacer(Modifier.width(8.dp))
		CloseButton()
	}
}

val sidebarItemData = listOf(
	SidebarItemData(
		"Tracks", FeatherIcons.Music, TracksScreen
	),
	SidebarItemData(
		"Albums", FeatherIcons.Disc, AlbumsScreen
	),
	SidebarItemData(
		"Artists", FeatherIcons.Users, ArtistsScreen
	),
)

@Composable
fun App(
	titlebarContainer: @Composable (content: @Composable () -> Unit) -> Unit = { },
) = AppTheme {
	val settings: Settings = koinInject()
	val history: History = koinInject()

	var sidebarWidth: Int by settings.observableState(
		"sidebarWidth", 200
	)

	Column(
		Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		titlebarContainer {
			TitleBar()
		}

		Navigator(TracksScreen) { nav ->
			LaunchedEffect(nav) { history.init(nav) }

			Row(Modifier.fillMaxSize().padding(4.dp)) {
				Sidebar(
					width = sidebarWidth.dp,
					items = sidebarItemData,
				)

				Box(
					Modifier.draggable(
							orientation = Orientation.Horizontal, state = rememberDraggableState { delta ->
								sidebarWidth += delta.toInt()
							}).pointerHoverIcon(PointerIcon.Hand).width(8.dp).fillMaxHeight()
				)

				Box(
					Modifier.fillMaxSize().background(
							MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.medium
						)
				) { CurrentScreen() }
			}
		}

	}
}


data class SidebarItemData(
	val title: String, val icon: ImageVector, val screen: Screen
)

@Composable
fun Sidebar(
	width: Dp, items: List<SidebarItemData>
) {
	Column(
		Modifier.fillMaxHeight().width(width)
	) {
		SearchBar(
			modifier = Modifier.fillMaxWidth(), placeholder = "Search Library"
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = "LIBRARY", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Spacer(modifier = Modifier.height(8.dp))

		LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			items(items) { SidebarItem(it) }
		}
	}
}

@Composable
fun SidebarItem(
	item: SidebarItemData
) {
	val history: History = koinInject()

	val handleClick = { history.navigateTo(item.screen) }

	val currentScreen by history.currentScreen.collectAsState()
	val isSelected by remember { derivedStateOf { currentScreen == item.screen } }
	val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
	val color =
		if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

	Row(
		Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable(onClick = handleClick).background(
				bgColor, MaterialTheme.shapes.small
			).padding(8.dp), verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			item.icon, item.title, tint = color
		)

		Spacer(Modifier.width(8.dp))

		Text(
			item.title, color = color
		)
	}
}