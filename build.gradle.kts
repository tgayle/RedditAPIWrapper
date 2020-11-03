import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
}

group = "com.tgayle"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // OkHttp/Retrofit Request Logger
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    // Retrofit HTTP Client
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Retrofit Serializer for kotlin models to JSON
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")

    // Kotlin/JSON Serializer
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}