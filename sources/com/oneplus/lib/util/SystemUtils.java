package com.oneplus.lib.util;

import android.os.Build.VERSION;

public class SystemUtils {
    private SystemUtils() {
    }

    public static boolean isAtLeastN() {
        return VERSION.SDK_INT >= 24;
    }

    public static boolean isAtLeastNMR1() {
        return VERSION.SDK_INT >= 25;
    }

    public static boolean isAtLeastO() {
        return !"REL".equals(VERSION.CODENAME) && ("O".equals(VERSION.CODENAME) || VERSION.CODENAME.startsWith("OMR"));
    }

    public static boolean isAtLeastM() {
        return VERSION.SDK_INT >= 23;
    }
}
