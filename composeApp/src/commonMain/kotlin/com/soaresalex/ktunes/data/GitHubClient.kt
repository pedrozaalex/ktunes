package com.soaresalex.ktunes.data

import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

object GitHubClient {
    private val github: GitHub = GitHub.connectAnonymously()

    fun searchRepositoriesByTopic(topic: String): List<GHRepository> {
        return github.searchRepositories()
            .q("topic:$topic")
            .list()
            .toList()
    }
}
