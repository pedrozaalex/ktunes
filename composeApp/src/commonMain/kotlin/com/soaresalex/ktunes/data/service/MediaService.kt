package com.soaresalex.ktunes.data.service

import com.soaresalex.ktunes.data.models.Album
import com.soaresalex.ktunes.data.models.Artist
import com.soaresalex.ktunes.data.models.Track

/**
 * Interface defining media retrieval capabilities
 */
interface MediaService {
	/**
	 * Retrieve tracks from the media source
	 * @return Flow of tracks
	 */
	suspend fun getTracks(): List<Track>

	/**
	 * Retrieve albums from the media source
	 * @return Flow of albums
	 */
	suspend fun getAlbums(): List<Album>

	/**
	 * Retrieve artists from the media source
	 * @return Flow of artists
	 */
	suspend fun getArtists(): List<Artist>
}
