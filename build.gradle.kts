// =============================================================================
// build.gradle.kts — root project build script (MindWalk)
//
// Declares plugin versions for the entire build. All plugins are applied with
// `apply false` so they are resolved here but only activated in the :app module
// that explicitly applies them.
//
// Plugin versions:
//   AGP (Android Gradle Plugin) 9.1.0  — compiles, packages, and signs APKs
//   Kotlin Android 2.2.10              — Kotlin language support for Android
//   Kotlin Compose Compiler 2.2.10     — separate Compose compiler plugin (Kotlin 2.x)
//   KSP 2.2.10-2.0.2                   — Kotlin Symbol Processing for Room codegen
//   Dokka 2.0.0                        — KDoc → HTML documentation generator
// =============================================================================
plugins {
    id("com.android.application") version "9.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
}
