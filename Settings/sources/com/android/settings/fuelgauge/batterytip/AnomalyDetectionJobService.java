package com.android.settings.fuelgauge.batterytip;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.StatsDimensionsValue;
import android.os.UserManager;
import android.support.annotation.GuardedBy;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.fuelgauge.PowerUsageFeatureProvider;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.fuelgauge.PowerWhitelistBackend;
import com.android.settingslib.utils.ThreadUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AnomalyDetectionJobService extends JobService {
    @VisibleForTesting
    static final long MAX_DELAY_MS = TimeUnit.MINUTES.toMillis(30);
    private static final int ON = 1;
    @VisibleForTesting
    static final int STATSD_UID_FILED = 1;
    private static final String TAG = "AnomalyDetectionService";
    @VisibleForTesting
    static final int UID_NULL = -1;
    @VisibleForTesting
    @GuardedBy("mLock")
    boolean mIsJobCanceled = false;
    private final Object mLock = new Object();

    public static void scheduleAnomalyDetection(Context context, Intent intent) {
        if (((JobScheduler) context.getSystemService(JobScheduler.class)).enqueue(new Builder(R.integer.job_anomaly_detection, new ComponentName(context, AnomalyDetectionJobService.class)).setOverrideDeadline(MAX_DELAY_MS).build(), new JobWorkItem(intent)) != 1) {
            Log.i(TAG, "Anomaly detection job service enqueue failed.");
        }
    }

    public boolean onStartJob(JobParameters params) {
        synchronized (this.mLock) {
            this.mIsJobCanceled = false;
        }
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$AnomalyDetectionJobService$7JxJe3rza0cCkIc77iCS-ZKPfL4(this, params));
        return true;
    }

    public static /* synthetic */ void lambda$onStartJob$0(AnomalyDetectionJobService anomalyDetectionJobService, JobParameters params) {
        Context context = anomalyDetectionJobService;
        Log.d(TAG, "postOnBackgroundThread onStartJob start");
        Context context2 = context;
        BatteryDatabaseManager batteryDatabaseManager = BatteryDatabaseManager.getInstance(anomalyDetectionJobService);
        BatteryTipPolicy policy = new BatteryTipPolicy(context);
        BatteryUtils batteryUtils = BatteryUtils.getInstance(anomalyDetectionJobService);
        ContentResolver contentResolver = anomalyDetectionJobService.getContentResolver();
        UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
        PowerWhitelistBackend powerWhitelistBackend = PowerWhitelistBackend.getInstance(context2);
        PowerUsageFeatureProvider powerUsageFeatureProvider = FeatureFactory.getFactory(anomalyDetectionJobService).getPowerUsageFeatureProvider(context);
        MetricsFeatureProvider metricsFeatureProvider = FeatureFactory.getFactory(anomalyDetectionJobService).getMetricsFeatureProvider();
        JobWorkItem item = anomalyDetectionJobService.dequeueWork(params);
        while (true) {
            JobWorkItem item2 = item;
            Context context3;
            if (item2 != null) {
                context3 = context2;
                JobWorkItem item3 = item2;
                context.saveAnomalyToDatabase(context2, userManager, batteryDatabaseManager, batteryUtils, policy, powerWhitelistBackend, contentResolver, powerUsageFeatureProvider, metricsFeatureProvider, item2.getIntent().getExtras());
                context.completeWork(params, item3);
                item = anomalyDetectionJobService.dequeueWork(params);
                context2 = context3;
            } else {
                JobParameters jobParameters = params;
                context3 = context2;
                Log.d(TAG, "postOnBackgroundThread onStartJob end");
                return;
            }
        }
    }

    public boolean onStopJob(JobParameters jobParameters) {
        synchronized (this.mLock) {
            this.mIsJobCanceled = true;
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0095 A:{SYNTHETIC, Splitter:B:25:0x0095} */
    /* JADX WARNING: Missing block: B:11:0x0066, code skipped:
            if (android.provider.Settings.Global.getInt(r3, "adaptive_battery_management_enabled", 1) == 1) goto L_0x0068;
     */
    @android.support.annotation.VisibleForTesting
    public void saveAnomalyToDatabase(android.content.Context r25, android.os.UserManager r26, com.android.settings.fuelgauge.batterytip.BatteryDatabaseManager r27, com.android.settings.fuelgauge.BatteryUtils r28, com.android.settings.fuelgauge.batterytip.BatteryTipPolicy r29, com.android.settingslib.fuelgauge.PowerWhitelistBackend r30, android.content.ContentResolver r31, com.android.settings.fuelgauge.PowerUsageFeatureProvider r32, com.android.settingslib.core.instrumentation.MetricsFeatureProvider r33, android.os.Bundle r34) {
        /*
        r24 = this;
        r1 = r25;
        r2 = r28;
        r3 = r31;
        r4 = r33;
        r5 = r34;
        r0 = "android.app.extra.STATS_DIMENSIONS_VALUE";
        r0 = r5.getParcelable(r0);
        r6 = r0;
        r6 = (android.os.StatsDimensionsValue) r6;
        r0 = "key_anomaly_timestamp";
        r7 = java.lang.System.currentTimeMillis();
        r7 = r5.getLong(r0, r7);
        r0 = "android.app.extra.STATS_BROADCAST_SUBSCRIBER_COOKIES";
        r14 = r5.getStringArrayList(r0);
        r0 = new com.android.settings.fuelgauge.batterytip.AnomalyInfo;
        r9 = com.android.internal.util.ArrayUtils.isEmpty(r14);
        r15 = 0;
        if (r9 != 0) goto L_0x0033;
    L_0x002c:
        r9 = r14.get(r15);
        r9 = (java.lang.String) r9;
        goto L_0x0035;
    L_0x0033:
        r9 = "";
    L_0x0035:
        r0.<init>(r9);
        r13 = r0;
        r0 = "AnomalyDetectionService";
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "Extra stats value: ";
        r9.append(r10);
        r10 = r6.toString();
        r9.append(r10);
        r9 = r9.toString();
        android.util.Log.i(r0, r9);
        r12 = r24;
        r0 = r12.extractUidFromStatsDimensionsValue(r6);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0146, IndexOutOfBoundsException | NullPointerException -> 0x0146 }
        r9 = r32.isSmartBatterySupported();	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0146, IndexOutOfBoundsException | NullPointerException -> 0x0146 }
        r11 = 1;
        if (r9 == 0) goto L_0x0075;
    L_0x0060:
        r9 = "adaptive_battery_management_enabled";
        r9 = android.provider.Settings.Global.getInt(r3, r9, r11);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x006d, IndexOutOfBoundsException | NullPointerException -> 0x006d }
        if (r9 != r11) goto L_0x006a;
    L_0x0068:
        r9 = r11;
        goto L_0x007f;
    L_0x006b:
        r9 = r15;
        goto L_0x007f;
    L_0x006d:
        r0 = move-exception;
        r21 = r6;
    L_0x0070:
        r3 = r13;
        r17 = r14;
        goto L_0x014c;
    L_0x0075:
        r9 = "app_auto_restriction_enabled";
        r9 = android.provider.Settings.Global.getInt(r3, r9, r11);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0146, IndexOutOfBoundsException | NullPointerException -> 0x0146 }
        if (r9 != r11) goto L_0x007e;
    L_0x007d:
        goto L_0x0068;
    L_0x007e:
        goto L_0x006b;
    L_0x007f:
        r16 = r9;
        r9 = r2.getPackageName(r0);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0146, IndexOutOfBoundsException | NullPointerException -> 0x0146 }
        r10 = r9;
        r17 = r2.getAppLongVersionCode(r10);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0146, IndexOutOfBoundsException | NullPointerException -> 0x0146 }
        r19 = r17;
        r9 = r30;
        r17 = r2.shouldHideAnomaly(r9, r0, r13);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0146, IndexOutOfBoundsException | NullPointerException -> 0x0146 }
        r11 = 2;
        if (r17 == 0) goto L_0x00cf;
    L_0x0095:
        r11 = new android.util.Pair[r11];	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c7, IndexOutOfBoundsException | NullPointerException -> 0x00c7 }
        r15 = 833; // 0x341 float:1.167E-42 double:4.116E-321;
        r15 = java.lang.Integer.valueOf(r15);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c7, IndexOutOfBoundsException | NullPointerException -> 0x00c7 }
        r3 = r13.anomalyType;	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c7, IndexOutOfBoundsException | NullPointerException -> 0x00c7 }
        r3 = android.util.Pair.create(r15, r3);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c7, IndexOutOfBoundsException | NullPointerException -> 0x00c7 }
        r15 = 0;
        r11[r15] = r3;	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c7, IndexOutOfBoundsException | NullPointerException -> 0x00c7 }
        r3 = 1389; // 0x56d float:1.946E-42 double:6.863E-321;
        r3 = java.lang.Integer.valueOf(r3);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c7, IndexOutOfBoundsException | NullPointerException -> 0x00c7 }
        r21 = r6;
        r5 = r19;
        r15 = java.lang.Long.valueOf(r5);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c5, IndexOutOfBoundsException | NullPointerException -> 0x00c5 }
        r3 = android.util.Pair.create(r3, r15);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c5, IndexOutOfBoundsException | NullPointerException -> 0x00c5 }
        r15 = 1;
        r11[r15] = r3;	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c5, IndexOutOfBoundsException | NullPointerException -> 0x00c5 }
        r3 = 1387; // 0x56b float:1.944E-42 double:6.853E-321;
        r4.action(r1, r3, r10, r11);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00c5, IndexOutOfBoundsException | NullPointerException -> 0x00c5 }
        r3 = r13;
        r17 = r14;
        goto L_0x0143;
    L_0x00c5:
        r0 = move-exception;
        goto L_0x0070;
    L_0x00c7:
        r0 = move-exception;
        r21 = r6;
        r3 = r13;
        r17 = r14;
        goto L_0x014c;
    L_0x00cf:
        r21 = r6;
        r5 = r19;
        r3 = 1389; // 0x56d float:1.946E-42 double:6.863E-321;
        if (r16 == 0) goto L_0x0102;
    L_0x00d7:
        r3 = r13.autoRestriction;	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00fd, IndexOutOfBoundsException | NullPointerException -> 0x00fd }
        if (r3 == 0) goto L_0x0102;
    L_0x00db:
        r3 = 1;
        r2.setForceAppStandby(r0, r10, r3);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00fd, IndexOutOfBoundsException | NullPointerException -> 0x00fd }
        r3 = r13.anomalyType;	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00fd, IndexOutOfBoundsException | NullPointerException -> 0x00fd }
        r3 = r3.intValue();	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x00fd, IndexOutOfBoundsException | NullPointerException -> 0x00fd }
        r17 = 2;
        r9 = r27;
        r22 = r10;
        r10 = r0;
        r18 = 1;
        r11 = r22;
        r12 = r3;
        r3 = r13;
        r13 = r17;
        r17 = r14;
        r19 = r15;
        r14 = r7;
        r9.insertAnomaly(r10, r11, r12, r13, r14);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        goto L_0x011b;
    L_0x00fd:
        r0 = move-exception;
        r3 = r13;
        r17 = r14;
        goto L_0x014c;
    L_0x0102:
        r22 = r10;
        r3 = r13;
        r17 = r14;
        r19 = r15;
        r18 = 1;
        r9 = r3.anomalyType;	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r12 = r9.intValue();	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r13 = 0;
        r9 = r27;
        r10 = r0;
        r11 = r22;
        r14 = r7;
        r9.insertAnomaly(r10, r11, r12, r13, r14);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
    L_0x011b:
        r9 = 1367; // 0x557 float:1.916E-42 double:6.754E-321;
        r10 = 2;
        r10 = new android.util.Pair[r10];	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r11 = 1366; // 0x556 float:1.914E-42 double:6.75E-321;
        r11 = java.lang.Integer.valueOf(r11);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r12 = r3.anomalyType;	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r11 = android.util.Pair.create(r11, r12);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r10[r19] = r11;	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r11 = 1389; // 0x56d float:1.946E-42 double:6.863E-321;
        r11 = java.lang.Integer.valueOf(r11);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r12 = java.lang.Long.valueOf(r5);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r11 = android.util.Pair.create(r11, r12);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r10[r18] = r11;	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
        r11 = r22;
        r4.action(r1, r9, r11, r10);	 Catch:{ IndexOutOfBoundsException | NullPointerException -> 0x0144, IndexOutOfBoundsException | NullPointerException -> 0x0144 }
    L_0x0143:
        goto L_0x0153;
    L_0x0144:
        r0 = move-exception;
        goto L_0x014c;
    L_0x0146:
        r0 = move-exception;
        r21 = r6;
        r3 = r13;
        r17 = r14;
    L_0x014c:
        r5 = "AnomalyDetectionService";
        r6 = "Parse stats dimensions value error.";
        android.util.Log.e(r5, r6, r0);
    L_0x0153:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fuelgauge.batterytip.AnomalyDetectionJobService.saveAnomalyToDatabase(android.content.Context, android.os.UserManager, com.android.settings.fuelgauge.batterytip.BatteryDatabaseManager, com.android.settings.fuelgauge.BatteryUtils, com.android.settings.fuelgauge.batterytip.BatteryTipPolicy, com.android.settingslib.fuelgauge.PowerWhitelistBackend, android.content.ContentResolver, com.android.settings.fuelgauge.PowerUsageFeatureProvider, com.android.settingslib.core.instrumentation.MetricsFeatureProvider, android.os.Bundle):void");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int extractUidFromStatsDimensionsValue(StatsDimensionsValue statsDimensionsValue) {
        if (statsDimensionsValue == null) {
            return -1;
        }
        if (statsDimensionsValue.isValueType(3) && statsDimensionsValue.getField() == 1) {
            return statsDimensionsValue.getIntValue();
        }
        if (statsDimensionsValue.isValueType(7)) {
            List<StatsDimensionsValue> values = statsDimensionsValue.getTupleValueList();
            int size = values.size();
            for (int i = 0; i < size; i++) {
                int uid = extractUidFromStatsDimensionsValue((StatsDimensionsValue) values.get(i));
                if (uid != -1) {
                    return uid;
                }
            }
        }
        return -1;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public JobWorkItem dequeueWork(JobParameters parameters) {
        synchronized (this.mLock) {
            if (this.mIsJobCanceled) {
                return null;
            }
            JobWorkItem dequeueWork = parameters.dequeueWork();
            return dequeueWork;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void completeWork(JobParameters parameters, JobWorkItem item) {
        synchronized (this.mLock) {
            if (this.mIsJobCanceled) {
                return;
            }
            parameters.completeWork(item);
        }
    }
}
