-optimizationpasses 5 
-keepattributes *Annotation*
-keepattributes Signature

-keep class sun.misc.Unsafe { *; }

-keep class info.papdt.blacklight.model.** { *; }
-keep class info.papdt.blacklight.support.Binded { *; }
-keepclassmembers class * {
    @info.papdt.blacklight.support.Binded *;
}

-dontwarn android.support.**
  
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
