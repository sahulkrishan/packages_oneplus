package com.android.settings.fuelgauge;

import android.content.Context;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settingslib.utils.AsyncLoader;

public class BatteryInfoLoader extends AsyncLoader<BatteryInfo> {
    private static final String LOG_TAG = "BatteryInfoLoader";
    @VisibleForTesting
    BatteryUtils batteryUtils;
    BatteryStatsHelper mStatsHelper;

    public BatteryInfoLoader(Context context, BatteryStatsHelper batteryStatsHelper) {
        super(context);
        this.mStatsHelper = batteryStatsHelper;
        this.batteryUtils = BatteryUtils.getInstance(context);
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(BatteryInfo result) {
    }

    public BatteryInfo loadInBackground() {
        return this.batteryUtils.getBatteryInfo(this.mStatsHelper, LOG_TAG);
    }
}
