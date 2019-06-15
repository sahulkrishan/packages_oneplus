package com.android.settings.fuelgauge.batterytip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AnomalyDetectionReceiver extends BroadcastReceiver {
    public static final String KEY_ANOMALY_TIMESTAMP = "key_anomaly_timestamp";
    private static final String TAG = "SettingsAnomalyReceiver";

    public void onReceive(Context context, Intent intent) {
        long configUid = intent.getLongExtra("android.app.extra.STATS_CONFIG_UID", -1);
        long configKey = intent.getLongExtra("android.app.extra.STATS_CONFIG_KEY", -1);
        long subscriptionId = intent.getLongExtra("android.app.extra.STATS_SUBSCRIPTION_ID", -1);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Anomaly intent received.  configUid = ");
        stringBuilder.append(configUid);
        stringBuilder.append(" configKey = ");
        stringBuilder.append(configKey);
        stringBuilder.append(" subscriptionId = ");
        stringBuilder.append(subscriptionId);
        Log.i(str, stringBuilder.toString());
        intent.getExtras().putLong(KEY_ANOMALY_TIMESTAMP, System.currentTimeMillis());
        AnomalyDetectionJobService.scheduleAnomalyDetection(context, intent);
    }
}
