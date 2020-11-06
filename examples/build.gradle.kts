import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
//    id("org.openjfx.javafxplugin") version "0.0.8"
    id("org.jetbrains.compose") version "0.1.0-build63"
    application
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(kotlin("stdlib"))

//    implementation("no.tornado:tornadofx:1.7.20")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")

    implementation(rootProject)
    implementation(compose.desktop.all)

}

//javafx {
//    version = "11.0.2"
//    modules = listOf("base", "controls", "graphics", "media", "web").map { "javafx.$it" }
//}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
//    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "examples.ExampleKt"
}