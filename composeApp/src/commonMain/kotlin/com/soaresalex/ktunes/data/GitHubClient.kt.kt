import org.kohsuke.github.GitHub

object GitHubClient {
    private val github: GitHub = GitHub.connectAnonymously()

    fun searchRepositoriesByTopic(topic: String): List<String> {
        val repositories = github.searchRepositories()
            .q("topic:$topic")
            .list()

        return repositories.map { it.htmlUrl.toString() }
    }
}
