package com.android.settings.fuelgauge.batterytip.tips;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.settings.R;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class SmartBatteryTip extends BatteryTip {
    public static final Creator CREATOR = new Creator() {
        public BatteryTip createFromParcel(Parcel in) {
            return new SmartBatteryTip(in, null);
        }

        public BatteryTip[] newArray(int size) {
            return new SmartBatteryTip[size];
        }
    };

    /* synthetic */ SmartBatteryTip(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    public SmartBatteryTip(int state) {
        super(0, state, false);
    }

    private SmartBatteryTip(Parcel in) {
        super(in);
    }

    public CharSequence getTitle(Context context) {
        return context.getString(R.string.battery_tip_smart_battery_title);
    }

    public CharSequence getSummary(Context context) {
        return context.getString(R.string.battery_tip_smart_battery_summary);
    }

    public int getIconId() {
        return R.drawable.ic_perm_device_information_red_24dp;
    }

    public void updateState(BatteryTip tip) {
        this.mState = tip.mState;
    }

    public void log(Context context, MetricsFeatureProvider metricsFeatureProvider) {
        metricsFeatureProvider.action(context, 1350, this.mState);
    }
}
