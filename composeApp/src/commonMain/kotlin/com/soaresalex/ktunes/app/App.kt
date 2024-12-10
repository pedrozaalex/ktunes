package com.soaresalex.ktunes.app

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.soaresalex.ktunes.theme.AppTheme
import com.soaresalex.ktunes.ui.components.CloseButton
import com.soaresalex.ktunes.ui.components.MenuButton
import com.soaresalex.ktunes.ui.components.NavigateBackButton
import com.soaresalex.ktunes.ui.screens.MainScreen

@Composable
fun TitleBar(
) = Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.height(24.dp).fillMaxWidth().padding(vertical = 2.dp)
) {
    NavigateBackButton()

    Row {
        MenuButton()
        CloseButton()
    }
}


@Composable
fun App(
    titlebarContainer: @Composable (content: @Composable () -> Unit) -> Unit = { },
) = AppTheme {
    Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        titlebarContainer({
            TitleBar()
        })

        Navigator(MainScreen) { CurrentScreen() }
    }
}