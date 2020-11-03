package com.tgayle.reddit.net

import com.tgayle.reddit.auth.AuthenticationParams
import com.tgayle.reddit.auth.AuthenticationState
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface AuthenticationService {
    companion object {
        fun defaultClient(): AuthenticationService {
            return Retrofit.Builder()
                    .addConverterFactory(defaultJsonConverter {
                        serializersModule = AuthenticationParams.jsonModule
                    })
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
}
