package com.tgayle.reddit

import com.tgayle.reddit.auth.AuthenticationState
import com.tgayle.reddit.auth.AuthenticationStrategy
import com.tgayle.reddit.models.ClientId
import com.tgayle.reddit.net.RedditAPIService
import com.tgayle.reddit.posts.PostManager
import kotlinx.coroutines.flow.MutableStateFlow

class RedditAPI(
        private val clientId: ClientId,
        private val client: RedditAPIService = RedditAPIService.defaultClient()) {
    val posts: PostManager = PostManager(client)

    private val _token = MutableStateFlow<AuthenticationState?>(null)

    suspend fun getToken() {
        _token.value
    }

    suspend fun authenticate(strategy: AuthenticationStrategy) {
        strategy.authenticate(clientId)
    }
}