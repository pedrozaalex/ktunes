package com.soaresalex.ktunes.di

import com.russhwolf.settings.Settings
import com.soaresalex.ktunes.data.repository.LibraryRepository
import com.soaresalex.ktunes.data.service.*
import com.soaresalex.ktunes.screenmodels.LibraryScreenModel
import com.soaresalex.ktunes.ui.navigation.History
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
	singleOf(::Settings)
	singleOf(::History)
	singleOf(::NullMetadataService) bind MetadataService::class
	singleOf(::LocalMediaService) bind MediaService::class
	singleOf(::LibraryRepository)
	singleOf(::GstreamerPlaybackService) bind PlaybackService::class
	singleOf(::PlayQueueServiceImpl) bind PlayQueueService::class
	single { ListeningHistoryServiceImpl(capacity = 50) } bind ListeningHistoryService::class

	factoryOf(::LibraryScreenModel)
}

fun initializeKoin() {
	startKoin {
		modules(appModule)
	}
}