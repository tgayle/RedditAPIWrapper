package com.tgayle.reddit.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
abstract class AuthenticationParams {
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