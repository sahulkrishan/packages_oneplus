package com.android.settings.connecteddevice;

import android.content.Context;
import com.android.settings.core.BasePreferenceController;

public class OPRecognizedCarKitsGroupController extends BasePreferenceController {
    private static final String KEY = "recognized_bluetooth_car_kits";

    public OPRecognizedCarKitsGroupController(Context context, String preferenceKey) {
        super(context, KEY);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            return 0;
        }
        return 2;
    }

    public String getPreferenceKey() {
        return KEY;
    }
}
