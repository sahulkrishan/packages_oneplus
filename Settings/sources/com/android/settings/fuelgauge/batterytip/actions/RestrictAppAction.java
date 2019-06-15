package com.android.settings.fuelgauge.batterytip.actions;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Pair;
import com.android.internal.util.CollectionUtils;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settings.fuelgauge.batterytip.BatteryDatabaseManager;
import com.android.settings.fuelgauge.batterytip.tips.RestrictAppTip;
import java.util.Iterator;
import java.util.List;

public class RestrictAppAction extends BatteryTipAction {
    @VisibleForTesting
    BatteryDatabaseManager mBatteryDatabaseManager;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private RestrictAppTip mRestrictAppTip;

    public RestrictAppAction(Context context, RestrictAppTip tip) {
        super(context);
        this.mRestrictAppTip = tip;
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        this.mBatteryDatabaseManager = BatteryDatabaseManager.getInstance(context);
    }

    public void handlePositiveAction(int metricsKey) {
        List<AppInfo> appInfos = this.mRestrictAppTip.getRestrictAppList();
        int size = appInfos.size();
        for (int i = 0; i < size; i++) {
            AppInfo appInfo = (AppInfo) appInfos.get(i);
            String packageName = appInfo.packageName;
            this.mBatteryUtils.setForceAppStandby(appInfo.uid, packageName, 1);
            int i2 = 833;
            if (CollectionUtils.isEmpty(appInfo.anomalyTypes)) {
                this.mMetricsFeatureProvider.action(this.mContext, 1362, packageName, Pair.create(Integer.valueOf(833), Integer.valueOf(metricsKey)));
            } else {
                Iterator it = appInfo.anomalyTypes.iterator();
                while (it.hasNext()) {
                    int type = ((Integer) it.next()).intValue();
                    this.mMetricsFeatureProvider.action(this.mContext, 1362, packageName, Pair.create(Integer.valueOf(i2), Integer.valueOf(metricsKey)), Pair.create(Integer.valueOf(1366), Integer.valueOf(type)));
                    i2 = 833;
                }
            }
        }
        this.mBatteryDatabaseManager.updateAnomalies(appInfos, 1);
    }
}
