package com.tgayle.reddit

import com.tgayle.reddit.auth.*
import com.tgayle.reddit.net.RedditAPIService
import com.tgayle.reddit.posts.PostManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class RedditClient(
    val strategy: AuthenticationStrategy,
    initialAuthenticationState: AuthenticationState? = null
) {
    private val _token = MutableStateFlow<AuthenticationState?>(initialAuthenticationState)
    val token: StateFlow<AuthenticationState?> get() = _token

    private val refreshingToken = Mutex(false)
    private var firstAuthenticated: Boolean = initialAuthenticationState != null

    suspend fun <T> ensureAuth(block: suspend () -> T): T {
        getToken()
        return block()
    }

    suspend fun getToken(): AuthenticationState {
        refreshingToken.withLock(_token) {
            val tokenState = _token.value
            if (tokenState == null || tokenState.expired) {
                println("We had to refresh the token! firstAuthenticated=$firstAuthenticated")
                // TODO: Return error cases.
                _token.value = if (firstAuthenticated) {
                    (authenticateForToken() as? AuthenticationResult.Success)?.result
                } else {
                    (refreshForToken() as? AuthenticationResult.Success)?.result
                }
                println(_token.value)
            } else {
                println("No need to refresh the token yet.")
            }
        }

        return token.value!!
    }

    suspend fun authenticate() {
        getToken()
        firstAuthenticated = false
    }

    internal abstract suspend fun authenticateForToken(): AuthenticationResult
    internal abstract suspend fun refreshForToken(): AuthenticationResult

}

class RedditAPI(
        private val client: RedditClient,
        internal val service: RedditAPIService = RedditAPIService.defaultClient { client.getToken().accessToken },
) {
    val posts: PostManager = PostManager(client, service)

    suspend fun authenticate() = client.authenticate()
}