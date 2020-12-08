import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "0.3.0-build134"
    application
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
    implementation("io.ktor:ktor-client-core:1.4.0")
    implementation("io.ktor:ktor-client-apache:1.4.0")

    implementation(rootProject)
    implementation(compose.desktop.currentOs)

}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "examples.ExampleKt"
}