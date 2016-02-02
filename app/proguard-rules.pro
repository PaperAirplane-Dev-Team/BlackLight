# should be removed in the furture
-ignorewarn

# disable obfuscate, BL is open source
-dontobfuscate
# http://stackoverflow.com/a/7587680/832776
#-optimizationpasses 5 
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
-dontwarn com.squareup.okhttp3.**

# Gif module
-keep class pl.droidsonroids.gif.**
