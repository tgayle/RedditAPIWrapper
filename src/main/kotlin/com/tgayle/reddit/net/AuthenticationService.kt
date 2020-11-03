package com.tgayle.reddit.net

import com.tgayle.reddit.auth.AuthenticationParams
import com.tgayle.reddit.auth.AuthenticationState
import kotlinx.serialization.*
import kotlinx.serialization.internal.NamedValueEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.*
import kotlin.reflect.typeOf

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

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> encodeToFieldMap(serializable: T): Map<String, String> {
    val encoder = FieldMapEncoder()
    serializer(typeOf<T>()).serialize(encoder, serializable)
    return encoder.map

}

@OptIn(InternalSerializationApi::class)
class FieldMapEncoder: NamedValueEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule
    val map = mutableMapOf<String, String>()

    override fun encodeTaggedValue(tag: String, value: Any) {
        map[tag] = value.toString()
    }
}