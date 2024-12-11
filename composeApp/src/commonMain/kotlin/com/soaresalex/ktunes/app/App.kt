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
import com.russhwolf.settings.int
import com.soaresalex.ktunes.theme.AppTheme
import com.soaresalex.ktunes.ui.components.CloseButton
import com.soaresalex.ktunes.ui.components.MenuButton
import com.soaresalex.ktunes.ui.components.NavigationButtons
import com.soaresalex.ktunes.ui.components.SearchBar
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
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.height(24.dp).fillMaxWidth().padding(vertical = 2.dp, horizontal = 0.dp)
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        NavigationButtons()
    }
    Row {
        MenuButton()
        CloseButton()
    }
}


@Composable
fun App(
    titlebarContainer: @Composable (content: @Composable () -> Unit) -> Unit = { },
) = AppTheme {
    val settings: Settings = koinInject()
    val history: History = koinInject()

    var sidebarWidth: Int by settings.int("sidebarWidth", 200)
    var _sidebarWidth by remember { mutableStateOf(sidebarWidth) }

    LaunchedEffect(_sidebarWidth) {
        sidebarWidth = _sidebarWidth
    }

    Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        titlebarContainer {
            TitleBar()
        }

        val sidebarItemData = listOf(
            SidebarItemData("Tracks", FeatherIcons.Music, TracksScreen),
            SidebarItemData("Albums", FeatherIcons.Disc, AlbumsScreen),
            SidebarItemData("Artists", FeatherIcons.Users, ArtistsScreen),
        )

        Navigator(TracksScreen) { nav ->
            LaunchedEffect(nav) { history.init(nav) }

            Row(Modifier.fillMaxSize()) {
                Sidebar(
                    width = _sidebarWidth.dp,
                    items = sidebarItemData,
                )

                Box(
                    Modifier.draggable(
                        orientation = Orientation.Horizontal, state = rememberDraggableState { delta ->
                            _sidebarWidth += delta.toInt()
                        }).pointerHoverIcon(PointerIcon.Hand).width(4.dp).fillMaxHeight()
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
        Modifier.fillMaxHeight().width(width).padding(8.dp)
    ) {
        SearchBar(modifier = Modifier.fillMaxWidth(), placeholder = "Search Library")

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
        Modifier.fillMaxWidth()

            .clip(MaterialTheme.shapes.small)

            .clickable(onClick = handleClick)

            .background(bgColor, MaterialTheme.shapes.small)

            .padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(item.icon, item.title, tint = color)

        Spacer(Modifier.width(8.dp))

        Text(item.title, color = color)
    }
}