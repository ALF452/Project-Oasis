package com.oasis.tracker.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val userAgentInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", "OasisGameTracker/1.0 (https://github.com/ALF452/Project-Oasis)")
            .build()
        chain.proceed(request)
    }

    // Exposed so other API clients (e.g. GitHubApi) can derive from the same
    // connection pool/dispatcher via newBuilder() instead of spinning up their own.
    val sharedOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .build()
    }

    val jsonConverter = json.asConverterFactory("application/json".toMediaType())

    val wikipediaApi: WikipediaApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://en.wikipedia.org/")
            .client(sharedOkHttpClient)
            .addConverterFactory(jsonConverter)
            .build()
            .create(WikipediaApi::class.java)
    }

    val archiveOrgApi: ArchiveOrgApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://archive.org/")
            .client(sharedOkHttpClient)
            .addConverterFactory(jsonConverter)
            .build()
            .create(ArchiveOrgApi::class.java)
    }
}
