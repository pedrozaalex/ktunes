package com.soaresalex.ktunes.plugins

import com.soaresalex.ktunes.config.PluginsConfig
import com.soaresalex.ktunes.data.GitHubClient
import com.soaresalex.ktunes.viewmodels.AudioPlaybackService
import io.github.classgraph.ClassGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.security.MessageDigest
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Sealed interface for defining plugin types with type-safe contracts
 */
sealed interface KTunesPlugin {
    val id: String
    val name: String
    val version: String
}

/**
 * Sealed interface for specific plugin types with type-safe contracts
 */
sealed interface TypedPlugin<T> : KTunesPlugin {
    fun getInstance(): T
}

/**
 * Audio Service Plugin with type-safe contract
 */
interface AudioServicePlugin : TypedPlugin<AudioPlaybackService> {
    override fun getInstance(): AudioPlaybackService
}

/**
 * Represents a downloadable and loadable plugin with type-safe properties
 */
data class PluginDescriptor<P : KTunesPlugin>(
    override val id: String,
    override val name: String,
    override val version: String,
    val pluginClass: KClass<P>,
    val jarFileUrl: URL,
    val checksumUrl: URL
) : KTunesPlugin

/**
 * Sealed class to represent plugin types with type-safe contracts
 */
sealed class PluginType<P : KTunesPlugin>(
    val displayName: String, val pluginClass: KClass<P>
) {
    /**
     * Audio Service Plugin Type
     */
    object AudioService : PluginType<AudioServicePlugin>(
        displayName = "Audio Service", pluginClass = AudioServicePlugin::class
    )

    companion object {
        /**
         * Get all available plugin types
         */
        fun getAllTypes(): List<PluginType<*>> = listOf(AudioService)
    }
}

/**
 * Manages the entire lifecycle of plugin discovery, download, validation, and loading
 */
class PluginManager<P : KTunesPlugin>(
    private val pluginsDirectory: File, private val pluginRegistry: PluginRegistry<P>
) {
    // Ensure plugin directory exists
    init {
        pluginsDirectory.mkdirs()
    }

    /**
     * Plugin download and verification process
     */
    suspend fun downloadPlugin(descriptor: PluginDescriptor<P>): File {
        return withContext(Dispatchers.IO) {
            // Create unique filename
            val dir = File(pluginsDirectory, descriptor.id)
            dir.mkdirs()

            val jarFile = File(dir, "${descriptor.name}-${descriptor.version}.jar")

            // Download the plugin
            descriptor.jarFileUrl.openStream().use { input ->
                jarFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Download the checksum file
            val checksumFile = File(dir, "${descriptor.name}-${descriptor.version}.sha256")

            descriptor.checksumUrl.openStream().use { input ->
                checksumFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Verify checksum
            if (calculateFileChecksum(jarFile) != checksumFile.readText()) {
                jarFile.delete()
                checksumFile.delete()
                throw SecurityException("Plugin checksum verification failed")
            }

            jarFile
        }
    }

    /**
     * Load and validate a plugin JAR with strict type checking
     */
    fun loadPlugin(pluginFile: File, pluginType: PluginType<P>): P {
        return ClassGraph().enableClassInfo().overrideClassLoaders(URLClassLoader(arrayOf(pluginFile.toURI().toURL())))
            .scan().use { scanResult ->
                // Find all classes that implement the appropriate interface based on plugin type
                val pluginClasses = scanResult.getClassesImplementing(pluginType.pluginClass.java.name)

                // Instantiate and validate the first found plugin
                pluginClasses.loadClasses().firstOrNull { pluginType.pluginClass.java.isAssignableFrom(it) }
                    ?.let { it.kotlin.createInstance() as P }
                    ?: throw IllegalArgumentException("No valid ${pluginType.displayName} plugin found in the JAR")
            }
    }

    /**
     * Calculate SHA-256 checksum of a file
     */
    private fun calculateFileChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read = input.read(buffer)
            while (read != -1) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * Type-safe plugin registry
     */
    class PluginRegistry<P : KTunesPlugin> {
        private val registeredPlugins = mutableMapOf<String, P>()

        fun registerPlugin(plugin: P) {
            registeredPlugins[plugin.id] = plugin
        }

        fun getPlugin(id: String): P {
            return registeredPlugins[id] ?: throw NoSuchElementException("Plugin not found: $id")
        }

        fun getAllPlugins(): List<P> = registeredPlugins.values.toList()
    }

    /**
     * Plugin marketplace with type-safe plugin discovery and installation
     */
    class PluginMarketplace<P : KTunesPlugin>(
        private val marketplaceUrl: String,
        private val pluginManager: PluginManager<P>,
        private val pluginType: PluginType<P>
    ) {
        fun fetchAvailablePlugins(): List<PluginDescriptor<P>> = GitHubClient.searchRepositoriesByTopic(
            when (pluginType) {
                PluginType.AudioService -> PluginsConfig.Sources.GitHub.Topics.AUDIO
            }
        ).map { repo ->
            val latestRelease = repo.latestRelease
            val assets = latestRelease.assets
            val jarFileUrl = URI(
                assets.firstOrNull { repo.name.endsWith(".jar") }?.browserDownloadUrl ?: throw IllegalArgumentException(
                    "No JAR file found in release assets"
                )
            ).toURL()
            var checksumUrl = URI(
                assets.firstOrNull { repo.name == PluginsConfig.Sources.GitHub.SHA256_CHECKSUM_FILENAME }?.browserDownloadUrl
                    ?: throw IllegalArgumentException("No checksum file found in release assets")
            ).toURL()

            PluginDescriptor(
                id = repo.id.toString(),
                name = repo.name,
                version = latestRelease.tagName,
                pluginClass = pluginType.pluginClass,
                jarFileUrl = jarFileUrl,
                checksumUrl = checksumUrl
            )
        }

        suspend fun installPlugin(descriptor: PluginDescriptor<P>) {
            val pluginFile = pluginManager.downloadPlugin(descriptor)
            val plugin = pluginManager.loadPlugin(pluginFile, pluginType)
            pluginManager.pluginRegistry.registerPlugin(plugin)
        }
    }
}