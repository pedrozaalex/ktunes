package com.soaresalex.ktunes.data

import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import com.soaresalex.ktunes.app.config.SpotifyConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.koin.core.component.KoinComponent
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit

@Serializable
data class SpotifyDeviceModel(
    val id: String,
    val isActive: Boolean,
    val isPrivateSession: Boolean,
    val isRestricted: Boolean,
    val name: String,
    val type: String,
    val volumePercent: Int,
    val supportsVolume: Boolean
)

@Serializable
data class SpotifyContextModel(
    val type: String,
    val href: String,
    val externalUrls: Map<String, String>,
    val uri: String
)

@Serializable
data class SpotifyImageModel(
    val url: String,
    val height: Int? = null,
    val width: Int? = null
)

@Serializable
data class SpotifyArtistModel(
    val externalUrls: Map<String, String>,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

@Serializable
data class SpotifyRestrictionsModel(
    val reason: String
)

@Serializable
data class SpotifyAlbumModel(
    val albumType: String,
    val totalTracks: Int,
    val availableMarkets: List<String>,
    val externalUrls: Map<String, String>,
    val href: String,
    val id: String,
    val images: List<SpotifyImageModel>,
    val name: String,
    val releaseDate: String,
    val releaseDatePrecision: String,
    val restrictions: SpotifyRestrictionsModel? = null,
    val type: String,
    val uri: String,
    val artists: List<SpotifyArtistModel>
)

@Serializable
data class SpotifyTrackModel(
    val album: SpotifyAlbumModel,
    val artists: List<SpotifyArtistModel>,
    val availableMarkets: List<String>,
    val discNumber: Int,
    val durationMs: Int,
    val explicit: Boolean,
    val externalIds: Map<String, String>,
    val externalUrls: Map<String, String>,
    val href: String,
    val id: String,
    val restrictions: SpotifyRestrictionsModel? = null,
    val name: String,
    val popularity: Int,
    val previewUrl: String? = null,
    val trackNumber: Int,
    val type: String,
    val uri: String,
    val isLocal: Boolean
)

@Serializable
data class SpotifyPlayerState(
    val device: SpotifyDeviceModel,
    val repeatState: String,
    val shuffleState: Boolean,
    val context: SpotifyContextModel? = null,
    val timestamp: Long,
    val progressMs: Int,
    val isPlaying: Boolean,
    val item: SpotifyTrackModel? = null,
    val currentlyPlayingType: String,
)

class SpotifyAuthClient(
    private val settings: Settings = Settings(),
    private val stateService: OAuthStateService = OAuthStateService()
) {
    @OptIn(ExperimentalSerializationApi::class)
    private val authHttpClient = HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                namingStrategy = JsonNamingStrategy.SnakeCase
            })
        }
        defaultRequest {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            url(SpotifyConfig.Hosts.ACCOUNTS)
        }
        expectSuccess = true
    }

    suspend fun handleAuthorizationCallback(code: String, state: String?): AuthResult {
        val result = try {
            val tokenResponse = exchangeCodeForTokens(code, state)
            saveTokens(tokenResponse)
            AuthResult.Success
        } catch (e: Exception) {
            Logger.e("Authorization failed: ${e.message}")
            AuthResult.Failure(e.message ?: "Unknown error")
        }

        return result
    }

    private suspend fun exchangeCodeForTokens(code: String, state: String?): TokenResponse {
        if (state == null || !stateService.validateAndConsume(state)) {
            throw IllegalStateException("Invalid or missing state parameter")
        }

        val response = authHttpClient.submitForm(
            url = SpotifyConfig.AuthEndpoints.TOKEN,
            formParameters = parameters {
                append("grant_type", "authorization_code")
                append("code", code)
                append("redirect_uri", SpotifyConfig.OAuth.Redirect.URI)
                append("client_id", SpotifyConfig.OAuth.CLIENT_ID)
                append("client_secret", SpotifyConfig.OAuth.CLIENT_SECRET)
            }
        )

        val tokens = try {
            response.body<TokenResponse>()
        } catch (e: Exception) {
            Logger.e("Token exchange failed: ${e.message}")
            throw SpotifyApiException("Token exchange failed", e)
        }

        return tokens
    }

    suspend fun refreshAccessToken(): TokenResponse {
        return authHttpClient.submitForm(
            url = SpotifyConfig.AuthEndpoints.TOKEN,
            formParameters = parameters {
                append("grant_type", "refresh_token")
                append("refresh_token", settings.getString(SpotifyConfig.TokenKeys.REFRESH, ""))
                append("client_id", SpotifyConfig.OAuth.CLIENT_ID)
                append("client_secret", SpotifyConfig.OAuth.CLIENT_SECRET)
            }
        ).body()
    }

    private fun saveTokens(tokenResponse: TokenResponse) {
        settings.putString(SpotifyConfig.TokenKeys.ACCESS, tokenResponse.accessToken)
        settings.putString(SpotifyConfig.TokenKeys.REFRESH, tokenResponse.refreshToken)
        settings.putLong(
            SpotifyConfig.TokenKeys.EXPIRATION,
            Clock.System.now().toEpochMilliseconds() + (tokenResponse.expiresIn * 1000)
        )
    }

    fun isAuthenticated(): Boolean {
        val accessToken = settings.getString(SpotifyConfig.TokenKeys.ACCESS, "")
        val expiration = settings.getLong(SpotifyConfig.TokenKeys.EXPIRATION, 0)
        return accessToken.isNotEmpty() && Clock.System.now().toEpochMilliseconds() < expiration
    }

    suspend fun generateAuthorizationUrl(): String {
        val state = stateService.generate()
        stateService.store(state)

        return "${SpotifyConfig.Hosts.ACCOUNTS}${SpotifyConfig.AuthEndpoints.AUTHORIZE}?" +
                "client_id=${SpotifyConfig.OAuth.CLIENT_ID}" +
                "&response_type=code" +
                "&redirect_uri=${SpotifyConfig.OAuth.Redirect.URI}" +
                "&state=$state" +
                "&scope=${SpotifyConfig.OAuth.Scopes.all}"
    }

    sealed class AuthResult {
        object Success : AuthResult()
        data class Failure(val errorMessage: String) : AuthResult()
    }

    class SpotifyApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

    @Serializable
    data class TokenResponse(
        val accessToken: String,
        val tokenType: String,
        val expiresIn: Long,
        val refreshToken: String,
        val scope: String
    )
}

