package com.tgayle.reddit

import com.tgayle.reddit.auth.AuthenticationState
import com.tgayle.reddit.auth.AuthenticationStrategy
import com.tgayle.reddit.models.ClientId
import com.tgayle.reddit.net.RedditAPIService
import com.tgayle.reddit.posts.PostManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RedditClient(
    val clientId: ClientId,
    val strategy: AuthenticationStrategy
) {
    private val _token = MutableStateFlow<AuthenticationState?>(null)
    val token: StateFlow<AuthenticationState?> get() = _token

    private val refreshingToken = Mutex(false)
    private var firstAuthenticated: Boolean = false

    suspend fun <T> ensureAuth(block: suspend () -> T): T {
        getToken()
        return block()
    }

    suspend fun getToken() {
        refreshingToken.withLock(_token) {
            val tokenState = _token.value
            if (tokenState == null || tokenState.expired) {
                println("We had to refresh the token!")
                _token.value = if (firstAuthenticated) strategy.authenticate(clientId) else strategy.refresh(clientId)
                println(_token.value)
            } else {
                println("No need to refresh the token yet.")
            }
        }
    }

    suspend fun authenticate() {
        getToken()
        firstAuthenticated = false
    }
}

class RedditAPI(
        val client: RedditClient,
        private val service: RedditAPIService = RedditAPIService.defaultClient(),
) {
    val posts: PostManager = PostManager(client, service)

    suspend fun authenticate() = client.authenticate()
}