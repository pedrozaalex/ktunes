package com.soaresalex.ktunes.repositories

import com.soaresalex.ktunes.data.Album
import com.soaresalex.ktunes.data.Artist
import com.soaresalex.ktunes.data.Playlist
import com.soaresalex.ktunes.data.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile

// Local filesystem-based library repository
class LocalFilesystemLibraryRepository(
    private val rootDirectory: Path = Path(System.getProperty("user.home"), "Music")
) : LibraryRepository {

    private val supportedFileExtensions = listOf(".mp3", ".flac", ".wav", ".m4a", ".ogg")

    // Utility to generate unique ID from file path
    private fun generateId(file: Path): String = file.toString().hashCode().toString()

    // Extract metadata from filename (assuming format: Artist - Title.ext)
    private fun extractMetadataFromFilename(filename: String): Pair<String, String> {
        val parts = filename.substringBeforeLast(".").split(" - ")
        return when {
            parts.size == 2 -> parts[0] to parts[1]
            else -> "Unknown Artist" to filename.substringBeforeLast(".")
        }
    }

    // Scan audio files in the root directory and subdirectories
    private suspend fun scanAudioFiles(): List<Track> = withContext(Dispatchers.IO) {
        Files.walk(rootDirectory).filter { path ->
            path.isRegularFile() && path.extension in supportedFileExtensions.map { it.removePrefix(".") }
        }.map { path ->
            val (artist, title) = extractMetadataFromFilename(path.fileName.toString())
            Track(
                title = title,
                artist = artist,
                duration = 0, // TODO: Implement audio duration extraction
            )
        }.toList()
    }

    // Scan and group tracks into albums
    private suspend fun scanAlbums(tracks: List<Track>): List<Album> = withContext(Dispatchers.IO) {
        tracks.groupBy { it.artist }.map { (artistName, artistTracks) ->
            Album(
                title = "Album by $artistName", artist = artistName, tracks = artistTracks
            )
        }
    }

    // Scan and create artists
    private suspend fun scanArtists(albums: List<Album>): List<Artist> = withContext(Dispatchers.IO) {
        albums.groupBy { it.artist }.map { (artistName, artistAlbums) ->
            Artist(
                name = artistName, albums = artistAlbums
            )
        }
    }

    override suspend fun getAllTracks(): List<Track> {
        return scanAudioFiles()
    }

    override suspend fun getAllAlbums(): List<Album> {
        val tracks = scanAudioFiles()
        return scanAlbums(tracks)
    }

    override suspend fun getAllArtists(): List<Artist> {
        val tracks = scanAudioFiles()
        val albums = scanAlbums(tracks)
        return scanArtists(albums)
    }

    override suspend fun getAllPlaylists(): List<Playlist> {
        // For local filesystem, we'll create a special playlist of recent files
        val tracks = scanAudioFiles().take(20)
        return listOf(
            Playlist(
                name = "Recent Tracks", creator = "System", description = "Recently accessed tracks", tracks = tracks
            )
        )
    }

    // Search implementations
    override suspend fun searchTracks(query: String): List<Track> {
        return scanAudioFiles().filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
    }

    override suspend fun searchAlbums(query: String): List<Album> {
        val tracks = searchTracks(query)
        return scanAlbums(tracks)
    }

    override suspend fun searchArtists(query: String): List<Artist> {
        val tracks = searchTracks(query)
        val albums = scanAlbums(tracks)
        return scanArtists(albums).filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    override suspend fun searchPlaylists(query: String): List<Playlist> {
        return getAllPlaylists().filter {
            it.name.contains(query, ignoreCase = true) || it.description?.contains(query, ignoreCase = true) == true
        }
    }
}
