package com.tgayle.reddit.auth

internal sealed class GrantType(val type: String) {
    object Password: GrantType("password")
    object RefreshToken: GrantType("refresh_token")
    object Standard: GrantType("authorization_code")
    object Anonymous: GrantType("client_credentials")
    data class AnonymousInstalled(val deviceId: String): GrantType("https://oauth.reddit.com/grants/installed_client")
}