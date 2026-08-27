package com.oasis.tracker.network

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveSearchResponse(val response: ArchiveSearchInnerResponse? = null)

@Serializable
data class ArchiveSearchInnerResponse(val docs: List<ArchiveDoc> = emptyList())

@Serializable
data class ArchiveDoc(
    val identifier: String,
    val title: String? = null,
    val mediatype: String? = null
)
