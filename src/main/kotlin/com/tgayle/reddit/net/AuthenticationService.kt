package com.tgayle.reddit.net

import com.tgayle.reddit.auth.AuthenticationState
import com.tgayle.reddit.models.Account
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.*

interface AuthenticationService {
    companion object {
        fun defaultClient(): AuthenticationService {
            return Retrofit.Builder()
                    .addConverterFactory(defaultJsonConverter())
                    .client(OkHttpClient.Builder()
                        .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .build())
                    .baseUrl("https://www.reddit.com/api/")
                    .build()
                    .create(AuthenticationService::class.java)
        }
    }

    @POST("v1/access_token")
    @FormUrlEncoded
    suspend fun getAccessToken(@FieldMap body: Map<String, String>, @HeaderMap headers: Map<String, String> = emptyMap()): AuthenticationState

    @GET("https://oauth.reddit.com/api/v1/me")
    suspend fun getCurrentUser(@HeaderMap headers: Map<String, String>): Account
}
