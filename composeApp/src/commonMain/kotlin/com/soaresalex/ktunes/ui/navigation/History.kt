package com.soaresalex.ktunes.ui.navigation

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent

@Stable
class History : KoinComponent {
    private var navigator: Navigator? = null

    private val _navigationHistory = MutableStateFlow<List<Screen>>(emptyList())
    val navigationHistory: StateFlow<List<Screen>> = _navigationHistory.asStateFlow()

    private val _currentHistoryIndex = MutableStateFlow(-1)
    val currentHistoryIndex: StateFlow<Int> = _currentHistoryIndex.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen?>(null)
    val currentScreen: StateFlow<Screen?> = _currentScreen.asStateFlow()

    private fun updateHistory(screen: Screen) {
        Logger.d("updateHistory: $screen")

        // If we're not at the end of history, remove forward history
        val currentHistory = if (_currentHistoryIndex.value < _navigationHistory.value.size - 1) {
            _navigationHistory.value.subList(0, _currentHistoryIndex.value + 1)
        } else {
            _navigationHistory.value
        }

        // Add the new screen
        val newHistory = currentHistory + screen
        _navigationHistory.value = newHistory

        // Update the current index and screen
        _currentHistoryIndex.value = newHistory.size - 1
        _currentScreen.value = screen
    }

    fun init(navigator: Navigator) {
        this.navigator = navigator
        updateHistory(navigator.lastItem)
    }

    fun navigateBack(): Boolean {
        if (canGoBack()) {
            _currentHistoryIndex.value--
            val previousScreen = _navigationHistory.value[_currentHistoryIndex.value]
            navigator?.replace(previousScreen)
            _currentScreen.value = previousScreen
            return true
        }
        return false
    }

    fun navigateForward(): Boolean {
        if (canGoForward()) {
            _currentHistoryIndex.value++
            val nextScreen = _navigationHistory.value[_currentHistoryIndex.value]
            navigator?.replace(nextScreen)
            _currentScreen.value = nextScreen
            return true
        }
        return false
    }

    fun navigateTo(screen: Screen) {
        navigator?.replace(screen)
        updateHistory(screen)
    }

    fun canGoBack(): Boolean {
        Logger.d("canGoBack: Current history index: ${_currentHistoryIndex.value}")
        return _currentHistoryIndex.value > 0
    }

    fun canGoForward(): Boolean {
        Logger.d("canGoForward: Current history index: ${_currentHistoryIndex.value}")
        return _currentHistoryIndex.value < _navigationHistory.value.size - 1
    }
}