# ClearSpend ProGuard / R8 Rules

# Retain Room database entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# Generative AI / Gemini SDK
-keep class com.google.ai.client.generativeai.** { *; }

# Kotlinx Serialization / Coroutines
-keepclassmembers class * {
    @kotlinx.coroutines.InternalCoroutinesApi *;
}
