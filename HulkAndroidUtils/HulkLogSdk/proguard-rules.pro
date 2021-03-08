# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

#keep all inreface
-keepclasseswithmembers interface * { *; }

# keep class of Byod-module
-keep class com.hulk.android.log.** { *; }
-keep class com.hulk.android.report.** { *; }

#keep hulk.util
-keep class com.hulk.util.** { *; }


