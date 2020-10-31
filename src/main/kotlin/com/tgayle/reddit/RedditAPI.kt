package com.tgayle.reddit

import com.tgayle.reddit.net.RedditAPIService
import com.tgayle.reddit.posts.PostManager

class RedditAPI(private val client: RedditAPIService = RedditAPIService.defaultClient()) {
    val posts: PostManager = PostManager(client)
}