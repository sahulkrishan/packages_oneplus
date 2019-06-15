package com.oneplus.settings.utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.oneplus.settings.SettingsBaseApplication;

public class OPPrefUtil {
    private static final String PREF_NAME = "OPSettingsPrefs";

    private static SharedPreferences getSharedPreferences() {
        return SettingsBaseApplication.mApplication.getSharedPreferences(PREF_NAME, 0);
    }

    private static Editor getSharedPreferencesEditor() {
        return SettingsBaseApplication.mApplication.getSharedPreferences(PREF_NAME, 0).edit();
    }

    public static void putInt(String key, int value) {
        Editor editor = getSharedPreferencesEditor();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(String key, int defaultValue) {
        return getSharedPreferences().getInt(key, defaultValue);
    }

    public static void putLong(String key, long value) {
        Editor editor = getSharedPreferencesEditor();
        editor.putLong(key, value);
        editor.apply();
    }

    public static long getLong(String key, long defaultValue) {
        return getSharedPreferences().getLong(key, defaultValue);
    }

    public static void putBoolean(String key, boolean value) {
        Editor editor = getSharedPreferencesEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getSharedPreferences().getBoolean(key, defaultValue);
    }

    public static void putString(String key, String value) {
        Editor editor = getSharedPreferencesEditor();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key, String defaultValue) {
        return getSharedPreferences().getString(key, defaultValue);
    }
}
