package com.oasis.tracker.network

import kotlinx.serialization.Serializable

@Serializable
data class WikiSearchResponse(val pages: List<WikiSearchPage> = emptyList())

@Serializable
data class WikiSearchPage(
    val id: Long = 0,
    val key: String = "",
    val title: String = "",
    val excerpt: String? = null,
    val description: String? = null,
    val thumbnail: WikiThumbnail? = null
)

@Serializable
data class WikiThumbnail(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class WikiSummary(
    val title: String = "",
    val extract: String? = null,
    val description: String? = null,
    val thumbnail: WikiThumbnail? = null,
    val originalimage: WikiThumbnail? = null,
    val content_urls: WikiContentUrls? = null
)

@Serializable
data class WikiContentUrls(val desktop: WikiPageUrl? = null)

@Serializable
data class WikiPageUrl(val page: String? = null)
