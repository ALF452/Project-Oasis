package com.oasis.tracker.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WikipediaApi {
    @GET("w/rest.php/v1/search/page")
    suspend fun search(@Query("q") query: String, @Query("limit") limit: Int = 15): WikiSearchResponse

    @GET("api/rest_v1/page/summary/{title}")
    suspend fun summary(@Path("title") title: String): WikiSummary
}
