package com.tgayle.reddit.net

import com.tgayle.reddit.models.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface RedditAPIService {
    companion object {
        const val BASE_REDDIT_URL = "https://oauth.reddit.com/"

        fun defaultClient(getAuthToken: (suspend () -> String?)?): RedditAPIService {
            return Retrofit.Builder()
                .baseUrl(BASE_REDDIT_URL)
                .addConverterFactory(defaultJsonConverter {
                    classDiscriminator = "kind"
                })
                .client(OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()

                        if (getAuthToken != null) {
                            request.addHeader("Authorization", "Bearer ${ runBlocking { getAuthToken() } }")
                        }

                        chain.proceed(request.build())
                    }
                    .build())
                .build()
                .create(RedditAPIService::class.java)
        }
    }

    @GET("/.json")
    suspend fun getFrontPage(@QueryMap query: Map<String, String>): Listing<Link>

    @GET("/r/{subreddit}/comments/{link_id}")
    suspend fun getLink(@Path("subreddit") subreddit: String, @Path("link_id") postId: String): List<Listing<Thing>>


    @GET("/r/{subreddit}")
    suspend fun getSubreddit(@Path("subreddit") subreddit: String, @QueryMap query: Map<String, String>): Listing<Link>


}