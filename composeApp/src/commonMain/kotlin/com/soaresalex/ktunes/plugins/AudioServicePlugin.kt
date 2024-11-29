package com.soaresalex.ktunes.plugins

import com.soaresalex.ktunes.viewmodels.AudioPlaybackService

interface AudioServicePlugin {
    val id: String
    val name: String
    fun createPlaybackService(): AudioPlaybackService
}