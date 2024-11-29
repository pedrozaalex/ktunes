package com.soaresalex.ktunes.plugins

import com.russhwolf.settings.Settings
import io.github.classgraph.ClassGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kohsuke.github.GitHub
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import org.koin.core.component.KoinComponent
import java.net.URI

/**
 * Manages plugin discovery, loading, and lifecycle
 */
class PluginManager(
    private val settings: Settings
) : KoinComponent {
    // Concurrent map to store loaded plugins
    private val loadedPlugins = ConcurrentHashMap<String, AudioSourcePlugin>()

    // Plugin directory
    private val pluginDirectory = File("plugins").also { it.mkdirs() }

    /**
     * Discover and load local plugins from JAR files
     */
    suspend fun discoverLocalPlugins() = withContext(Dispatchers.IO) {
        ClassGraph()
            .enableClassInfo()
            .scan()
            .use { scanResult ->
                scanResult.getSubclasses(AudioSourcePlugin::class.java.name)
                    .map { it.loadClass(AudioSourcePlugin::class.java) }
                    .filter { !it.isInterface }
                    .forEach { pluginClass ->
                        val plugin = pluginClass.getDeclaredConstructor().newInstance() as AudioSourcePlugin
                        loadedPlugins[plugin.pluginId] = plugin
                        restorePluginConfiguration(plugin, settings)
                    }
            }
    }

    /**
     * Restore plugin configuration using simple key-value storage
     */
    private fun restorePluginConfiguration(plugin: AudioSourcePlugin, settings: Settings) {
        val configMap = mutableMapOf<String, Any>()

        // Restore settings based on plugin's config options
        plugin.configOptions.forEach { option ->
            val key = "${plugin.pluginId}_${option.key}"

            when (option.type) {
                ConfigOptionType.STRING -> settings.getStringOrNull(key)
                ConfigOptionType.INTEGER -> settings.getIntOrNull(key)
                ConfigOptionType.BOOLEAN -> settings.getBooleanOrNull(key)
                ConfigOptionType.FLOAT -> settings.getFloatOrNull(key)
            }?.let { configMap[option.key] = it }
        }

        // Configure plugin if any settings were restored
        if (configMap.isNotEmpty()) {
            plugin.configure(configMap)
        }
    }

    /**
     * Download and install plugins from GitHub
     * @param githubTopic GitHub topic to search for plugins
     */
    suspend fun discoverAndInstallGitHubPlugins(githubTopic: String = "ktunes-music-source-plugin") =
        withContext(Dispatchers.IO) {
            val github = GitHub.connectAnonymously()

            // Search repositories with the specified topic
            val repos = github.searchRepositories()
                .q("topic:$githubTopic")
                .list()

            repos.forEach { repo ->
                // Find release assets that are JAR files
                val pluginJars = repo.listReleases()
                    .flatMap { it.assets }
                    .filter { it.name.endsWith(".jar") }

                pluginJars.forEach { asset ->
                    val destinationFile = File(pluginDirectory, asset.name)

                    // Download JAR if not already exists
                    if (!destinationFile.exists()) {
                        URI(asset.browserDownloadUrl).toURL().openStream().use { input ->
                            Files.copy(input, destinationFile.toPath())
                        }
                    }
                }
            }

            // Rediscover plugins after download
            discoverLocalPlugins()
        }

    /**
     * Get available plugins
     */
    fun getAvailablePlugins(): List<AudioSourcePlugin> = loadedPlugins.values.toList()

    /**
     * Configure a specific plugin
     * @param pluginId Plugin identifier
     * @param config Configuration map
     */
    fun configurePlugin(pluginId: String, config: Map<String, Any>) {
        val plugin = loadedPlugins[pluginId]
            ?: throw IllegalArgumentException("Plugin $pluginId not found")

        // Apply configuration
        val configSuccess = plugin.configure(config)

        // Persist configuration if successful
        if (configSuccess) {
            // Clear previous settings
            settings.keys.filter { it.startsWith(pluginId) }.forEach { settings.remove(it) }

            // Persist configuration
            config.forEach { (key, value) ->
                val key = "${pluginId}_$key"
                when (value) {
                    is String -> settings.putString(key, value)
                    is Int -> settings.putInt(key, value)
                    is Boolean -> settings.putBoolean(key, value)
                    else -> {
                        // Log or handle unsupported types
                        println("Unsupported configuration type for key $key: ${value.javaClass}")
                    }
                }
            }
        }
    }

    /**
     * Get settings for a specific plugin
     * @param pluginId Plugin identifier
     */
    fun getPluginSettings(pluginId: String): Map<String, Any>? {
        return loadedPlugins[pluginId]?.configOptions?.associate { option ->
            val key = "${pluginId}_${option.key}"
            val value = when (option.type) {
                ConfigOptionType.STRING -> settings.getString(key, "")
                ConfigOptionType.INTEGER -> settings.getInt(key, 0)
                ConfigOptionType.BOOLEAN -> settings.getBoolean(key, false)
                ConfigOptionType.FLOAT -> settings.getFloat(key, 0f)
            }

            option.key to value
        }
    }
}