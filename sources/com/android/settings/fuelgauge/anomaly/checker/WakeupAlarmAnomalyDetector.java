package com.android.settings.fuelgauge.anomaly.checker;

import android.content.Context;
import android.os.BatteryStats.Counter;
import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Pkg;
import android.support.annotation.VisibleForTesting;
import android.util.ArrayMap;
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
import java.util.Map.Entry;
import java.util.Set;

public class WakeupAlarmAnomalyDetector implements AnomalyDetector {
    private static final String TAG = "WakeupAlarmAnomalyDetector";
    private AnomalyUtils mAnomalyUtils;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private Context mContext;
    private long mWakeupAlarmThreshold;
    private Set<String> mWakeupBlacklistedTags;

    public WakeupAlarmAnomalyDetector(Context context) {
        this(context, new AnomalyDetectionPolicy(context), AnomalyUtils.getInstance(context));
    }

    @VisibleForTesting
    WakeupAlarmAnomalyDetector(Context context, AnomalyDetectionPolicy policy, AnomalyUtils anomalyUtils) {
        this.mContext = context;
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        this.mAnomalyUtils = anomalyUtils;
        this.mWakeupAlarmThreshold = policy.wakeupAlarmThreshold;
        this.mWakeupBlacklistedTags = policy.wakeupBlacklistedTags;
    }

    public List<Anomaly> detectAnomalies(BatteryStatsHelper batteryStatsHelper) {
        return detectAnomalies(batteryStatsHelper, null);
    }

    public List<Anomaly> detectAnomalies(BatteryStatsHelper batteryStatsHelper, String targetPackageName) {
        double totalRunningHours;
        List<BatterySipper> batterySippers = batteryStatsHelper.getUsageList();
        List<Anomaly> anomalies = new ArrayList();
        double totalRunningHours2 = ((double) this.mBatteryUtils.calculateRunningTimeBasedOnStatsType(batteryStatsHelper, 0)) / 3600000.0d;
        int targetUid = this.mBatteryUtils.getPackageUid(targetPackageName);
        if (totalRunningHours2 >= 1.0d) {
            int i = 0;
            int size = batterySippers.size();
            while (i < size) {
                List<BatterySipper> batterySippers2;
                BatterySipper sipper = (BatterySipper) batterySippers.get(i);
                Uid uid = sipper.uidObj;
                if (uid == null || this.mBatteryUtils.shouldHideSipper(sipper)) {
                    batterySippers2 = batterySippers;
                    totalRunningHours = totalRunningHours2;
                } else if (targetUid == -1 || targetUid == uid.getUid()) {
                    int wakeupAlarmCount = (int) (((double) getWakeupAlarmCountFromUid(uid)) / totalRunningHours2);
                    totalRunningHours = totalRunningHours2;
                    if (((long) wakeupAlarmCount) > this.mWakeupAlarmThreshold) {
                        totalRunningHours2 = this.mBatteryUtils.getPackageName(uid.getUid());
                        CharSequence displayName = Utils.getApplicationLabel(this.mContext, totalRunningHours2);
                        int targetSdkVersion = this.mBatteryUtils.getTargetSdkVersion(totalRunningHours2);
                        batterySippers2 = batterySippers;
                        batterySippers = new Builder().setUid(uid.getUid()).setType(1).setDisplayName(displayName).setPackageName(totalRunningHours2).setTargetSdkVersion(targetSdkVersion).setBackgroundRestrictionEnabled(this.mBatteryUtils.isBackgroundRestrictionEnabled(targetSdkVersion, uid.getUid(), totalRunningHours2)).setWakeupAlarmCount(wakeupAlarmCount).build();
                        if (this.mAnomalyUtils.getAnomalyAction(batterySippers).isActionActive(batterySippers)) {
                            anomalies.add(batterySippers);
                        }
                    } else {
                        batterySippers2 = batterySippers;
                    }
                } else {
                    batterySippers2 = batterySippers;
                    totalRunningHours = totalRunningHours2;
                }
                i++;
                totalRunningHours2 = totalRunningHours;
                batterySippers = batterySippers2;
            }
        }
        totalRunningHours = totalRunningHours2;
        return anomalies;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getWakeupAlarmCountFromUid(Uid uid) {
        int wakeups = 0;
        ArrayMap<String, ? extends Pkg> packageStats = uid.getPackageStats();
        for (int ipkg = packageStats.size() - 1; ipkg >= 0; ipkg--) {
            for (Entry<String, ? extends Counter> alarm : ((Pkg) packageStats.valueAt(ipkg)).getWakeupAlarmStats().entrySet()) {
                if (this.mWakeupBlacklistedTags == null || !this.mWakeupBlacklistedTags.contains(alarm.getKey())) {
                    wakeups += ((Counter) alarm.getValue()).getCountLocked(0);
                }
            }
        }
        return wakeups;
    }
}
