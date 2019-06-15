package com.oneplus.lib.util;

import android.graphics.PorterDuff.Mode;
import android.os.Build.VERSION;

public class DrawableUtils {
    public static Mode parseTintMode(int value, Mode defaultMode) {
        if (value == 3) {
            return Mode.SRC_OVER;
        }
        if (value == 5) {
            return Mode.SRC_IN;
        }
        if (value == 9) {
            return Mode.SRC_ATOP;
        }
        switch (value) {
            case 14:
                return Mode.MULTIPLY;
            case 15:
                return Mode.SCREEN;
            case 16:
                return VERSION.SDK_INT >= 11 ? Mode.valueOf("ADD") : defaultMode;
            default:
                return defaultMode;
        }
    }
}
