package com.android.settings.fuelgauge.anomaly.action;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import com.android.settings.fuelgauge.anomaly.Anomaly;

public class ForceStopAction extends AnomalyAction {
    private static final String TAG = "ForceStopAction";
    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;

    public ForceStopAction(Context context) {
        super(context);
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        this.mPackageManager = context.getPackageManager();
        this.mActionMetricKey = 807;
    }

    public void handlePositiveAction(Anomaly anomaly, int contextMetricsKey) {
        super.handlePositiveAction(anomaly, contextMetricsKey);
        this.mActivityManager.forceStopPackage(anomaly.packageName);
    }

    public boolean isActionActive(Anomaly anomaly) {
        boolean z = false;
        try {
            if ((this.mPackageManager.getApplicationInfo(anomaly.packageName, 128).flags & 2097152) == 0) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot find info for app: ");
            stringBuilder.append(anomaly.packageName);
            Log.e(str, stringBuilder.toString());
            return false;
        }
    }

    public int getActionType() {
        return 0;
    }
}
