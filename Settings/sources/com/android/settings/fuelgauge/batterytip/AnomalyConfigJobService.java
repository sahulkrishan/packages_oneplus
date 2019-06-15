package com.android.settings.fuelgauge.batterytip;

import android.app.StatsManager;
import android.app.StatsManager.StatsUnavailableException;
import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.utils.ThreadUtils;
import java.util.concurrent.TimeUnit;

public class AnomalyConfigJobService extends JobService {
    @VisibleForTesting
    static final long CONFIG_UPDATE_FREQUENCY_MS = TimeUnit.DAYS.toMillis(1);
    private static final int DEFAULT_VERSION = 0;
    public static final String KEY_ANOMALY_CONFIG_VERSION = "anomaly_config_version";
    public static final String PREF_DB = "anomaly_pref";
    private static final String TAG = "AnomalyConfigJobService";

    public static void scheduleConfigUpdate(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JobScheduler.class);
        Builder jobBuilder = new Builder(R.integer.job_anomaly_config_update, new ComponentName(context, AnomalyConfigJobService.class)).setPeriodic(CONFIG_UPDATE_FREQUENCY_MS).setRequiresDeviceIdle(true).setRequiresCharging(true).setPersisted(true);
        if (jobScheduler.getPendingJob(R.integer.job_anomaly_config_update) == null && jobScheduler.schedule(jobBuilder.build()) != 1) {
            Log.i(TAG, "Anomaly config update job service schedule failed.");
        }
    }

    public boolean onStartJob(JobParameters params) {
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$AnomalyConfigJobService$ABo24-XwFDn4e3D3k2rc6z-5bdU(this, params));
        return true;
    }

    public static /* synthetic */ void lambda$onStartJob$0(AnomalyConfigJobService anomalyConfigJobService, JobParameters params) {
        Log.d(TAG, "postOnBackgroundThread checkAnomalyConfig start");
        StatsManager statsManager = (StatsManager) anomalyConfigJobService.getSystemService(StatsManager.class);
        anomalyConfigJobService.checkAnomalyConfig(statsManager);
        try {
            BatteryTipUtils.uploadAnomalyPendingIntent(anomalyConfigJobService, statsManager);
        } catch (StatsUnavailableException e) {
            Log.w(TAG, "Failed to uploadAnomalyPendingIntent.", e);
        }
        anomalyConfigJobService.jobFinished(params, false);
        Log.d(TAG, "postOnBackgroundThread checkAnomalyConfig end");
    }

    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    /* Access modifiers changed, original: declared_synchronized */
    @VisibleForTesting
    public synchronized void checkAnomalyConfig(StatsManager statsManager) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_DB, 0);
        int currentVersion = sharedPreferences.getInt(KEY_ANOMALY_CONFIG_VERSION, 0);
        int newVersion = Global.getInt(getContentResolver(), KEY_ANOMALY_CONFIG_VERSION, 0);
        String rawConfig = Global.getString(getContentResolver(), "anomaly_config");
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CurrentVersion: ");
        stringBuilder.append(currentVersion);
        stringBuilder.append(" new version: ");
        stringBuilder.append(newVersion);
        Log.i(str, stringBuilder.toString());
        if (newVersion > currentVersion) {
            try {
                statsManager.removeConfig(1);
            } catch (StatsUnavailableException e) {
                Log.i(TAG, "When updating anomaly config, failed to first remove the old config 1", e);
            }
            if (!TextUtils.isEmpty(rawConfig)) {
                try {
                    statsManager.addConfig(1, Base64.decode(rawConfig, 0));
                    Log.i(TAG, "Upload the anomaly config. configKey: 1");
                    Editor editor = sharedPreferences.edit();
                    editor.putInt(KEY_ANOMALY_CONFIG_VERSION, newVersion);
                    editor.commit();
                } catch (IllegalArgumentException e2) {
                    Log.e(TAG, "Anomaly raw config is in wrong format", e2);
                } catch (StatsUnavailableException e3) {
                    Log.i(TAG, "Upload of anomaly config failed for configKey 1", e3);
                }
            }
        }
    }
}
