package com.oneplus.lib.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import java.util.Map;

public class PreferenceUtils {
    private static final String MESSAGEDATA = "messagedata";
    private static final String SIMINFODATA = "siminfodata";

    public static void writeMessageData(Context context, long time, String title, String content, String url, String id) {
        Editor editor = context.getSharedPreferences(MESSAGEDATA, 0).edit();
        String valueOf = String.valueOf(time);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(title);
        stringBuilder.append(",");
        stringBuilder.append(content);
        stringBuilder.append(",");
        stringBuilder.append(url);
        stringBuilder.append(",");
        stringBuilder.append(id);
        editor.putString(valueOf, stringBuilder.toString());
        editor.apply();
    }

    public static Map<?, ?> getAllMessageData(Context context) {
        return context.getSharedPreferences(MESSAGEDATA, 0).getAll();
    }

    public static void deleteMessageData(Context context, long time) {
        Editor editor = context.getSharedPreferences(MESSAGEDATA, 0).edit();
        editor.remove(String.valueOf(time));
        editor.apply();
    }

    public static void writeSimInfoData(Context context, String country, String operator, int index) {
        Editor editor = context.getSharedPreferences(SIMINFODATA, 0).edit();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SIMINFODATA);
        stringBuilder.append(index);
        String stringBuilder2 = stringBuilder.toString();
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append(country);
        stringBuilder3.append(",");
        stringBuilder3.append(operator);
        stringBuilder3.append(",");
        editor.putString(stringBuilder2, stringBuilder3.toString());
        editor.apply();
    }

    public static String readSimInfoData(Context context, int index) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SIMINFODATA, 0);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SIMINFODATA);
        stringBuilder.append(index);
        return sharedPreferences.getString(stringBuilder.toString(), null);
    }

    public static void removeSimInfoData(Context context, int index) {
        Editor editor = context.getSharedPreferences(MESSAGEDATA, 0).edit();
        editor.remove(SIMINFODATA);
        editor.apply();
    }

    public static void clearMesData(Context context) {
        Editor editor = context.getSharedPreferences(MESSAGEDATA, 0).edit();
        editor.clear();
        editor.apply();
    }

    public static void applyInt(Context context, String key, int value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    public static boolean contains(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).contains(key);
    }
}
