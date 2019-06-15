package com.android.settings.fuelgauge.batterytip.tips;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.settings.R;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class LowBatteryTip extends EarlyWarningTip {
    public static final Creator CREATOR = new Creator() {
        public BatteryTip createFromParcel(Parcel in) {
            return new LowBatteryTip(in);
        }

        public BatteryTip[] newArray(int size) {
            return new LowBatteryTip[size];
        }
    };
    private CharSequence mSummary;

    public LowBatteryTip(int state, boolean powerSaveModeOn, CharSequence summary) {
        super(state, powerSaveModeOn);
        this.mType = 5;
        this.mSummary = summary;
    }

    public LowBatteryTip(Parcel in) {
        super(in);
        this.mSummary = in.readCharSequence();
    }

    public CharSequence getSummary(Context context) {
        if (this.mState == 1) {
            return context.getString(R.string.battery_tip_early_heads_up_done_summary);
        }
        return this.mSummary;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeCharSequence(this.mSummary);
    }

    public void log(Context context, MetricsFeatureProvider metricsFeatureProvider) {
        metricsFeatureProvider.action(context, 1352, this.mState);
    }
}
