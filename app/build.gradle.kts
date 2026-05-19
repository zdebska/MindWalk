// =============================================================================
// app/build.gradle.kts — MindWalk application module build script
//
// Configures the single :app module. Plugin declarations mirror the versions
// pinned in the root build.gradle.kts. KSP is required by Room to generate
// DAO implementation classes at compile time.
// =============================================================================

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Kotlin Compose compiler plugin (separate from the Kotlin plugin since 2.0)
    id("org.jetbrains.kotlin.plugin.compose")
    // Kotlin Symbol Processing — used by Room to generate DAO/database code
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.mindwalk"

    // compileSdk 35 = Android 15; allows use of the latest APIs during compilation
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mindwalk"

        // minSdk 26 = Android 8.0; required for Java 8 time APIs and modern Location APIs
        minSdk = 26
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Enables vector drawables backport for API < 21 (not strictly needed at minSdk 26
        // but retained as a safe default)
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // Minification disabled for thesis build; enable for production to shrink APK
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Java 8 required for Kotlin coroutines and lambda-friendly APIs
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        // Enable Jetpack Compose support
        compose = true
    }

    packaging {
        resources {
            // Exclude duplicate licence files pulled in by OkHttp / Retrofit transitive deps
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // ── Compose BOM ───────────────────────────────────────────────────────────
    // Bill of Materials: pins all androidx.compose.* library versions consistently.
    // Must be declared before individual Compose artifacts.
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))

    // ── Core Compose UI ───────────────────────────────────────────────────────
    implementation("androidx.compose.ui:ui")
    // Layout previews inside Android Studio
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Material Design 3 components (cards, buttons, chips, sliders, dialogs …)
    implementation("androidx.compose.material3:material3")
    // Full Material icon set (>2 000 icons); used for Eco, LocationOn, WifiOff, etc.
    implementation("androidx.compose.material:material-icons-extended")

    // ── Debug tooling ─────────────────────────────────────────────────────────
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ── Instrumented tests ────────────────────────────────────────────────────
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // ── Navigation ────────────────────────────────────────────────────────────
    // Jetpack Navigation Compose: NavHost, NavController, composable() DSL
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ── Lifecycle / ViewModel ─────────────────────────────────────────────────
    // viewModel() and collectAsStateWithLifecycle() for Compose integration
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    // ComponentActivity base required for rememberLauncherForActivityResult
    implementation("androidx.activity:activity-compose:1.9.0")

    // ── Map rendering ─────────────────────────────────────────────────────────
    // OSMDroid: offline-capable OpenStreetMap tile renderer embedded in an AndroidView
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // ── HTTP client ───────────────────────────────────────────────────────────
    // OkHttp: underlying HTTP engine for Retrofit; also used directly for Nominatim calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // LoggingInterceptor: logs request/response bodies in debug builds
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ── REST client ───────────────────────────────────────────────────────────
    // Retrofit 2: type-safe HTTP interface for the Azure Python route backend and OSRM
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson converter: deserialises JSON responses into data classes (RouteResponse, etc.)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ── Local database ────────────────────────────────────────────────────────
    // Room runtime: SQLite ORM for SavedRoute and WalkRecord persistence
    implementation("androidx.room:room-runtime:2.7.2")
    // Room coroutine extensions: suspend DAO functions and Flow support
    implementation("androidx.room:room-ktx:2.7.2")
    // KSP annotation processor: generates RoomDatabase and DAO implementations
    ksp("androidx.room:room-compiler:2.7.2")
}
