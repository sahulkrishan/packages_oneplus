package com.android.settings.fuelgauge.batterytip.actions;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Pair;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settings.fuelgauge.batterytip.tips.UnrestrictAppTip;

public class UnrestrictAppAction extends BatteryTipAction {
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private UnrestrictAppTip mUnRestrictAppTip;

    public UnrestrictAppAction(Context context, UnrestrictAppTip tip) {
        super(context);
        this.mUnRestrictAppTip = tip;
        this.mBatteryUtils = BatteryUtils.getInstance(context);
    }

    public void handlePositiveAction(int metricsKey) {
        AppInfo appInfo = this.mUnRestrictAppTip.getUnrestrictAppInfo();
        this.mBatteryUtils.setForceAppStandby(appInfo.uid, appInfo.packageName, 0);
        this.mMetricsFeatureProvider.action(this.mContext, 1363, appInfo.packageName, Pair.create(Integer.valueOf(833), Integer.valueOf(metricsKey)));
    }
}
