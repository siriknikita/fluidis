# Fluidis ProGuard Rules

# kotlinx-serialization (used by type-safe navigation routes)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.fluidis.app.**$$serializer { *; }
-keepclassmembers class com.fluidis.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.fluidis.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
