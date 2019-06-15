package com.android.settings.fuelgauge;

public class Estimate {
    public static final int AVERAGE_TIME_TO_DISCHARGE_UNKNOWN = -1;
    public final long averageDischargeTime;
    public final long estimateMillis;
    public final boolean isBasedOnUsage;

    public Estimate(long estimateMillis, boolean isBasedOnUsage, long averageDischargeTime) {
        this.estimateMillis = estimateMillis;
        this.isBasedOnUsage = isBasedOnUsage;
        this.averageDischargeTime = averageDischargeTime;
    }
}
