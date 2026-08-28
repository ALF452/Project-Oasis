package com.oasis.tracker.network

enum class SearchSource { WIKIPEDIA, ARCHIVE_ORG }

data class GameSearchResult(
    val title: String,
    val subtitle: String?,
    val coverUrl: String?,
    val sourceUrl: String,
    val source: SearchSource,
    // TEMPORARY: raw cover-resolution diagnostics shown in the UI, since prior
    // guesses at Wikipedia's actual JSON field names didn't fix missing cover
    // art and this sandbox can't reach the real API to check directly. Remove
    // once the real shape is confirmed from a device screenshot.
    val debugInfo: String? = null
)
