package com.android.settings.fuelgauge.batterytip.tips;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.annotation.VisibleForTesting;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import java.util.List;

public class HighUsageTip extends BatteryTip {
    public static final Creator CREATOR = new Creator() {
        public BatteryTip createFromParcel(Parcel in) {
            return new HighUsageTip(in);
        }

        public BatteryTip[] newArray(int size) {
            return new HighUsageTip[size];
        }
    };
    @VisibleForTesting
    final List<AppInfo> mHighUsageAppList;
    private final long mLastFullChargeTimeMs;

    public HighUsageTip(long lastFullChargeTimeMs, List<AppInfo> appList) {
        super(2, appList.isEmpty() ? 2 : 0, true);
        this.mLastFullChargeTimeMs = lastFullChargeTimeMs;
        this.mHighUsageAppList = appList;
    }

    @VisibleForTesting
    HighUsageTip(Parcel in) {
        super(in);
        this.mLastFullChargeTimeMs = in.readLong();
        this.mHighUsageAppList = in.createTypedArrayList(AppInfo.CREATOR);
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.mLastFullChargeTimeMs);
        dest.writeTypedList(this.mHighUsageAppList);
    }

    public CharSequence getTitle(Context context) {
        return context.getString(R.string.battery_tip_high_usage_title);
    }

    public CharSequence getSummary(Context context) {
        return context.getString(R.string.battery_tip_high_usage_summary);
    }

    public int getIconId() {
        return R.drawable.ic_perm_device_information_red_24dp;
    }

    public void updateState(BatteryTip tip) {
        this.mState = tip.mState;
    }

    public void log(Context context, MetricsFeatureProvider metricsFeatureProvider) {
        metricsFeatureProvider.action(context, 1348, this.mState);
        int size = this.mHighUsageAppList.size();
        for (int i = 0; i < size; i++) {
            metricsFeatureProvider.action(context, 1354, ((AppInfo) this.mHighUsageAppList.get(i)).packageName, new Pair[0]);
        }
    }

    public long getLastFullChargeTimeMs() {
        return this.mLastFullChargeTimeMs;
    }

    public List<AppInfo> getHighUsageAppList() {
        return this.mHighUsageAppList;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        stringBuilder.append(" {");
        int size = this.mHighUsageAppList.size();
        for (int i = 0; i < size; i++) {
            AppInfo appInfo = (AppInfo) this.mHighUsageAppList.get(i);
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(" ");
            stringBuilder2.append(appInfo.toString());
            stringBuilder2.append(" ");
            stringBuilder.append(stringBuilder2.toString());
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
