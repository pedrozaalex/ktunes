package com.soaresalex.ktunes.plugins

import kotlin.reflect.KClass

sealed class PluginType(
    val displayName: String,
    val pluginClass: KClass<*>
) {
    object AudioService : PluginType(
        displayName = "Audio Service",
        pluginClass = AudioServicePlugin::class
    )

    companion object {
        fun getAllTypes(): List<PluginType> = listOf(AudioService)
    }
}