package com.soaresalex.ktunes.plugins

import com.soaresalex.ktunes.config.PluginsConfig
import com.soaresalex.ktunes.data.GitHubClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pf4j.DefaultPluginManager
import org.pf4j.PluginDescriptor
import org.pf4j.PluginState
import org.pf4j.update.DefaultUpdateRepository
import org.pf4j.update.UpdateManager
import java.io.File
import java.net.URI

class KTunesPluginManager() {
    private val pluginManager = DefaultPluginManager()
    private lateinit var updateManager: UpdateManager

    init {
        pluginManager.loadPlugins()
        pluginManager.startPlugins()
    }

    /**
     * Setup update repositories based on GitHub topics
     */
    fun setupUpdateRepositories() {
        val audioPluginRepos = GitHubClient.searchRepositoriesByTopic(
            PluginsConfig.Sources.GitHub.Topics.AUDIO
        ).map { repo ->
            val repoUrl = URI("${repo.htmlUrl}/releases").toURL()
            DefaultUpdateRepository(repo.name, repoUrl)
        }

        updateManager = UpdateManager(pluginManager, audioPluginRepos)
    }

    /**
     * Find available audio service plugins
     */
    fun findAudioServicePlugins(): List<AudioServicePlugin> {
        return pluginManager.plugins.filter { it.pluginState == PluginState.STARTED }.mapNotNull { pluginWrapper ->
            pluginWrapper.plugin as? AudioServicePlugin
        }
    }

    /**
     * Download and install a plugin from a descriptor
     */
    suspend fun installPlugin(descriptor: PluginDescriptor): Boolean = withContext(Dispatchers.IO) {
        try {
            // Validate and download plugin
            val pluginFile = downloadPlugin(descriptor)

            // Load and start the plugin
            val pluginId = pluginManager.loadPlugin(pluginFile.toPath())
            pluginManager.startPlugin(pluginId)

            true
        } catch (e: Exception) {
            // Log or handle installation errors
            false
        }
    }

    /**
     * Download plugin JAR with checksum verification
     */
    private suspend fun downloadPlugin(descriptor: PluginDescriptor): File = withContext(Dispatchers.IO) {
        val pluginFile = File("${descriptor.pluginId}.jar")

        // Download logic using Ktor or your existing GitHubClient
//        val downloadUrl = descriptor.getDownloadUrl()
//        val checksumUrl = descriptor.getChecksumUrl()

        // Implement download and checksum verification similar to original implementation
        // Use your existing checksum logic here

        pluginFile
    }

    /**
     * Update installed plugins
     */
    suspend fun updatePlugins(pluginsToUpdate: List<PluginDescriptor>) = withContext(Dispatchers.IO) {
        pluginsToUpdate.forEach { descriptor ->
            pluginManager.stopPlugin(descriptor.pluginId)
            installPlugin(descriptor)
        }
    }
}