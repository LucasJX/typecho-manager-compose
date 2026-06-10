-dontwarn javax.annotation.**
-keepattributes *Annotation*

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.flypigs.typechomanager.**$$serializer { *; }
-keepclassmembers class com.flypigs.typechomanager.** { *** Companion; }
-keepclasseswithmembers class com.flypigs.typechomanager.** { kotlinx.serialization.KSerializer serializer(...); }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
