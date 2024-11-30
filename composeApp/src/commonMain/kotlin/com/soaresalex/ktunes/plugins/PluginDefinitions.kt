package com.soaresalex.ktunes.plugins

import com.soaresalex.ktunes.viewmodels.AudioPlaybackService
import org.pf4j.Plugin
import org.pf4j.PluginWrapper

/**
 * Base plugin interface for KTunes
 */
interface KTunesPlugin {
    val id: String
    val name: String
    val version: String

    fun getConfigs(): Map<String, String>
    fun applyConfigs(configs: Map<String, String>)
}

/**
 * Audio Service Plugin interface
 */
interface AudioServicePlugin : KTunesPlugin {
    fun createAudioPlaybackService(): AudioPlaybackService
}

/**
 * Abstract base class for Audio Service Plugins
 */
abstract class BaseAudioServicePlugin(wrapper: PluginWrapper) : Plugin(wrapper), AudioServicePlugin
