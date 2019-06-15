package com.android.settings.connecteddevice;

import android.content.Context;
import com.android.settings.core.BasePreferenceController;

public class OPOtherPairedBluetoothDeviceController extends BasePreferenceController {
    private static final String KEY = "other_paired_bluetooth_devices";

    public OPOtherPairedBluetoothDeviceController(Context context, String preferenceKey) {
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
