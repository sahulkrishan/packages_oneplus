package com.android.settings.fuelgauge.batterytip;

import android.os.BatteryStats.HistoryItem;
import com.android.settings.fuelgauge.BatteryInfo.BatteryDataParser;

public class HighUsageDataParser implements BatteryDataParser {
    private int mBatteryDrain;
    private byte mEndBatteryLevel;
    private long mEndTimeMs;
    private byte mLastPeriodBatteryLevel;
    private int mThreshold;
    private final long mTimePeriodMs;

    public HighUsageDataParser(long timePeriodMs, int threshold) {
        this.mTimePeriodMs = timePeriodMs;
        this.mThreshold = threshold;
    }

    public void onParsingStarted(long startTime, long endTime) {
        this.mEndTimeMs = endTime;
    }

    public void onDataPoint(long time, HistoryItem record) {
        if (time == 0 || record.currentTime <= this.mEndTimeMs - this.mTimePeriodMs) {
            this.mLastPeriodBatteryLevel = record.batteryLevel;
        }
        this.mEndBatteryLevel = record.batteryLevel;
    }

    public void onDataGap() {
    }

    public void onParsingDone() {
        this.mBatteryDrain = this.mLastPeriodBatteryLevel - this.mEndBatteryLevel;
    }

    public boolean isDeviceHeavilyUsed() {
        return this.mBatteryDrain > this.mThreshold;
    }
}
