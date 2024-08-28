@file:Suppress("PropertyName")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.polyfrost.org/releases") // Adds the Polyfrost maven repository to get Polyfrost Gradle Toolkit
    }
    plugins {
    }
}

val mod_name: String by settings

// Configures the root project Gradle name based on the value in `gradle.properties`
rootProject.name = mod_name