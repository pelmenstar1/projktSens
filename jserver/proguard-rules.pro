-keepclassmembers class * {
    public static final com.pelmenstar.projktSens.shared.serialization.ObjectSerializer SERIALIZER;
}

-keepnames class * extends com.pelmenstar.projktSens.shared.android.ui.settings.Setting

-keepclassmembers class com.pelmenstar.projktSens.jserver.AppPreferences {
    public static final com.pelmenstar.projktSens.jserver.AppPreferences INSTANCE;
}