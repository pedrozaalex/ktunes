package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Track

/**
 * Interface for fetching additional metadata for tracks
 */
interface MetadataService {
    /**
     * Fetch metadata for a given filename
     * @param filename The name of the file to fetch metadata for
     * @return Metadata information, or null if not found
     */
    suspend fun fetchMetadata(filename: String): Track?
}
