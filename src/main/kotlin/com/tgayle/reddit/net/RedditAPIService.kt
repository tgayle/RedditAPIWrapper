package com.tgayle.reddit.net

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.tgayle.reddit.models.Comment
import com.tgayle.reddit.models.Listing
import com.tgayle.reddit.models.Link
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface RedditAPIService {
    companion object {
        const val BASE_REDDIT_URL = "https://reddit.com"

        fun defaultClient(): RedditAPIService {
            return Retrofit.Builder()
                .baseUrl(BASE_REDDIT_URL)
                .addConverterFactory(defaultJsonConverter())
                .build()
                .create(RedditAPIService::class.java)
        }
    }

    @GET("/.json")
    suspend fun getFrontPage(): Listing<Link>

    @GET("/r/{subreddit}/{link_id}/.json")
    suspend fun getComments(@Path("subreddit") subreddit: String, @Path("link_id") postId: String): List<Listing<Comment>>


}