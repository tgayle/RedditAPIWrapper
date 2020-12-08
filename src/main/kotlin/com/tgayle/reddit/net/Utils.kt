package com.tgayle.reddit.net

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter

fun defaultJsonConverter(builder: JsonBuilder.() -> Unit = {}): Json {
    val jsonConverter = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "kind"
        this.builder()
    }

    return jsonConverter
}

fun Json.asJsonConverterFactory() = this.asConverterFactory("application/json".toMediaType())