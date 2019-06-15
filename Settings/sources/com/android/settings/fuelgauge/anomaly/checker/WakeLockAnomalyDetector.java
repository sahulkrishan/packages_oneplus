package com.android.settings.fuelgauge.anomaly.checker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BatteryStats.Timer;
import android.os.BatteryStats.Uid;
import android.os.SystemClock;
import android.support.annotation.VisibleForTesting;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.Utils;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.fuelgauge.anomaly.Anomaly;
import com.android.settings.fuelgauge.anomaly.Anomaly.Builder;
import com.android.settings.fuelgauge.anomaly.AnomalyDetectionPolicy;
import com.android.settings.fuelgauge.anomaly.AnomalyUtils;
import java.util.ArrayList;
import java.util.List;

public class WakeLockAnomalyDetector implements AnomalyDetector {
    private static final String TAG = "WakeLockAnomalyChecker";
    private AnomalyUtils mAnomalyUtils;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private Context mContext;
    private PackageManager mPackageManager;
    @VisibleForTesting
    long mWakeLockThresholdMs;

    public WakeLockAnomalyDetector(Context context) {
        this(context, new AnomalyDetectionPolicy(context), AnomalyUtils.getInstance(context));
    }

    @VisibleForTesting
    WakeLockAnomalyDetector(Context context, AnomalyDetectionPolicy policy, AnomalyUtils anomalyUtils) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        this.mAnomalyUtils = anomalyUtils;
        this.mWakeLockThresholdMs = policy.wakeLockThreshold;
    }

    public List<Anomaly> detectAnomalies(BatteryStatsHelper batteryStatsHelper) {
        return detectAnomalies(batteryStatsHelper, null);
    }

    public List<Anomaly> detectAnomalies(BatteryStatsHelper batteryStatsHelper, String targetPackageName) {
        long rawRealtime;
        int targetUid;
        List<BatterySipper> batterySippers = batteryStatsHelper.getUsageList();
        List<Anomaly> anomalies = new ArrayList();
        long rawRealtime2 = SystemClock.elapsedRealtime();
        int targetUid2 = this.mBatteryUtils.getPackageUid(targetPackageName);
        int i = 0;
        int size = batterySippers.size();
        while (i < size) {
            List<BatterySipper> batterySippers2;
            BatterySipper sipper = (BatterySipper) batterySippers.get(i);
            Uid uid = sipper.uidObj;
            if (uid == null || this.mBatteryUtils.shouldHideSipper(sipper)) {
                batterySippers2 = batterySippers;
                rawRealtime = rawRealtime2;
                targetUid = targetUid2;
            } else if (targetUid2 == -1 || targetUid2 == uid.getUid()) {
                long currentDurationMs = getCurrentDurationMs(uid, rawRealtime2);
                long backgroundDurationMs = getBackgroundTotalDurationMs(uid, rawRealtime2);
                rawRealtime = rawRealtime2;
                if (backgroundDurationMs <= this.mWakeLockThresholdMs || currentDurationMs == 0) {
                    batterySippers2 = batterySippers;
                    targetUid = targetUid2;
                } else {
                    rawRealtime2 = this.mBatteryUtils.getPackageName(uid.getUid());
                    batterySippers2 = batterySippers;
                    targetUid = targetUid2;
                    batterySippers = new Builder().setUid(uid.getUid()).setType(0).setDisplayName(Utils.getApplicationLabel(this.mContext, rawRealtime2)).setPackageName(rawRealtime2).setWakeLockTimeMs(backgroundDurationMs).build();
                    if (this.mAnomalyUtils.getAnomalyAction(batterySippers).isActionActive(batterySippers)) {
                        anomalies.add(batterySippers);
                    }
                }
            } else {
                batterySippers2 = batterySippers;
                rawRealtime = rawRealtime2;
                targetUid = targetUid2;
            }
            i++;
            rawRealtime2 = rawRealtime;
            batterySippers = batterySippers2;
            targetUid2 = targetUid;
        }
        rawRealtime = rawRealtime2;
        targetUid = targetUid2;
        return anomalies;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public long getCurrentDurationMs(Uid uid, long elapsedRealtimeMs) {
        Timer timer = uid.getAggregatedPartialWakelockTimer();
        return timer != null ? timer.getCurrentDurationMsLocked(elapsedRealtimeMs) : 0;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public long getBackgroundTotalDurationMs(Uid uid, long elapsedRealtimeMs) {
        Timer timer = uid.getAggregatedPartialWakelockTimer();
        Timer subTimer = timer != null ? timer.getSubTimer() : null;
        return subTimer != null ? subTimer.getTotalDurationMsLocked(elapsedRealtimeMs) : 0;
    }
}
