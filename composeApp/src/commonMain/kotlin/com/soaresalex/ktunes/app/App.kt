package com.soaresalex.ktunes.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.soaresalex.ktunes.ui.components.*
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
				PermanentNavigationDrawer(
					drawerContent = {
						PermanentDrawerSheet(Modifier.width(sidebarWidth.dp)) {
							Column(Modifier.verticalScroll(rememberScrollState())) {
								SearchBar(
									modifier = Modifier.fillMaxWidth(), placeholder = "Search Library"
								)

								Spacer(modifier = Modifier.height(16.dp))

								Text(
									text = "LIBRARY",
									fontWeight = FontWeight.Bold,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)

								Spacer(modifier = Modifier.height(8.dp))

								sidebarItemData.forEach {
									NavigationDrawerItem(
										icon = { Icon(it.icon, it.title) },
										label = { Text(it.title) },
										selected = history.currentScreen.collectAsState().value == it.screen,
										onClick = { history.navigateTo(it.screen) },
										modifier = Modifier.height(40.dp).padding(2.dp)
									)
								}
							}
						}
					}) {
					Row {
						Box(
							Modifier.width(8.dp).fillMaxHeight().draggable(
									rememberDraggableState { sidebarWidth += it.toInt() }, Orientation.Horizontal
								).pointerHoverIcon(PointerIcon.Hand)
						) { }

						Box(
							Modifier.fillMaxSize().background(
								MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.medium
							).clip(MaterialTheme.shapes.medium)
						) { CurrentScreen() }
					}
				}
			}
		}

	}
}

@Composable
fun TitleBar() = Row(
	Modifier.height(48.dp).fillMaxWidth().padding(horizontal = 8.dp),
	Arrangement.SpaceBetween,
	Alignment.CenterVertically
) {
	Row {
		NavigationControls()
	}

	PlaybackControls()

	Row(verticalAlignment = Alignment.CenterVertically) {
		SettingsButton()
		Spacer(Modifier.width(8.dp))
		CloseButton()
	}
}


data class SidebarItemData(
	val title: String, val icon: ImageVector, val screen: Screen
)

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
fun Sidebar(
	width: Dp, items: List<SidebarItemData>
) {
//	Column(
//		Modifier.fillMaxHeight().width(width)
//	) {
//		SearchBar(
//			modifier = Modifier.fillMaxWidth(), placeholder = "Search Library"
//		)
//
//		Spacer(modifier = Modifier.height(16.dp))
//
//		Text(
//			text = "LIBRARY", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant
//		)
//
//		Spacer(modifier = Modifier.height(8.dp))
//
//		LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//			items(items) { SidebarItem(it) }
//		}
//	}


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