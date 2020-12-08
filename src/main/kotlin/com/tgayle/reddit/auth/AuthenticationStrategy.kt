package com.tgayle.reddit.auth

import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.models.ClientId
import com.tgayle.reddit.models.RedditScope
import com.tgayle.reddit.net.AuthenticationService
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

private val String.base64 get() = Base64.getEncoder().encodeToString(this.encodeToByteArray()).toString()
private val String.uriSafe get() = URLEncoder.encode(this, Charsets.UTF_8)

sealed class AuthenticationResult {
    sealed class OAuthFailure: AuthenticationResult() {
        data class BadState(val errors: List<String>): OAuthFailure()
        data class BadCode(val errors: List<String>): OAuthFailure()
        object MissingRefreshToken: OAuthFailure()
    }

    data class UnknownFailure(val exception: Exception? = null): AuthenticationResult()
    data class Success(val result: AuthenticationState, val client: RedditClient): AuthenticationResult()
}


sealed class AuthenticationStrategy(internal val clientId: ClientId, private val secret: String) {
    internal val authenticationService = AuthenticationService.defaultClient()

    abstract suspend fun authenticate(): AuthenticationResult

    internal open suspend fun refresh(): AuthenticationResult = authenticate()

    open fun basicAuthHeaders() = mutableMapOf(
            "Authorization" to "Basic " + "${clientId.id}:$secret".base64
    )

    abstract fun getClient(): RedditClient
}


typealias StateValidator = suspend (state: String) -> Boolean
sealed class OAuthStrategy(clientId: ClientId, secret: String): AuthenticationStrategy(clientId, secret) {
    abstract fun getClient(username: String): RedditClient
    abstract suspend fun refresh(username: String): AuthenticationResult
}


class WebApp(clientId: ClientId, secret: String): OAuthStrategy(clientId, secret) {
    override suspend fun authenticate(): AuthenticationResult {
        TODO()
    }

    override fun getClient(): RedditClient {
        TODO("Not yet implemented")
    }

    override fun getClient(username: String): RedditClient {
        TODO("Not yet implemented")
    }

    override suspend fun refresh(username: String): AuthenticationResult {
        TODO("Not yet implemented")
    }

}

class InstalledApp(
    clientId: ClientId,
    private val redirectUri: String,
    private val tokenStore: TokenStore = InMemoryTokenStore(),
    private val validateAuthorization: StateValidator = { true }
): OAuthStrategy(clientId, "") {
    enum class TokenDuration(val length: String) {
        Temporary("temporary"),
        Permanent("permanent")
    }

    override suspend fun authenticate(): AuthenticationResult {
        error("Cannot authentication an Installed App without a code or redirect uri. See InstalledApp.authenticate(String) and InstalledApp.refresh(String)")
    }

    override fun getClient(): RedditClient {
        error("Cannot get client without target user.")
    }

    override fun getClient(username: String): RedditClient = getClient(username, null)

    private fun getClient(username: String, authState: AuthenticationState? = null): RedditClient {
        return object: RedditClient(this, authState) {
            override suspend fun authenticateForToken(): AuthenticationResult {
                return refreshForToken()
            }

            override suspend fun refreshForToken(): AuthenticationResult {
                return refresh(username)
            }

        }
    }

    suspend fun authenticate(code: String): AuthenticationResult {
        val headers = basicAuthHeaders()
        val body = mapOf(
            "grant_type" to "authorization_code",
            "code" to code,
            "redirect_uri" to redirectUri
        )

        val response = authenticationService.getAccessToken(body, headers)
        val currentUser = authenticationService.getCurrentUser(mapOf(
            "Authorization" to "Bearer ${response.accessToken}"
        ))

        if (response.refreshToken != null) {
            val tokenInfo = UserTokenInfo(
                currentUser.username,
                response.refreshToken,
                response.accessToken,
                response.scope.split(",", " ")
            )
            tokenStore.setUserTokenInfo(tokenInfo)
        }

        return AuthenticationResult.Success(response, getClient(currentUser.username, response))
    }

    override suspend fun refresh(username: String): AuthenticationResult {
        val tokenInfo = tokenStore.getUserTokenInfo(username)

        if (tokenInfo == null) {
            println("User $username doesn't have refresh token information!")
            return AuthenticationResult.OAuthFailure.MissingRefreshToken
        }

        val headers = basicAuthHeaders()
        val body = mapOf(
            "grant_type" to "refresh_token",
            "refresh_token" to tokenInfo.refreshToken,
        )

        val response = authenticationService.getAccessToken(body, headers)

        return AuthenticationResult.Success(response, getClient(username))
    }


    suspend fun onAuthorizationComplete(redirectUri: String): AuthenticationResult {
        val divider = redirectUri.indexOf('?')
        val baseUrl = redirectUri.substring(0, divider)
        val query = redirectUri.substring(divider + 1)
        val params = query.split("&").map {
            val divider = it.indexOf('=')
            val key = it.substring(0, divider)
            val value = it.substring(divider + 1)

            key to URLDecoder.decode(value, Charsets.UTF_8)
        }.toMap()

        val code = params["code"]
        val state = params["state"]
        val errors = params["error"]?.split(",").orEmpty()
        println("Code is $code")

        if (code == null) {
            return AuthenticationResult.OAuthFailure.BadCode(errors)
        }

        if (state == null || !validateAuthorization(state)) {
            return AuthenticationResult.OAuthFailure.BadState(errors)
        }

        return authenticate(code)
    }

    fun getAuthorizationUrl(
        redirectUri: String,
        state: String = UUID.randomUUID().toString(),
        scopes: List<String> = RedditScope.allScopes(),
        compact: Boolean = false,
        duration: TokenDuration = TokenDuration.Temporary
    ): String {
        val baseUrl = "https://www.reddit.com/api/v1/authorize${ if (compact) ".compact" else "" }?"

        val params = mapOf(
            "client_id" to clientId.id,
            "response_type" to "code",
            "state" to state,
            "redirect_uri" to redirectUri,
            "duration" to duration.length,
            "scope" to scopes.joinToString(" ")
        )


        return baseUrl + params.entries.joinToString("&") { (key, value) ->
            "$key=${value.uriSafe}"
        }

    }
}

class Script(clientId: ClientId, secret: String, private val username: String, private val password: String): AuthenticationStrategy(clientId, secret) {
    override suspend fun authenticate(): AuthenticationResult {
        val headers = basicAuthHeaders()
        val body = mapOf(
            "username" to username,
            "password" to password,
            "grant_type" to "password"
        )
        val state = authenticationService.getAccessToken(body, headers)

        return AuthenticationResult.Success(state, getClient())
    }

    override fun getClient(): RedditClient {
        return object: RedditClient(this) {
            override suspend fun authenticateForToken(): AuthenticationResult {
                return this@Script.authenticate()
            }

            override suspend fun refreshForToken(): AuthenticationResult {
                return this@Script.refresh()
            }

        }
    }
}

class Anonymous(clientId: ClientId, secret: String): AuthenticationStrategy(clientId, secret) {

    override suspend fun authenticate(): AuthenticationResult {
        val state = authenticationService.getAccessToken(
            body = mapOf(
                "grant_type" to "client_credentials",
            ),
            headers = basicAuthHeaders()
        )

        return AuthenticationResult.Success(state, getClient())
    }

    override fun getClient(): RedditClient {
        return object: RedditClient(this) {
            override suspend fun authenticateForToken(): AuthenticationResult {
                return this@Anonymous.authenticate()
            }

            override suspend fun refreshForToken(): AuthenticationResult {
                return this@Anonymous.refresh()
            }

        }
    }
}

