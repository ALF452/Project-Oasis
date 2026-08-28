package com.oasis.tracker.network

enum class SearchSource { WIKIPEDIA, ARCHIVE_ORG }

data class GameSearchResult(
    val title: String,
    val subtitle: String?,
    val coverUrl: String?,
    val sourceUrl: String,
    val source: SearchSource
)
