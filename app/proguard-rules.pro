# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Retrofit ──
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ── Gson ──
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Keep network data classes used by Gson/Retrofit
-keep class com.ferlagod.miscuras.network.FormPayload { *; }

# ── Room ──
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# ── SQLCipher ──
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# ── Google Generative AI (Gemini) ──
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# ── ARCore / Sceneview ──
-keep class com.google.ar.** { *; }
-dontwarn com.google.ar.**
-keep class io.github.sceneview.** { *; }
-dontwarn io.github.sceneview.**

# ── Vico Charts ──
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# ── Coil ──
-dontwarn coil.**

# ── Data classes / Entities (prevent field stripping) ──
-keep class com.ferlagod.miscuras.data.entities.** { *; }

# ── Google ErrorProne Annotations (Tink/Security Crypto) ──
-dontwarn com.google.errorprone.annotations.**

# ── Koin ──
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep ViewModels and their constructors for Koin reflection
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepnames class * extends androidx.lifecycle.ViewModel
# ── Kotlin Metadata (Needed for Room and Koin reflection) ──
-keep class kotlin.Metadata { *; }

# ── Backup Models ──
-keep class com.ferlagod.miscuras.data.models.** { *; }

# ── Jetpack Navigation Compose ──
-keep class androidx.navigation.** { *; }
