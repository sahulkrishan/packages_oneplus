package com.android.settings.fuelgauge.batterytip.tips;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class UnrestrictAppTip extends BatteryTip {
    public static final Creator CREATOR = new Creator() {
        public BatteryTip createFromParcel(Parcel in) {
            return new UnrestrictAppTip(in);
        }

        public BatteryTip[] newArray(int size) {
            return new UnrestrictAppTip[size];
        }
    };
    private AppInfo mAppInfo;

    public UnrestrictAppTip(int state, AppInfo appInfo) {
        super(7, state, true);
        this.mAppInfo = appInfo;
    }

    @VisibleForTesting
    UnrestrictAppTip(Parcel in) {
        super(in);
        this.mAppInfo = (AppInfo) in.readParcelable(getClass().getClassLoader());
    }

    public CharSequence getTitle(Context context) {
        return null;
    }

    public CharSequence getSummary(Context context) {
        return null;
    }

    public int getIconId() {
        return 0;
    }

    public String getPackageName() {
        return this.mAppInfo.packageName;
    }

    public void updateState(BatteryTip tip) {
        this.mState = tip.mState;
    }

    public void log(Context context, MetricsFeatureProvider metricsFeatureProvider) {
    }

    public AppInfo getUnrestrictAppInfo() {
        return this.mAppInfo;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.mAppInfo, flags);
    }
}
