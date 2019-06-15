package com.android.settings.fuelgauge.batterytip;

import android.app.StatsManager;
import android.app.StatsManager.StatsUnavailableException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AnomalyConfigReceiver extends BroadcastReceiver {
    private static final String TAG = "AnomalyConfigReceiver";

    public void onReceive(Context context, Intent intent) {
        if ("android.app.action.STATSD_STARTED".equals(intent.getAction()) || "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            StatsManager statsManager = (StatsManager) context.getSystemService(StatsManager.class);
            AnomalyConfigJobService.scheduleConfigUpdate(context);
            try {
                BatteryTipUtils.uploadAnomalyPendingIntent(context, statsManager);
            } catch (StatsUnavailableException e) {
                Log.w(TAG, "Failed to uploadAnomalyPendingIntent.", e);
            }
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                AnomalyCleanupJobService.scheduleCleanUp(context);
            }
        }
    }
}
