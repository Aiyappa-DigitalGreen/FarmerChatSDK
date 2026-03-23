# FarmerChat SDK consumer ProGuard rules

# Keep SDK public API
-keep public class com.farmerchat.sdk.FarmerChatSdk { *; }
-keep public class com.farmerchat.sdk.FarmerChatConfig { *; }
-keep public class com.farmerchat.sdk.FarmerChatFab { *; }
-keep public interface com.farmerchat.sdk.api.SdkAnalyticsListener { *; }
-keep public interface com.farmerchat.sdk.auth.TokenStore { *; }

# Keep domain models for Gson
-keep class com.farmerchat.sdk.domain.model.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Koin
-keep class org.koin.** { *; }
