package com.qytech.tidal

import android.app.Application
import android.content.Context
import com.tidal.sdk.auth.CredentialsProvider
import com.tidal.sdk.auth.TidalAuth
import com.tidal.sdk.auth.model.AuthConfig
import com.tidal.sdk.auth.network.NetworkLogLevel
import com.tidal.sdk.eventproducer.EventProducer
import com.tidal.sdk.eventproducer.model.EventsConfig
import com.tidal.sdk.player.Player
import com.tidal.sdk.player.playbackengine.player.CacheProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * 存储公共变量的
 */
object TidalService {
    internal var authConfig: AuthConfig? = null

    internal var tidalAuth: TidalAuth? = null
        private set

    internal var credentialsProvider: CredentialsProvider? = null
        private set

    internal var player: Player? = null

    fun init(context: Context, config: AuthConfig) {
        authConfig = config
        tidalAuth = initTidalAuth(context)
        credentialsProvider = tidalAuth?.credentialsProvider
    }

    private fun initTidalAuth(context: Context): TidalAuth {
        val tidalAuthConfig = AuthConfig(
            clientId = authConfig?.clientId ?: "",
            clientSecret = authConfig?.clientSecret ?: "",
            scopes = setOf(
                "user.read",
                "collection.read",
                "search.read",
                "playlists.write",
                "collection.write",
                "playlists.read",
                "playback",
                "recommendations.read",
                "entitlements.read",
                "search.write"
            ),
            credentialsKey = "storage",
            enableCertificatePinning = true,
            logLevel = NetworkLogLevel.BODY
        )
        return TidalAuth.getInstance(tidalAuthConfig, context)
    }

    fun getPlayerInstance(context: Context): Player? {
        if (credentialsProvider != null) {
            player = Player(
                context.applicationContext as Application,
                credentialsProvider!!,
                EventProducer.getInstance(
                    credentialsProvider!!,
                    EventsConfig(
                        Int.MAX_VALUE,
                        emptySet(),
                        "player-sample-1.0.0"
                    ),
                    context,
                    CoroutineScope(Dispatchers.IO)
                )
                    .eventSender,
                userClientIdSupplier = null,
                isOfflineMode = false,
                isDebuggable = BuildConfig.DEBUG,
                cacheProvider = CacheProvider.Internal(),
                version = "player-sample-1.0.0"
            )
        }
        return player
    }
}