package com.tgayle.reddit.net

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter

fun defaultJsonConverter(builder: JsonBuilder.() -> Unit = {}): Converter.Factory {
    val jsonConverter = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        this.builder()
    }.asConverterFactory("application/json".toMediaType())

    return jsonConverter
}