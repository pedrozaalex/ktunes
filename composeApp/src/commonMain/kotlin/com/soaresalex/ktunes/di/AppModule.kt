package com.soaresalex.ktunes.di

import com.russhwolf.settings.Settings
import com.soaresalex.ktunes.data.AuthRedirectServer
import com.soaresalex.ktunes.data.OAuthStateService
import com.soaresalex.ktunes.data.SpotifyApiClient
import com.soaresalex.ktunes.data.SpotifyAuthClient
import com.soaresalex.ktunes.plugins.PluginManager
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    // Singleton Services
    singleOf(::Settings)
    singleOf(::OAuthStateService)
    singleOf(::SpotifyAuthClient)
    singleOf(::SpotifyApiClient)
    singleOf(::AuthRedirectServer)
    singleOf(::PluginManager)

    // ViewModels
    factoryOf(::PlaybackViewModel)
    factoryOf(::PluginViewModel)
}

fun initializeKoin() {
    startKoin {
        modules(appModule)
    }
}