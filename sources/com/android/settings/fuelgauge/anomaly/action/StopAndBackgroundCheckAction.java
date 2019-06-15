package com.android.settings.fuelgauge.anomaly.action;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import com.android.settings.fuelgauge.anomaly.Anomaly;

public class StopAndBackgroundCheckAction extends AnomalyAction {
    @VisibleForTesting
    BackgroundCheckAction mBackgroundCheckAction;
    @VisibleForTesting
    ForceStopAction mForceStopAction;

    public StopAndBackgroundCheckAction(Context context) {
        this(context, new ForceStopAction(context), new BackgroundCheckAction(context));
        this.mActionMetricKey = 1233;
    }

    @VisibleForTesting
    StopAndBackgroundCheckAction(Context context, ForceStopAction forceStopAction, BackgroundCheckAction backgroundCheckAction) {
        super(context);
        this.mForceStopAction = forceStopAction;
        this.mBackgroundCheckAction = backgroundCheckAction;
    }

    public void handlePositiveAction(Anomaly anomaly, int metricsKey) {
        super.handlePositiveAction(anomaly, metricsKey);
        this.mForceStopAction.handlePositiveAction(anomaly, metricsKey);
        this.mBackgroundCheckAction.handlePositiveAction(anomaly, metricsKey);
    }

    public boolean isActionActive(Anomaly anomaly) {
        return this.mForceStopAction.isActionActive(anomaly) && this.mBackgroundCheckAction.isActionActive(anomaly);
    }

    public int getActionType() {
        return 3;
    }
}
