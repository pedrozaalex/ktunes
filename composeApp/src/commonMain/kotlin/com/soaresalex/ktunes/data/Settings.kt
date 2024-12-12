package com.soaresalex.ktunes.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.russhwolf.settings.Settings

/**
 * Creates a persistent settings-backed mutable state that automatically
 * synchronizes between UI state and stored settings.
 *
 * @param settings The Settings instance to use for storage
 * @param key The key under which the value will be stored
 * @param defaultValue The initial value if no stored value exists
 * @return A mutable property that keeps UI and settings in sync
 */
@Composable
fun <T> Settings.observableState(
    key: String,
    defaultValue: T
): MutableState<T> {
    val storedValue = getOrDefault(
        key,
        defaultValue
    )
    val state = remember { mutableStateOf(storedValue) }

    LaunchedEffect(state.value) { // Update settings when UI state changes
        update(
            key,
            state.value
        )
    }

    return state
}

// Helper extension function to handle different types safely
private fun <T> Settings.update(
    key: String,
    value: T
) {
    when (value) {
        is String -> putString(
            key,
            value
        )

        is Int -> putInt(
            key,
            value
        )

        is Long -> putLong(
            key,
            value
        )

        is Boolean -> putBoolean(
            key,
            value
        )

        is Float -> putFloat(
            key,
            value
        )

        is Double -> putDouble(
            key,
            value
        )

        else -> error("Unsupported type for settings storage")
    }
}

// Helper extension function to retrieve default value safely
private fun <T> Settings.getOrDefault(
    key: String,
    defaultValue: T
): T {
    return when (defaultValue) {
        is String -> getString(
            key,
            defaultValue as String
        ) as T

        is Int -> getInt(
            key,
            defaultValue as Int
        ) as T

        is Long -> getLong(
            key,
            defaultValue as Long
        ) as T

        is Boolean -> getBoolean(
            key,
            defaultValue as Boolean
        ) as T

        is Float -> getFloat(
            key,
            defaultValue as Float
        ) as T

        is Double -> getDouble(
            key,
            defaultValue as Double
        ) as T

        else -> error("Unsupported type for settings retrieval")
    }
}