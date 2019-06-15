package com.android.settings.fuelgauge.anomaly;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Pair;
import android.util.SparseIntArray;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.fuelgauge.anomaly.action.AnomalyAction;
import com.android.settings.fuelgauge.anomaly.action.ForceStopAction;
import com.android.settings.fuelgauge.anomaly.action.LocationCheckAction;
import com.android.settings.fuelgauge.anomaly.action.StopAndBackgroundCheckAction;
import com.android.settings.fuelgauge.anomaly.checker.AnomalyDetector;
import com.android.settings.fuelgauge.anomaly.checker.BluetoothScanAnomalyDetector;
import com.android.settings.fuelgauge.anomaly.checker.WakeLockAnomalyDetector;
import com.android.settings.fuelgauge.anomaly.checker.WakeupAlarmAnomalyDetector;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import java.util.ArrayList;
import java.util.List;

public class AnomalyUtils {
    private static final SparseIntArray mMetricArray = new SparseIntArray();
    private static AnomalyUtils sInstance;
    private Context mContext;

    static {
        mMetricArray.append(0, 1235);
        mMetricArray.append(1, 1236);
        mMetricArray.append(2, 1237);
    }

    @VisibleForTesting
    AnomalyUtils(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static AnomalyUtils getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AnomalyUtils(context);
        }
        return sInstance;
    }

    public AnomalyAction getAnomalyAction(Anomaly anomaly) {
        switch (anomaly.type) {
            case 0:
                return new ForceStopAction(this.mContext);
            case 1:
                if (anomaly.targetSdkVersion >= 26 || (anomaly.targetSdkVersion < 26 && anomaly.backgroundRestrictionEnabled)) {
                    return new ForceStopAction(this.mContext);
                }
                return new StopAndBackgroundCheckAction(this.mContext);
            case 2:
                return new LocationCheckAction(this.mContext);
            default:
                return null;
        }
    }

    public AnomalyDetector getAnomalyDetector(int anomalyType) {
        switch (anomalyType) {
            case 0:
                return new WakeLockAnomalyDetector(this.mContext);
            case 1:
                return new WakeupAlarmAnomalyDetector(this.mContext);
            case 2:
                return new BluetoothScanAnomalyDetector(this.mContext);
            default:
                return null;
        }
    }

    public List<Anomaly> detectAnomalies(BatteryStatsHelper batteryStatsHelper, AnomalyDetectionPolicy policy, String targetPackageName) {
        List<Anomaly> anomalies = new ArrayList();
        for (int type : Anomaly.ANOMALY_TYPE_LIST) {
            if (policy.isAnomalyDetectorEnabled(type)) {
                anomalies.addAll(getAnomalyDetector(type).detectAnomalies(batteryStatsHelper, targetPackageName));
            }
        }
        return anomalies;
    }

    public void logAnomalies(MetricsFeatureProvider provider, List<Anomaly> anomalies, int contextId) {
        int size = anomalies.size();
        for (int i = 0; i < size; i++) {
            logAnomaly(provider, (Anomaly) anomalies.get(i), contextId);
        }
    }

    public void logAnomaly(MetricsFeatureProvider provider, Anomaly anomaly, int contextId) {
        provider.action(this.mContext, mMetricArray.get(anomaly.type, 0), anomaly.packageName, Pair.create(Integer.valueOf(833), Integer.valueOf(contextId)), Pair.create(Integer.valueOf(1234), Integer.valueOf(getAnomalyAction(anomaly).getActionType())));
    }
}
