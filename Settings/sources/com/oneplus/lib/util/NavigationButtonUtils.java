package com.oneplus.lib.util;

import android.content.Context;
import android.provider.Settings.System;

public class NavigationButtonUtils {
    private static final String KEY_OP_NAVIGATION_BAR_TYPE = "op_navigation_bar_type";
    private static final int TYPE_GESTURE_NAVIGATION_BAR = 3;

    public static boolean isGestureNavigationBar(Context context) {
        if (System.getInt(context.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, 1) == 3) {
            return true;
        }
        return false;
    }
}
