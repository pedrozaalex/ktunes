package com.soaresalex.ktunes.ui.screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.soaresalex.ktunes.ui.navigation.LibraryView

object LibraryScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(LibraryView.TracksView) {
            CurrentTab()
        }
    }
}