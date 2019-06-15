package com.android.settings.fuelgauge.batterytip.detectors;

import android.content.ContentResolver;
import android.provider.Settings.Global;
import com.android.settings.fuelgauge.batterytip.BatteryTipPolicy;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.SmartBatteryTip;

public class SmartBatteryDetector implements BatteryTipDetector {
    private ContentResolver mContentResolver;
    private BatteryTipPolicy mPolicy;

    public SmartBatteryDetector(BatteryTipPolicy policy, ContentResolver contentResolver) {
        this.mPolicy = policy;
        this.mContentResolver = contentResolver;
    }

    public BatteryTip detect() {
        boolean z = true;
        int state = 0;
        if (!(Global.getInt(this.mContentResolver, "adaptive_battery_management_enabled", 1) == 0 || this.mPolicy.testSmartBatteryTip)) {
            z = false;
        }
        if (!z) {
            state = 2;
        }
        return new SmartBatteryTip(state);
    }
}
