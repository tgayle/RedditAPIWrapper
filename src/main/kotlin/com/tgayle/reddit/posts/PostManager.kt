package com.tgayle.reddit.posts

import com.tgayle.reddit.net.RedditAPIService

class PostManager(private val client: RedditAPIService) {

    suspend fun getFrontPage() = client.getFrontPage()
}