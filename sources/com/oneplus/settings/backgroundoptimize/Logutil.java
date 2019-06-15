package com.oneplus.settings.backgroundoptimize;

import android.util.Log;
import com.oneplus.settings.SettingsBaseApplication;

public class Logutil {
    public static void loge(String tag, String msg) {
        if (SettingsBaseApplication.ONEPLUS_DEBUG) {
            Log.e(tag, msg);
        }
    }
}
