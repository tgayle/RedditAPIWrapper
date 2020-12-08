package com.tgayle.reddit.auth

data class UserTokenInfo(val username: String, val refreshToken: String, val accessToken: String?, val scopes: List<String>)

interface TokenStore {
    suspend fun getUserTokenInfo(username: String): UserTokenInfo?
    suspend fun setUserAccessToken(username: String, accessToken: String?)
    suspend fun setUserRefreshToken(username: String, refreshToken: String)
    suspend fun setUserTokenInfo(tokenInfo: UserTokenInfo)
}

class InMemoryTokenStore: TokenStore {
    private val tokens = mutableMapOf<String, UserTokenInfo?>()

    override suspend fun getUserTokenInfo(username: String): UserTokenInfo? {
        return tokens[username]
    }

    override suspend fun setUserAccessToken(username: String, accessToken: String?) {
        val token = tokens[username] ?: throw IllegalStateException("Tried to update token information that doesn't exist.")
        tokens[username] = token.copy(accessToken = accessToken)

    }

    override suspend fun setUserRefreshToken(username: String, refreshToken: String) {
        val token = tokens[username] ?: throw IllegalStateException("Tried to update token information that doesn't exist.")
        tokens[username] = token.copy(refreshToken = refreshToken)
    }

    override suspend fun setUserTokenInfo(tokenInfo: UserTokenInfo) {
        tokens[tokenInfo.username] = tokenInfo
    }

}