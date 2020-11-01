package com.tgayle.reddit.posts

import com.tgayle.reddit.models.Link
import com.tgayle.reddit.net.RedditAPIService

class PostManager(private val client: RedditAPIService) {

    suspend fun getFrontPage() = client.getFrontPage()

    suspend fun Link.comments() {

    }
}