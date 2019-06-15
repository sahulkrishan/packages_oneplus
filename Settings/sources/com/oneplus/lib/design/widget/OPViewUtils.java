package com.oneplus.lib.design.widget;

import android.graphics.PorterDuff.Mode;

class OPViewUtils {
    OPViewUtils() {
    }

    static Mode parseTintMode(int value, Mode defaultMode) {
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
            default:
                return defaultMode;
        }
    }
}
