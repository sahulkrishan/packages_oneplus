package com.android.settings.fuelgauge.batterytip.detectors;

import android.content.Context;
import android.content.IntentFilter;
import android.os.PowerManager;
import com.android.settings.fuelgauge.PowerUsageFeatureProvider;
import com.android.settings.fuelgauge.batterytip.BatteryTipPolicy;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.EarlyWarningTip;
import com.android.settings.overlay.FeatureFactory;

public class EarlyWarningDetector implements BatteryTipDetector {
    private Context mContext;
    private BatteryTipPolicy mPolicy;
    private PowerManager mPowerManager;
    private PowerUsageFeatureProvider mPowerUsageFeatureProvider;

    public EarlyWarningDetector(BatteryTipPolicy policy, Context context) {
        this.mPolicy = policy;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mContext = context;
        this.mPowerUsageFeatureProvider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
    }

    public BatteryTip detect() {
        int state = 0;
        boolean discharging = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("plugged", -1) == 0;
        boolean powerSaveModeOn = this.mPowerManager.isPowerSaveMode();
        boolean earlyWarning = this.mPowerUsageFeatureProvider.getEarlyWarningSignal(this.mContext, EarlyWarningDetector.class.getName()) || this.mPolicy.testBatterySaverTip;
        if (powerSaveModeOn) {
            state = 1;
        } else if (!(this.mPolicy.batterySaverTipEnabled && discharging && earlyWarning)) {
            state = 2;
        }
        return new EarlyWarningTip(state, powerSaveModeOn);
    }
}