class SpotifyApiClient(
    private val authClient: SpotifyAuthClient,
    private val settings: Settings
) {
    @OptIn(ExperimentalSerializationApi::class)
    private val apiHttpClient = HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                namingStrategy = JsonNamingStrategy.SnakeCase
            })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val accessToken = settings.getString(SpotifyConfig.TokenKeys.ACCESS, "")
                    val refreshToken = settings.getString(SpotifyConfig.TokenKeys.REFRESH, "")

                    BearerTokens(
                        accessToken = accessToken,
                        refreshToken = refreshToken
                    )
                }
                refreshTokens {
                    try {
                        val tokenResponse = authClient.refreshAccessToken()
                        BearerTokens(
                            accessToken = tokenResponse.accessToken,
                            refreshToken = tokenResponse.refreshToken
                        )
                    } catch (e: Exception) {
                        Logger.e("Token refresh failed: ${e.message}")
                        null
                    }
                }
            }
        }
        defaultRequest {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            url(SpotifyConfig.Hosts.API)
        }
        expectSuccess = true
    }

    suspend fun pause() {
        apiHttpClient.put(SpotifyConfig.PlayerEndpoints.PAUSE)
    }

    suspend fun resume() {
        apiHttpClient.put(SpotifyConfig.PlayerEndpoints.PLAY)
    }

    suspend fun nextTrack() {
        apiHttpClient.post(SpotifyConfig.PlayerEndpoints.NEXT)
    }

    suspend fun previousTrack() {
        apiHttpClient.post(SpotifyConfig.PlayerEndpoints.PREVIOUS)
    }

    suspend fun getCurrentPlayerState(): SpotifyPlayerState? {
        val response = apiHttpClient.get(SpotifyConfig.PlayerEndpoints.PLAYER_STATE)

        return if (response.status.value == 204) {
            null // No device is currently playing
        } else {
            response.body()
        }
    }
}

class AuthRedirectServer(
    private val auth: SpotifyAuthClient
) : KoinComponent {
    var onAuthenticationResult: ((SpotifyAuthClient.AuthResult) -> Unit)? = null

    private var server = embeddedServer(Netty, SpotifyConfig.OAuth.Redirect.PORT.toInt()) {
        routing {
            get(SpotifyConfig.OAuth.Redirect.PATH) {
                val code = call.parameters["code"]
                val state = call.parameters["state"]

                val result = code?.let {
                    auth.handleAuthorizationCallback(it, state)
                }

                when (result) {
                    is SpotifyAuthClient.AuthResult.Success -> {
                        onAuthenticationResult?.invoke(result)

                        call.respondText("Authentication successful! You can close this window.")
                    }

                    is SpotifyAuthClient.AuthResult.Failure ->
                        call.respondText("Authentication failed: ${result.errorMessage}")

                    null ->
                        call.respondText("Invalid authentication attempt.")
                }
            }
        }
    }

    fun startListening() {
        server.start(wait = false)
    }

    fun stopListening() {
        server.stop(1000, 2000)
    }
}

class OAuthStateService {
    // Thread-safe storage of valid states
    private val validStates = mutableSetOf<String>()
    private val stateMutex = Mutex()

    /**
     * Generates a cryptographically secure state parameter
     */
    fun generate(): String {
        // Use SecureRandom to generate a cryptographically strong random state
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)

        // Convert to Base64 to ensure URL-safe characters
        val state = Base64.getUrlEncoder()
            .encodeToString(randomBytes)
            .replace("=", "") // Remove padding

        return state
    }

    /**
     * Registers a generated state as valid
     */
    suspend fun store(state: String) {
        stateMutex.withLock {
            // Limit the number of valid states to prevent memory issues
            if (validStates.size > 100) {
                validStates.clear()
            }
            validStates.add(state)
        }
    }

    /**
     * Validates and consumes the state
     */
    suspend fun validateAndConsume(state: String): Boolean {
        return stateMutex.withLock {
            val isValid = validStates.contains(state)
            if (isValid) {
                // Remove the state after successful validation to prevent reuse
                validStates.remove(state)
            }
            isValid
        }
    }
}
