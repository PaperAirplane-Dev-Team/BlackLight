# should be removed in the furture
-ignorewarn

# disable obfuscate, BL is open source
-dontobfuscate
# http://stackoverflow.com/a/7587680/832776
-optimizations !code/allocation/variable

-keepattributes *Annotation*
-keepattributes Signature

# Gson
-keep class sun.misc.Unsafe { *; }

# Gson model
-keep class info.papdt.blacklight.model.** { *; }
-keep class info.papdt.blacklight.support.Binded { *; }
-keepclassmembers class * {
    @info.papdt.blacklight.support.Binded *;
}

# picasso
-dontwarn com.squareup.okhttp.**

# httpcore
-dontwarn org.apache.commons.**
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

