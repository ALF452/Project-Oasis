package com.oasis.tracker.network

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Outcome of a [GameSearchRepository.search] call, distinguishing a genuine
 *  zero-result search from both sources being unreachable (no connectivity,
 *  Wikipedia/archive.org down) — the two look identical as a plain empty list. */
sealed interface SearchOutcome {
    data class Success(val results: List<GameSearchResult>) : SearchOutcome
    data object BothSourcesFailed : SearchOutcome
}

class GameSearchRepository(
    private val wikipediaApi: WikipediaApi = NetworkModule.wikipediaApi,
    private val archiveOrgApi: ArchiveOrgApi = NetworkModule.archiveOrgApi
) {

    /** Searches Wikipedia and archive.org concurrently, keeping either source's failure from sinking the other. */
    suspend fun search(query: String): SearchOutcome {
        if (query.isBlank()) return SearchOutcome.Success(emptyList())
        return coroutineScope {
            val wiki = async { runCatching { searchWikipedia(query) } }
            val archive = async { runCatching { searchArchiveOrg(query) } }
            val wikiResult = wiki.await()
            val archiveResult = archive.await()
            if (wikiResult.isFailure && archiveResult.isFailure) {
                SearchOutcome.BothSourcesFailed
            } else {
                SearchOutcome.Success(wikiResult.getOrDefault(emptyList()) + archiveResult.getOrDefault(emptyList()))
            }
        }
    }

    /**
     * The lightweight search endpoint sometimes has no thumbnail for a page even
     * when one exists — the fuller page-summary endpoint (the article's actual
     * lead image) usually does. Resolved concurrently per result here, at search
     * time, so both the results list itself and whatever gets added from it show
     * real cover art rather than only fixing it up after the fact on add.
     */
    private suspend fun searchWikipedia(query: String): List<GameSearchResult> = coroutineScope {
        val response = wikipediaApi.search(query)
        response.pages.map { page ->
            async {
                val directCover = normalizeProtocolRelativeUrl(page.thumbnail?.url)
                // TEMPORARY: always fetch the summary (even when the direct cover already
                // resolved) and report everything raw, so a device screenshot shows exactly
                // what each endpoint actually returned instead of guessing blind again.
                val summaryResult = runCatching { wikipediaApi.summary(page.key) }
                val summary = summaryResult.getOrNull()
                val summaryCover = summary?.let { normalizeProtocolRelativeUrl(it.thumbnail?.url ?: it.originalimage?.url) }
                val debug = "search.thumb=${page.thumbnail} | summary.err=${summaryResult.exceptionOrNull()?.message} | " +
                    "summary.thumb=${summary?.thumbnail} | summary.orig=${summary?.originalimage}"
                GameSearchResult(
                    title = page.title,
                    subtitle = page.description ?: stripHtml(page.excerpt),
                    coverUrl = directCover ?: summaryCover,
                    sourceUrl = "https://en.wikipedia.org/wiki/${page.key}",
                    source = SearchSource.WIKIPEDIA,
                    debugInfo = debug
                )
            }
        }.awaitAll()
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
