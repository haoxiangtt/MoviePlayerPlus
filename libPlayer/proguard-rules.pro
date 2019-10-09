# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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


#android全局混淆配置
-ignorewarnings                     # 忽略警告，避免打包时某些警告出现
-optimizationpasses 5               # 指定代码的压缩级别
-dontusemixedcaseclassnames         # 是否使用大小写混合 混淆时不会产生形形色色的类名
-dontskipnonpubliclibraryclasses    # 是否混淆第三方jar
-verbose                            # 混淆时是否记录日志

-dontpreverify                      # 混淆时是否做预校验
-dontoptimize                       # 不优化输入的类文件

-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

-keepattributes Exceptions, *Annotation*, SourceFile, InnerClasses, LineNumberTable, Signature, Deprecated, EnclosingMethod
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*    #优化 混淆时采用的算法

-keep public class * extends android.app.Activity    # 未指定成员，仅仅保持类名不被混淆
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.app.View
-keep public class * extends android.app.IntentService
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.hardware.display.DisplayManager
-keep public class * extends android.os.UserManager
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.app.Fragment

-keep public class * extends android.support.v4.widget
-keep public class * extends android.support.v4.**    #  *匹配任意字符不包括.  **匹配任意字符
-keep interface android.support.v4.app.** { *; }    #{ *;}    表示一个接口中的所有的东西都不被混淆
# 下面这行表示保持这个包下面的所有的类里面的所有内容都不混淆
-keep class android.support.v4.** { *; }
-keep class android.os.**{*;}
-keep class android.support.v8.renderscript.** { *; }

-keep class **.R$* { *; }
-keep class **.R{ *; }

# keep住源文件以及行号
-keepattributes SourceFile,LineNumberTable

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#实现了android.os.Parcelable接口类的任何类，以及其内部定义的Creator内部类类型的public final静态成员变量，都不能被混淆和删除
-keep class * implements android.os.Parcelable {    # 保持Parcelable不被混淆
  public static final android.os.Parcelable$Creator *;
}

-keepclasseswithmembernames class * {     # 保持 native 方法不被混淆
    native <methods>;
}

-keepclasseswithmembers class * {         # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {         # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclasseswithmembers class * {
  public <init>(android.content.Context, android.util.AttributeSet, int, int);
}

-keepclassmembers class * extends android.app.Activity { #保持类成员
   public void *(android.view.View);
}

#-keepclassmembers class * extends android.content.Context {
#  public void *(android.view.View);
#  public void *(android.view.MenuItem);
#}

-keepclassmembers enum * {                  # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
  static final long serialVersionUID;
  private static final java.io.ObjectStreamField[] serialPersistentFields;
  private void writeObject(java.io.ObjectOutputStream);
  private void readObject(java.io.ObjectInputStream);
  java.lang.Object writeReplace();
  java.lang.Object readResolve();
}

#-libraryjars   libs/jar包名字.jar   #缺省proguard 会检查每一个引用是否正确，但是第三方库里面往往有些不会用到的类，没有正确引用。如果不配置的话，系统就会报错。
-dontwarn android.support.v4.**
-dontwarn android.os.**

-keep class org.videolan.libvlc.**{*;}