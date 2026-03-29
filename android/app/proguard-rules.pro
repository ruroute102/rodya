# ТехГид ProGuard rules

# ── Retrofit + Moshi ────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# ── DTO ─────────────────────────────────────────────────────────
-keep class ru.techgid.data.remote.dto.** { *; }

# ── Room ────────────────────────────────────────────────────────
-keep class ru.techgid.data.local.entity.** { *; }

# ── Enum ────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Compose ─────────────────────────────────────────────────────
-dontwarn androidx.compose.**

# ── R8 full mode ────────────────────────────────────────────────
-allowaccessmodification
-repackageclasses ''
