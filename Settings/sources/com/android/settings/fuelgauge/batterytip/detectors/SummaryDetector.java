package com.android.settings.fuelgauge.batterytip.detectors;

import com.android.settings.fuelgauge.batterytip.BatteryTipPolicy;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.SummaryTip;

public class SummaryDetector implements BatteryTipDetector {
    private long mAverageTimeMs;
    private BatteryTipPolicy mPolicy;

    public SummaryDetector(BatteryTipPolicy policy, long averageTimeMs) {
        this.mPolicy = policy;
        this.mAverageTimeMs = averageTimeMs;
    }

    public BatteryTip detect() {
        int state;
        if (this.mPolicy.summaryEnabled) {
            state = 0;
        } else {
            state = 2;
        }
        return new SummaryTip(state, this.mAverageTimeMs);
    }
}
