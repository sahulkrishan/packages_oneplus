package com.oneplus.lib.util;

import android.util.Log;

public class LogUtils {
    public static void dumpTrace(String tag) {
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("|----");
            stringBuilder.append(element.toString());
            Log.d(tag, stringBuilder.toString());
        }
    }
}
