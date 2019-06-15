package com.android.settings.fuelgauge.batterytip;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.utils.ThreadUtils;
import java.util.concurrent.TimeUnit;

public class AnomalyCleanupJobService extends JobService {
    @VisibleForTesting
    static final long CLEAN_UP_FREQUENCY_MS = TimeUnit.DAYS.toMillis(1);
    private static final String TAG = "AnomalyCleanUpJobService";

    public static void scheduleCleanUp(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JobScheduler.class);
        Builder jobBuilder = new Builder(R.integer.job_anomaly_clean_up, new ComponentName(context, AnomalyCleanupJobService.class)).setPeriodic(CLEAN_UP_FREQUENCY_MS).setRequiresDeviceIdle(true).setRequiresCharging(true).setPersisted(true);
        if (jobScheduler.getPendingJob(R.integer.job_anomaly_clean_up) == null && jobScheduler.schedule(jobBuilder.build()) != 1) {
            Log.i(TAG, "Anomaly clean up job service schedule failed.");
        }
    }

    public boolean onStartJob(JobParameters params) {
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$AnomalyCleanupJobService$Wvu3W97OjsnNVurAIkZXTma9fMg(this, BatteryDatabaseManager.getInstance(this), new BatteryTipPolicy(this), params));
        return true;
    }

    public static /* synthetic */ void lambda$onStartJob$0(AnomalyCleanupJobService anomalyCleanupJobService, BatteryDatabaseManager batteryDatabaseManager, BatteryTipPolicy policy, JobParameters params) {
        Log.d(TAG, "postOnBackgroundThread deleteAllAnomalies start");
        batteryDatabaseManager.deleteAllAnomaliesBeforeTimeStamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis((long) policy.dataHistoryRetainDay));
        anomalyCleanupJobService.jobFinished(params, false);
        Log.d(TAG, "postOnBackgroundThread deleteAllAnomalies end");
    }

    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
