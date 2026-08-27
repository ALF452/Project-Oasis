package com.oasis.tracker.update

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun latestRelease(@Path("owner") owner: String, @Path("repo") repo: String): GitHubRelease

    companion object {
        fun create(): GitHubApi {
            val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
            val client = OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Accept", "application/vnd.github+json")
                        .header("User-Agent", "OasisGameTracker-UpdateChecker")
                        .build()
                    chain.proceed(request)
                })
                .build()
            return Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(GitHubApi::class.java)
        }
    }
}
