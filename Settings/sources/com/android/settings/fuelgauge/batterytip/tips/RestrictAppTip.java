package com.android.settings.fuelgauge.batterytip.tips;

import android.content.Context;
import android.content.res.Resources;
import android.icu.text.ListFormatter;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RestrictAppTip extends BatteryTip {
    public static final Creator CREATOR = new Creator() {
        public BatteryTip createFromParcel(Parcel in) {
            return new RestrictAppTip(in);
        }

        public BatteryTip[] newArray(int size) {
            return new RestrictAppTip[size];
        }
    };
    private List<AppInfo> mRestrictAppList;

    public RestrictAppTip(int state, List<AppInfo> restrictApps) {
        super(1, state, state == 0);
        this.mRestrictAppList = restrictApps;
        this.mNeedUpdate = false;
    }

    public RestrictAppTip(int state, AppInfo appInfo) {
        super(1, state, state == 0);
        this.mRestrictAppList = new ArrayList();
        this.mRestrictAppList.add(appInfo);
        this.mNeedUpdate = false;
    }

    @VisibleForTesting
    RestrictAppTip(Parcel in) {
        super(in);
        this.mRestrictAppList = in.createTypedArrayList(AppInfo.CREATOR);
    }

    public CharSequence getTitle(Context context) {
        CharSequence appLabel;
        int num = this.mRestrictAppList.size();
        if (num > 0) {
            appLabel = Utils.getApplicationLabel(context, ((AppInfo) this.mRestrictAppList.get(0)).packageName);
        } else {
            appLabel = "";
        }
        Resources resources = context.getResources();
        if (this.mState == 1) {
            return resources.getQuantityString(R.plurals.battery_tip_restrict_handled_title, num, new Object[]{appLabel, Integer.valueOf(num)});
        }
        return resources.getQuantityString(R.plurals.battery_tip_restrict_title, num, new Object[]{Integer.valueOf(num)});
    }

    public CharSequence getSummary(Context context) {
        CharSequence appLabel;
        int resId;
        int num = this.mRestrictAppList.size();
        if (num > 0) {
            appLabel = Utils.getApplicationLabel(context, ((AppInfo) this.mRestrictAppList.get(0)).packageName);
        } else {
            appLabel = "";
        }
        if (this.mState == 1) {
            resId = R.plurals.battery_tip_restrict_handled_summary;
        } else {
            resId = R.plurals.battery_tip_restrict_summary;
        }
        return context.getResources().getQuantityString(resId, num, new Object[]{appLabel, Integer.valueOf(num)});
    }

    public int getIconId() {
        if (this.mState == 1) {
            return R.drawable.ic_perm_device_information_green_24dp;
        }
        return R.drawable.ic_battery_alert_24dp;
    }

    public void updateState(BatteryTip tip) {
        if (tip.mState == 0) {
            this.mState = 0;
            this.mRestrictAppList = ((RestrictAppTip) tip).mRestrictAppList;
            this.mShowDialog = true;
        } else if (this.mState == 0 && tip.mState == 2) {
            this.mState = 1;
            this.mShowDialog = false;
        } else {
            this.mState = tip.getState();
            this.mShowDialog = tip.shouldShowDialog();
            this.mRestrictAppList = ((RestrictAppTip) tip).mRestrictAppList;
        }
    }

    public void log(Context context, MetricsFeatureProvider metricsFeatureProvider) {
        metricsFeatureProvider.action(context, 1347, this.mState);
        if (this.mState == 0) {
            int size = this.mRestrictAppList.size();
            for (int i = 0; i < size; i++) {
                AppInfo appInfo = (AppInfo) this.mRestrictAppList.get(i);
                Iterator it = appInfo.anomalyTypes.iterator();
                while (it.hasNext()) {
                    Integer anomalyType = (Integer) it.next();
                    metricsFeatureProvider.action(context, 1353, appInfo.packageName, Pair.create(Integer.valueOf(1366), anomalyType));
                }
            }
        }
    }

    public List<AppInfo> getRestrictAppList() {
        return this.mRestrictAppList;
    }

    public CharSequence getRestrictAppsString(Context context) {
        List<CharSequence> appLabels = new ArrayList();
        int size = this.mRestrictAppList.size();
        for (int i = 0; i < size; i++) {
            appLabels.add(Utils.getApplicationLabel(context, ((AppInfo) this.mRestrictAppList.get(i)).packageName));
        }
        return ListFormatter.getInstance().format(appLabels);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        stringBuilder.append(" {");
        int size = this.mRestrictAppList.size();
        for (int i = 0; i < size; i++) {
            AppInfo appInfo = (AppInfo) this.mRestrictAppList.get(i);
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(" ");
            stringBuilder2.append(appInfo.toString());
            stringBuilder2.append(" ");
            stringBuilder.append(stringBuilder2.toString());
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(this.mRestrictAppList);
    }
}
