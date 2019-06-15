package com.android.settingslib;

import android.content.Context;
import android.provider.Settings.Global;

public class WirelessUtils {
    public static boolean isRadioAllowed(Context context, String type) {
        boolean z = true;
        if (!isAirplaneModeOn(context)) {
            return true;
        }
        String toggleable = Global.getString(context.getContentResolver(), "airplane_mode_toggleable_radios");
        if (toggleable == null || !toggleable.contains(type)) {
            z = false;
        }
        return z;
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
    }
}
