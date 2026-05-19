# =============================================================================
# proguard-rules.pro — MindWalk app-specific R8/ProGuard rules
#
# Applied alongside the default android-optimize.txt when isMinifyEnabled = true.
# Currently minification is disabled for the thesis build; these rules are kept
# here so they are correct when minification is turned on for production.
# =============================================================================

# ── Retrofit 2 ───────────────────────────────────────────────────────────────
# Retrofit uses reflection to read @GET/@POST annotations on interface methods.
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ── Gson ─────────────────────────────────────────────────────────────────────
# Gson deserialises JSON into data classes by field name; stripping those fields
# would break all API responses.
-keep class com.example.mindwalk.data.** { *; }
-keepclassmembers class com.example.mindwalk.data.** { *; }
-keep class sun.misc.Unsafe { *; }

# ── OkHttp ───────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ── Room ─────────────────────────────────────────────────────────────────────
# Room generates implementation classes at compile time via KSP; at runtime it
# looks them up by name, so they must not be renamed.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# ── OSMDroid ─────────────────────────────────────────────────────────────────
-dontwarn org.osmdroid.**
-keep class org.osmdroid.** { *; }

# ── Kotlin coroutines ─────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Debug stack traces ───────────────────────────────────────────────────────
# Preserve line numbers in obfuscated stack traces for easier crash analysis.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
