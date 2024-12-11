package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Track
import org.koin.core.component.KoinComponent

/**
 * Basic implementation of MetadataService that does minimal external metadata fetching
 */
class NullMetadataService : MetadataService, KoinComponent {
    override suspend fun fetchMetadata(filename: String): Track? {
        // Placeholder implementation
        // In a real app, this might use an online service like MusicBrainz, Spotify API, etc.
        return null
    }
}