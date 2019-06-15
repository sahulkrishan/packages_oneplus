package com.android.settings.fuelgauge.batterytip.tips;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class SummaryTip extends BatteryTip {
    public static final Creator CREATOR = new Creator() {
        public BatteryTip createFromParcel(Parcel in) {
            return new SummaryTip(in);
        }

        public BatteryTip[] newArray(int size) {
            return new SummaryTip[size];
        }
    };
    private long mAverageTimeMs;

    public SummaryTip(int state, long averageTimeMs) {
        super(6, state, true);
        this.mAverageTimeMs = averageTimeMs;
    }

    @VisibleForTesting
    SummaryTip(Parcel in) {
        super(in);
        this.mAverageTimeMs = in.readLong();
    }

    public CharSequence getTitle(Context context) {
        return context.getString(R.string.battery_tip_summary_title);
    }

    public CharSequence getSummary(Context context) {
        return context.getString(R.string.battery_tip_summary_summary);
    }

    public int getIconId() {
        return R.drawable.ic_battery_status_good_24dp;
    }

    public void updateState(BatteryTip tip) {
        this.mState = tip.mState;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.mAverageTimeMs);
    }

    public void log(Context context, MetricsFeatureProvider metricsFeatureProvider) {
        metricsFeatureProvider.action(context, 1349, this.mState);
    }

    public long getAverageTimeMs() {
        return this.mAverageTimeMs;
    }
}
