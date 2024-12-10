package com.soaresalex.ktunes.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator

object MainScreen : Screen {
    @Composable
    override fun Content() =
        Box(
            Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(8.dp))
        ) {
            Navigator(LibraryScreen) {
                CurrentScreen()
            }
        }
}
