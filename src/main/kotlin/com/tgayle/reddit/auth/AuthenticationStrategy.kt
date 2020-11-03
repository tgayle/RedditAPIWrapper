package com.tgayle.reddit.auth

import com.tgayle.reddit.models.ClientId
import com.tgayle.reddit.net.AuthenticationService
import com.tgayle.reddit.net.encodeToFieldMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.*

sealed class AuthenticationStrategy(val secret: String) {
    internal val authenticationService = AuthenticationService.defaultClient()

    abstract suspend fun authenticate(clientId: ClientId): AuthenticationState
}

sealed class GrantType(val type: String) {
    object Password: GrantType("password")
    object RefreshToken: GrantType("refresh_token")
    object Standard: GrantType("authorization_code")
    object Anonymous: GrantType("client_credentials")
    data class AnonymousApplication(val deviceId: String): GrantType("https://oauth.reddit.com/grants/installed_client")
}

@Serializable
data class AuthenticationState(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("token_type")
        val tokenType: String,
        val scope: String,
        @SerialName("expires_in")
        val expiresIn: Long,
) {

    val expirationTime = System.currentTimeMillis() + (expiresIn * 1000)
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

@Serializable
abstract class AuthenticationParams() {
    @SerialName("grant_type")
    abstract val grantType: String

    companion object {
        val jsonModule: SerializersModule = SerializersModule {
            polymorphic(AuthenticationParams::class) {
                subclass(Script.ScriptAuthenticationParams::class)
            }
        }
    }
}

class Script(secret: String, val username: String, val password: String): AuthenticationStrategy(secret) {
    @Serializable
    data class ScriptAuthenticationParams internal constructor(
            val username: String,
            val password: String,
    ): AuthenticationParams() {
        @SerialName("grant_type")
        override val grantType = "password"
    }

    override suspend fun authenticate(clientId: ClientId): AuthenticationState {
        val headers = mapOf(
                "Authorization" to "Basic " + Base64.getEncoder().encodeToString("${clientId.id}:$secret".encodeToByteArray()).also(::println)
        )
        return authenticationService.getAccessToken(encodeToFieldMap(ScriptAuthenticationParams(username, password)).also(::println), headers)
    }
}