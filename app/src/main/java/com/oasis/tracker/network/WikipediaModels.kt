package com.oasis.tracker.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

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

/**
 * Wikipedia's two REST APIs don't agree on what this field is called: the
 * page-summary endpoint (RESTBase) names it "source", while the newer core
 * search endpoint uses "url". Accepting both means we don't have to be sure
 * which one a given response actually used — with ignoreUnknownKeys on, a
 * wrong guess here doesn't error, it just silently deserializes to null.
 */
@Serializable
data class WikiThumbnail(
    @JsonNames("source") val url: String? = null,
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
