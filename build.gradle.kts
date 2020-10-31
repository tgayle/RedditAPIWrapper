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

    // Retrofit HTTP Client
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Retrofit Serializer for kotlin models to JSON
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")

    // Kotlin/JSON Serializer
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
}
