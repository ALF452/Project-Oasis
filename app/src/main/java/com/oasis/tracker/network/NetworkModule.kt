package com.oasis.tracker.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Response
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

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .build()
    }

    private val jsonConverter = json.asConverterFactory("application/json".toMediaType())

    val wikipediaApi: WikipediaApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://en.wikipedia.org/")
            .client(okHttpClient)
            .addConverterFactory(jsonConverter)
            .build()
            .create(WikipediaApi::class.java)
    }

    val archiveOrgApi: ArchiveOrgApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://archive.org/")
            .client(okHttpClient)
            .addConverterFactory(jsonConverter)
            .build()
            .create(ArchiveOrgApi::class.java)
    }
}
