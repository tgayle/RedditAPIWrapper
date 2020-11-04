
plugins {
    kotlin("jvm")
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")

    implementation(rootProject)
}

javafx {
    version = "11.0.2"
    modules = listOf("base", "controls", "graphics", "media", "web").map { "javafx.$it" }
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "11"
}