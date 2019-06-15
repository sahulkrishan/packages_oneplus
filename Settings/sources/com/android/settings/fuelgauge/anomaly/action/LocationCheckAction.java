package com.android.settings.fuelgauge.anomaly.action;

import android.content.Context;
import android.content.pm.permission.RuntimePermissionPresenter;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.PointerIconCompat;
import com.android.settings.fuelgauge.anomaly.Anomaly;

public class LocationCheckAction extends AnomalyAction {
    private static final String TAG = "LocationCheckAction";
    private final RuntimePermissionPresenter mRuntimePermissionPresenter;

    public LocationCheckAction(Context context) {
        this(context, RuntimePermissionPresenter.getInstance(context));
    }

    @VisibleForTesting
    LocationCheckAction(Context context, RuntimePermissionPresenter runtimePermissionPresenter) {
        super(context);
        this.mRuntimePermissionPresenter = runtimePermissionPresenter;
        this.mActionMetricKey = PointerIconCompat.TYPE_GRABBING;
    }

    public void handlePositiveAction(Anomaly anomaly, int contextMetricsKey) {
        super.handlePositiveAction(anomaly, contextMetricsKey);
        this.mRuntimePermissionPresenter.revokeRuntimePermission(anomaly.packageName, "android.permission.ACCESS_COARSE_LOCATION");
        this.mRuntimePermissionPresenter.revokeRuntimePermission(anomaly.packageName, "android.permission.ACCESS_FINE_LOCATION");
    }

    public boolean isActionActive(Anomaly anomaly) {
        return isPermissionGranted(anomaly, "android.permission.ACCESS_COARSE_LOCATION") || isPermissionGranted(anomaly, "android.permission.ACCESS_FINE_LOCATION");
    }

    public int getActionType() {
        return 2;
    }

    private boolean isPermissionGranted(Anomaly anomaly, String permission) {
        return PermissionChecker.checkPermission(this.mContext, permission, -1, anomaly.uid, anomaly.packageName) == 0;
    }
}
