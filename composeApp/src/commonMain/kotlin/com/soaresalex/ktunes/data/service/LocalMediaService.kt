package com.soaresalex.ktunes.data.service

import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.data.models.Track
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.Artwork
import java.io.File

class LocalMediaService(
    private val metadataService: MetadataService,
    private val settings: Settings
) : MediaService {
    private val libraryPath: String
        get() = settings.getString(
            LIBRARY_PATH_KEY,
            defaultLibraryPath()
        )

    fun updateLibraryPath(newPath: String) {
        require(File(newPath).isDirectory) { "Specified path must be a valid directory" }
        settings.putString(
            LIBRARY_PATH_KEY,
            newPath
        )
    }

    override suspend fun getTracks(): List<Track> {
        val files = scanMediaFiles()

        return files.mapNotNull { file ->
            runCatching {
                extractTrackMetadata(file)
            }.getOrElse {
                Logger.e(
                    "Error extracting metadata from file: ${file.name}",
                    it
                )
                null
            }
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
        val rootDir = File(libraryPath)
        return rootDir.walkTopDown().filter { it.isFile && it.extension.lowercase() in SUPPORTED_AUDIO_EXTENSIONS }
            .toList()
    }

    private suspend fun extractTrackMetadata(file: File): Track {
        val localMetadata = extractLocalFileMetadata(file)
        val enhancedMetadata = localMetadata ?: runCatching {
            metadataService.fetchMetadata(file.name)
        }.getOrNull()

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
        return try {
            MediaFileReader(file).let {
                Track(
                    id = file.absolutePath.hashCode().toString(),
                    title = it.getTitle() ?: file.nameWithoutExtension,
                    artist = it.getArtist() ?: "Unknown Artist",
                    album = it.getAlbum() ?: "Unknown Album",
                    duration = it.getDuration(),
                    albumArtUri = it.getAlbumArtUri(),
                    trackNumber = it.getTrackNumber(),
                    year = it.getYear() ?: 0,
                    fileUri = file.absolutePath
                )
            }
        } catch (e: Exception) {
            Logger.e(
                "Error extracting metadata from file: ${file.name}",
                e
            )
            null
        }
    }

    class MediaFileReader(private val file: File) {
        private val audioFile by lazy { AudioFileIO.read(file) }
        private val tag by lazy { audioFile.tag }

        private fun getTagValue(fieldKey: FieldKey): String? {
            return tag.getFirst(fieldKey).takeIf { !it.isNullOrBlank() }
        }

        fun getTitle(): String? = getTagValue(FieldKey.TITLE)
        fun getArtist(): String? = getTagValue(FieldKey.ARTIST)
        fun getAlbum(): String? = getTagValue(FieldKey.ALBUM)

        fun getDuration(): Long = (audioFile.audioHeader.preciseTrackLength * 1000).toLong()

        fun getAlbumArtUri(): String? {
            return try { // First, try to get the image URL if available
                tag.firstArtwork?.imageUrl?.takeIf { it.isNotBlank() } ?: processArtworkBinaryData()
            } catch (e: Exception) {
                Logger.w("Error retrieving album artwork: ${e.message}")
                null
            }
        }

        private fun processArtworkBinaryData(): String? {
            val artwork = tag.firstArtwork ?: return null

            // Check if artwork is linked or contains binary data
            return when {
                artwork.isLinked -> artwork.imageUrl

                artwork.binaryData != null -> { // Save the binary data to a temporary file
                    createTempArtworkFile(artwork)
                }

                else -> null
            }
        }

        private fun createTempArtworkFile(artwork: Artwork): String? {
            return try { // Determine file extension based on MIME type
                val extension = when (artwork.mimeType?.lowercase()) {
                    "image/jpeg" -> ".jpg"
                    "image/png" -> ".png"
                    "image/gif" -> ".gif"
                    else -> ".img"
                }

                // Create a temporary file in the system's temp directory
                val tempFile = File.createTempFile(
                    "album_art_",
                    extension,
                    File(System.getProperty("java.io.tmpdir"))
                )
                tempFile.deleteOnExit() // Ensure temporary file is deleted when JVM exits

                // Write binary data to the temporary file
                tempFile.writeBytes(artwork.binaryData)

                // Return the absolute path of the temporary file
                tempFile.absolutePath
            } catch (e: Exception) {
                Logger.w("Failed to create temporary artwork file: ${e.message}")
                null
            }
        }

        fun getTrackNumber(): Int? = getTagValue(FieldKey.TRACK)?.toIntOrNull()

        fun getYear(): Int? = getTagValue(FieldKey.YEAR)?.toIntOrNull()
    }


    companion object {
        private val SUPPORTED_AUDIO_EXTENSIONS = setOf(
            "mp3",
            "flac",
            "wav",
            "ogg",
            "m4a",
            "aac",
            "wma"
        )

        private const val LIBRARY_PATH_KEY = "library_path"

        private fun defaultLibraryPath(): String = when {
            System.getProperty("os.name").lowercase().contains("win") -> "${System.getProperty("user.home")}\\Music"

            else -> "${System.getProperty("user.home")}/Music"
        }
    }
}