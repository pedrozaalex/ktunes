package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.data.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalMediaService(
    private val metadataService: MetadataService
) : MediaService {
    /**
     * Configuration for media library location and scanning
     */
    data class LibraryConfig(
        var libraryFolderPath: String = defaultLibraryPath()
    ) {
        companion object {
            fun defaultLibraryPath(): String = when {
                System.getProperty("os.name").lowercase().contains("win") -> "${System.getProperty("user.home")}\\Music"

                System.getProperty("os.name").lowercase().contains("mac") -> "${System.getProperty("user.home")}/Music"

                else -> "${System.getProperty("user.home")}/Music"
            }
        }

        /**
         * Update the library folder path
         * @param newPath The new path to set as the library folder
         */
        fun updateLibraryPath(newPath: String) {
            libraryFolderPath = newPath
        }
    }

    private val libraryConfig = LibraryConfig()

    override suspend fun getTracks(): List<Track> = withContext(Dispatchers.IO) {
        val mediaFiles = scanMediaFiles()
        mediaFiles.map { file ->
            extractTrackMetadata(file)
        }
    }

    override suspend fun getAlbums(): List<Album> {
        val tracks = getTracks()
        return tracks.groupBy { it.album }.map { (albumTitle, albumTracks) ->
            Album(
                id = albumTitle.hashCode().toString(),
                title = albumTitle,
                artist = albumTracks.firstOrNull()?.artist ?: "Unknown Artist",
                releaseYear = albumTracks.firstOrNull()?.year,
                coverArtUri = albumTracks.firstOrNull()?.albumArtUri,
                trackCount = albumTracks.size,
                totalDuration = albumTracks.sumOf { it.duration })
        }
    }

    override suspend fun getArtists(): List<Artist> {
        val tracks = getTracks()
        return tracks.groupBy { it.artist }.map { (artistName, artistTracks) ->
            Artist(
                id = artistName.hashCode().toString(),
                name = artistName,
                albumCount = artistTracks.map { it.album }.distinct().size,
                trackCount = artistTracks.size
            )
        }
    }

    private fun scanMediaFiles(): List<File> {
        val rootDir = File(libraryConfig.libraryFolderPath)
        return rootDir.walkTopDown().filter { it.isFile }
            .filter { it.extension.lowercase() in SUPPORTED_AUDIO_EXTENSIONS }.toList()
    }

    private suspend fun extractTrackMetadata(file: File): Track {
        // First try to extract metadata from the file directly
        val fileMetadata = try {
            extractLocalFileMetadata(file)
        } catch (e: Exception) {
            null
        }

        // If local metadata extraction fails, try online metadata service
        val enhancedMetadata = fileMetadata ?: run {
            try {
                metadataService.fetchMetadata(file.name)
            } catch (e: Exception) {
                null
            }
        }

        return Track(
            id = file.absolutePath.hashCode().toString(),
            title = enhancedMetadata?.title ?: file.nameWithoutExtension,
            artist = enhancedMetadata?.artist ?: "Unknown Artist",
            album = enhancedMetadata?.album ?: "Unknown Album",
            duration = enhancedMetadata?.duration ?: 0L,
            fileUri = file.absolutePath,
            albumArtUri = enhancedMetadata?.albumArtUri,
            trackNumber = enhancedMetadata?.trackNumber,
            year = enhancedMetadata?.year
        )
    }

    private fun extractLocalFileMetadata(file: File): Track? {
        // Platform-specific metadata extraction would go here
        // This is a placeholder and would need platform-specific implementations
        return null
    }

    companion object {
        private val SUPPORTED_AUDIO_EXTENSIONS = setOf(
            "mp3", "flac", "wav", "ogg", "m4a", "aac", "wma"
        )
    }
}