package com.tgayle.reddit.auth

import com.tgayle.reddit.models.ClientId
import com.tgayle.reddit.net.AuthenticationService
import com.tgayle.reddit.net.serialization.encodeToMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

sealed class AuthenticationStrategy(private val secret: String) {
    internal val authenticationService = AuthenticationService.defaultClient()

    abstract suspend fun authenticate(clientId: ClientId): AuthenticationState

    open suspend fun refresh(clientId: ClientId): AuthenticationState = authenticate(clientId)

    open fun basicAuthHeaders(clientId: ClientId) = mutableMapOf(
            "Authorization" to "Basic " + "${clientId.id}:$secret".base64()
    )
}

class WebApp(secret: String): AuthenticationStrategy(secret) {
    override suspend fun authenticate(clientId: ClientId): AuthenticationState {
        return AuthenticationState("", "" ,"", 4)
    }
}

class InstalledApp: AuthenticationStrategy("") {

    override suspend fun authenticate(clientId: ClientId): AuthenticationState {
        TODO("Not yet implemented")
    }
}

class Script(secret: String, private val username: String, private val password: String): AuthenticationStrategy(secret) {
    @Serializable
    data class ScriptAuthenticationParams internal constructor(
            val username: String,
            val password: String,
    ): AuthenticationParams() {
        @SerialName("grant_type")
        override val grantType = "password"
    }

    override suspend fun authenticate(clientId: ClientId): AuthenticationState {
        val headers = basicAuthHeaders(clientId)
        return authenticationService.getAccessToken(encodeToMap(ScriptAuthenticationParams(username, password)), headers)
    }
}

class Anonymous(secret: String): AuthenticationStrategy(secret) {

    override suspend fun authenticate(clientId: ClientId): AuthenticationState {
        return authenticationService.getAccessToken(
            body = mapOf(
                "grant_type" to "client_credentials",
            ),
            headers = basicAuthHeaders(clientId)
        )
    }
}

private fun String.base64(): String {
    return Base64.getEncoder().encodeToString(this.encodeToByteArray()).toString()
}