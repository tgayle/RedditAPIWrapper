package com.tgayle.reddit.posts

import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.models.Link
import com.tgayle.reddit.net.RedditAPIService

class PostManager(val client: RedditClient, private val service: RedditAPIService) {

    suspend fun getFrontPage() = client.ensureAuth {
        service.getFrontPage()
    }

    suspend fun getComments(subreddit: String, linkId: String) = client.ensureAuth {
        service.getComments(subreddit, linkId)
    }
    suspend fun Link.comments() = client.ensureAuth {
        service.getComments(subreddit, id)
    }
}