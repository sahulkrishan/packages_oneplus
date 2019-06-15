package com.oneplus.lib.util;

import android.content.Context;

public class CheckOnePlusDeviceUtil {
    public static final String FEATURE_ONEPLUS_DEVICE = "com.oneplus.software.oos";

    public static boolean isOnePlusDevice(Context context) {
        return context.getPackageManager().hasSystemFeature(FEATURE_ONEPLUS_DEVICE);
    }
}
