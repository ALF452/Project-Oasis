package com.oasis.tracker.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ArchiveOrgApi {
    @GET("advancedsearch.php")
    suspend fun search(
        @Query("q") query: String,
        @Query("fl[]") fields: List<String> = listOf("identifier", "title", "mediatype"),
        @Query("rows") rows: Int = 20,
        @Query("output") output: String = "json"
    ): ArchiveSearchResponse
}
