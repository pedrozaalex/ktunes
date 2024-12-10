package com.soaresalex.ktunes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.soaresalex.ktunes.theme.LocalThemeIsDark
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import kotlin.system.exitProcess

@Composable
fun NavigateBackButton() = IconButton(onClick = { /* Navigate back */ }) {
    Icon(FeatherIcons.ChevronLeft, contentDescription = "Back")
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