package com.soaresalex.ktunes.repositories

import com.soaresalex.ktunes.data.Album
import com.soaresalex.ktunes.data.Artist
import com.soaresalex.ktunes.data.Playlist
import com.soaresalex.ktunes.data.Track
import org.koin.core.component.KoinComponent

// Library repository interface for abstracting data source
interface LibraryRepository {
    suspend fun getAllTracks(): List<Track>
    suspend fun getAllAlbums(): List<Album>
    suspend fun getAllArtists(): List<Artist>
    suspend fun getAllPlaylists(): List<Playlist>

    suspend fun searchTracks(query: String): List<Track>
    suspend fun searchAlbums(query: String): List<Album>
    suspend fun searchArtists(query: String): List<Artist>
    suspend fun searchPlaylists(query: String): List<Playlist>
}

// Interface for repository providers
interface LibraryRepositoryProvider {
    fun getRepository(): LibraryRepository
    fun setRepository(repository: LibraryRepository)
}

// Dynamic repository manager
class DynamicLibraryRepositoryProvider : LibraryRepositoryProvider, KoinComponent {
    private var currentRepository: LibraryRepository = LocalFilesystemLibraryRepository()

    override fun getRepository(): LibraryRepository = currentRepository

    override fun setRepository(repository: LibraryRepository) {
        currentRepository = repository
    }
}