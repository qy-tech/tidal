# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class androidx.hilt.** { *; }

#-dontwarn com.android.org.conscrypt.TrustManagerImpl
#-dontwarn dalvik.system.BlockGuard$VmPolicy
#-dontwarn dalvik.system.CloseGuard
#-dontwarn libcore.util.NativeAllocationRegistry
#-dontwarn org.osgi.service.component.annotations.Component
#-dontwarn org.osgi.service.metatype.annotations.Designate
#-dontwarn sun.misc.Cleaner

#-keepattributes SourceFile
#-keepattributes LineNumberTable
#-keep class com.google.gson.** { *; }
-keep class com.google.common.reflect.** {*;}
-keep class open.android.lib.dsm.** {*;}
#
-keep class com.qytech.tidal.data.** { *; }
#-keep interface retrofit2.** { *; }
#-keep class retrofit2.** { *; }