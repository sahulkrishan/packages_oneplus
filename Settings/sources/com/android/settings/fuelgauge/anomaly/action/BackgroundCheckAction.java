package com.android.settings.fuelgauge.anomaly.action;

import android.app.AppOpsManager;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.PointerIconCompat;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.fuelgauge.anomaly.Anomaly;

public class BackgroundCheckAction extends AnomalyAction {
    private AppOpsManager mAppOpsManager;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;

    public BackgroundCheckAction(Context context) {
        super(context);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mActionMetricKey = PointerIconCompat.TYPE_GRAB;
        this.mBatteryUtils = BatteryUtils.getInstance(context);
    }

    public void handlePositiveAction(Anomaly anomaly, int contextMetricsKey) {
        super.handlePositiveAction(anomaly, contextMetricsKey);
        if (anomaly.targetSdkVersion < 26) {
            this.mAppOpsManager.setMode(64, anomaly.uid, anomaly.packageName, 1);
        }
    }

    public boolean isActionActive(Anomaly anomaly) {
        return this.mBatteryUtils.isBackgroundRestrictionEnabled(anomaly.targetSdkVersion, anomaly.uid, anomaly.packageName) ^ 1;
    }

    public int getActionType() {
        return 1;
    }
}
