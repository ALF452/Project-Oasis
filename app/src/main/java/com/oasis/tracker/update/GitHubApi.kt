package com.oasis.tracker.update

import com.oasis.tracker.network.NetworkModule
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun latestRelease(@Path("owner") owner: String, @Path("repo") repo: String): GitHubRelease

    companion object {
        fun create(): GitHubApi {
            // Derived from the shared client via newBuilder() so this reuses its
            // connection pool/dispatcher instead of spinning up a second one.
            val client = NetworkModule.sharedOkHttpClient.newBuilder()
                .addInterceptor(Interceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Accept", "application/vnd.github+json")
                        .build()
                    chain.proceed(request)
                })
                .build()
            return Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(client)
                .addConverterFactory(NetworkModule.jsonConverter)
                .build()
                .create(GitHubApi::class.java)
        }
    }
}
