// =============================================================================
// settings.gradle.kts — Gradle settings for the MindWalk project
//
// Defines repository sources and the project/module structure.
// =============================================================================

pluginManagement {
    // Repositories searched when resolving Gradle plugin artifacts
    repositories {
        google()            // AGP, KSP, AndroidX, Compose Compiler
        mavenCentral()      // Kotlin, Retrofit, OkHttp, Room, OSMDroid
        gradlePluginPortal() // Community Gradle plugins
    }
}

dependencyResolutionManagement {
    // Repositories searched when resolving library dependencies declared in build scripts
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MindWalk"

// Declares the single application module; additional library modules would be added here
include(":app")
