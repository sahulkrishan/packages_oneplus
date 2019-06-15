package com.android.settings.fuelgauge.batterytip.detectors;

import android.content.Context;
import android.os.PowerManager;
import com.android.settings.fuelgauge.BatteryInfo;
import com.android.settings.fuelgauge.batterytip.BatteryTipPolicy;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.LowBatteryTip;
import java.util.concurrent.TimeUnit;

public class LowBatteryDetector implements BatteryTipDetector {
    private BatteryInfo mBatteryInfo;
    private BatteryTipPolicy mPolicy;
    private PowerManager mPowerManager;
    private int mWarningLevel;

    public LowBatteryDetector(Context context, BatteryTipPolicy policy, BatteryInfo batteryInfo) {
        this.mPolicy = policy;
        this.mBatteryInfo = batteryInfo;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mWarningLevel = context.getResources().getInteger(17694806);
    }

    public BatteryTip detect() {
        boolean powerSaveModeOn = this.mPowerManager.isPowerSaveMode();
        boolean lowBattery = this.mBatteryInfo.batteryLevel <= this.mWarningLevel || (this.mBatteryInfo.discharging && this.mBatteryInfo.remainingTimeUs < TimeUnit.HOURS.toMicros((long) this.mPolicy.lowBatteryHour));
        int state = 2;
        if (this.mPolicy.lowBatteryEnabled) {
            if (powerSaveModeOn) {
                state = 1;
            } else if (this.mPolicy.testLowBatteryTip || (this.mBatteryInfo.discharging && lowBattery)) {
                state = 0;
            }
        }
        return new LowBatteryTip(state, powerSaveModeOn, this.mBatteryInfo.remainingLabel);
    }
}
