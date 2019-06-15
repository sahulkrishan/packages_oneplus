package com.android.settings.fuelgauge.batterytip.tips;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.settings.R;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class EarlyWarningTip extends BatteryTip {
    public static final Creator CREATOR = new Creator() {
        public BatteryTip createFromParcel(Parcel in) {
            return new EarlyWarningTip(in);
        }

        public BatteryTip[] newArray(int size) {
            return new EarlyWarningTip[size];
        }
    };
    private boolean mPowerSaveModeOn;

    public EarlyWarningTip(int state, boolean powerSaveModeOn) {
        super(3, state, false);
        this.mPowerSaveModeOn = powerSaveModeOn;
    }

    public EarlyWarningTip(Parcel in) {
        super(in);
        this.mPowerSaveModeOn = in.readBoolean();
    }

    public CharSequence getTitle(Context context) {
        int i;
        if (this.mState == 1) {
            i = R.string.battery_tip_early_heads_up_done_title;
        } else {
            i = R.string.battery_tip_early_heads_up_title;
        }
        return context.getString(i);
    }

    public CharSequence getSummary(Context context) {
        int i;
        if (this.mState == 1) {
            i = R.string.battery_tip_early_heads_up_done_summary;
        } else {
            i = R.string.battery_tip_early_heads_up_summary;
        }
        return context.getString(i);
    }

    public int getIconId() {
        if (this.mState == 1) {
            return R.drawable.ic_battery_status_maybe_24dp;
        }
        return R.drawable.ic_battery_status_bad_24dp;
    }

    public void updateState(BatteryTip tip) {
        EarlyWarningTip earlyWarningTip = (EarlyWarningTip) tip;
        if (earlyWarningTip.mState == 0) {
            this.mState = 0;
        } else {
            if (this.mState == 0) {
                int i = 2;
                if (earlyWarningTip.mState == 2) {
                    if (earlyWarningTip.mPowerSaveModeOn) {
                        i = 1;
                    }
                    this.mState = i;
                }
            }
            this.mState = earlyWarningTip.getState();
        }
        this.mPowerSaveModeOn = earlyWarningTip.mPowerSaveModeOn;
    }

    public void log(Context context, MetricsFeatureProvider metricsFeatureProvider) {
        metricsFeatureProvider.action(context, 1351, this.mState);
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeBoolean(this.mPowerSaveModeOn);
    }

    public boolean isPowerSaveModeOn() {
        return this.mPowerSaveModeOn;
    }
}
