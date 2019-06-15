package com.oneplus.settings.ringtone;

import android.util.Log;

public class OPMyLog {
    private static final boolean DEBUG = false;

    public static void d(String tag, String msg) {
    }

    public static void e(String tag, String msg) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(tag);
        stringBuilder.append("] ");
        stringBuilder.append(msg);
        Log.e("chenhl", stringBuilder.toString());
    }

    public static void e(String tag, String msg, Throwable tr) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(tag);
        stringBuilder.append("] ");
        stringBuilder.append(msg);
        Log.e("chenhl", stringBuilder.toString(), tr);
    }
}
