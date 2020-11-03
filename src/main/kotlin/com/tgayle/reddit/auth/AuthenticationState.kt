package com.tgayle.reddit.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val expired get() = expirationTime < System.currentTimeMillis()
}