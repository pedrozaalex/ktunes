package com.soaresalex.ktunes.config

import com.soaresalex.ktunes.BuildConfig

/**
 * Centralized and clean configuration for Spotify integration
 */
object SpotifyConfig {
    // Hosts and Base URLs
    object Hosts {
        const val ACCOUNTS = "https://accounts.spotify.com/"
        const val API = "https://api.spotify.com/v1/"
    }

    // OAuth Configuration
    object OAuth {
        const val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
        const val CLIENT_SECRET = BuildConfig.SPOTIFY_CLIENT_SECRET

        // Redirect Configuration
        object Redirect {
            const val HOST = BuildConfig.SPOTIFY_REDIRECT_HOST
            const val PORT = BuildConfig.SPOTIFY_REDIRECT_PORT
            const val PATH = BuildConfig.SPOTIFY_REDIRECT_PATH
            const val URI = "$HOST:$PORT$PATH"
        }

        // Spotify API Scopes
        object Scopes {
            const val READ_PLAYBACK_STATE = "user-read-playback-state"
            const val MODIFY_PLAYBACK_STATE = "user-modify-playback-state"
            const val READ_CURRENT_PLAYING = "user-read-currently-playing"

            // Generate space-separated scope string
            val all = listOf(
                READ_PLAYBACK_STATE,
                MODIFY_PLAYBACK_STATE,
                READ_CURRENT_PLAYING
            ).joinToString("%20")
        }
    }

    // Authentication Endpoints
    object AuthEndpoints {
        const val AUTHORIZE = "authorize"
        const val TOKEN = "api/token"
    }

    // Player Endpoints
    object PlayerEndpoints {
        const val PLAYER_STATE = "me/player"
        const val PLAY = "me/player/play"
        const val PAUSE = "me/player/pause"
        const val NEXT = "me/player/next"
        const val PREVIOUS = "me/player/previous"
    }

    // Token Storage Keys
    object TokenKeys {
        const val ACCESS = "spotify_access_token"
        const val REFRESH = "spotify_refresh_token"
        const val EXPIRATION = "spotify_token_expiration"
    }
}