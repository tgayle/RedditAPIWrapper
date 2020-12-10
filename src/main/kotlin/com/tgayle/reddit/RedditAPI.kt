package com.tgayle.reddit

import com.tgayle.RedditClient
import com.tgayle.reddit.net.RedditAPIService
import com.tgayle.reddit.posts.PostManager

class RedditAPI(
    private val client: RedditClient,
    internal val service: RedditAPIService = RedditAPIService.defaultClient { client.getToken().accessToken },
) {
    val posts: PostManager = PostManager(client, service)

    suspend fun authenticate() = client.authenticate()
    suspend fun getCurrentUser() = client.ensureAuth {
        service.getCurrentUser()
    }
}