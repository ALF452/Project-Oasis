package com.oasis.tracker.network

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GameSearchRepository(
    private val wikipediaApi: WikipediaApi = NetworkModule.wikipediaApi,
    private val archiveOrgApi: ArchiveOrgApi = NetworkModule.archiveOrgApi
) {

    /** Searches Wikipedia and archive.org concurrently, keeping either source's failure from sinking the other. */
    suspend fun search(query: String): List<GameSearchResult> {
        if (query.isBlank()) return emptyList()
        return coroutineScope {
            val wiki = async { runCatching { searchWikipedia(query) }.getOrDefault(emptyList()) }
            val archive = async { runCatching { searchArchiveOrg(query) }.getOrDefault(emptyList()) }
            wiki.await() + archive.await()
        }
    }

    private suspend fun searchWikipedia(query: String): List<GameSearchResult> {
        val response = wikipediaApi.search(query)
        return response.pages.map { page ->
            GameSearchResult(
                title = page.title,
                subtitle = page.description ?: stripHtml(page.excerpt),
                coverUrl = normalizeProtocolRelativeUrl(page.thumbnail?.url),
                sourceUrl = "https://en.wikipedia.org/wiki/${page.key}",
                source = SearchSource.WIKIPEDIA
            )
        }
    }

    private suspend fun searchArchiveOrg(query: String): List<GameSearchResult> {
        val response = archiveOrgApi.search("$query AND mediatype:(software OR image)")
        return response.response?.docs.orEmpty().map { doc ->
            GameSearchResult(
                title = doc.title ?: doc.identifier,
                subtitle = doc.mediatype,
                coverUrl = "https://archive.org/services/img/${doc.identifier}",
                sourceUrl = "https://archive.org/details/${doc.identifier}",
                source = SearchSource.ARCHIVE_ORG
            )
        }
    }

    private fun normalizeProtocolRelativeUrl(url: String?): String? = when {
        url == null -> null
        url.startsWith("//") -> "https:$url"
        else -> url
    }

    private fun stripHtml(text: String?): String? = text?.replace(Regex("<[^>]*>"), "")
}
