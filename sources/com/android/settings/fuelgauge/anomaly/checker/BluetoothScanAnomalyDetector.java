package com.android.settings.fuelgauge.anomaly.checker;

import android.content.Context;
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

public class BluetoothScanAnomalyDetector implements AnomalyDetector {
    private static final String TAG = "BluetoothScanAnomalyDetector";
    private AnomalyUtils mAnomalyUtils;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private long mBluetoothScanningThreshold;
    private Context mContext;

    public BluetoothScanAnomalyDetector(Context context) {
        this(context, new AnomalyDetectionPolicy(context), AnomalyUtils.getInstance(context));
    }

    @VisibleForTesting
    BluetoothScanAnomalyDetector(Context context, AnomalyDetectionPolicy policy, AnomalyUtils anomalyUtils) {
        this.mContext = context;
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        this.mBluetoothScanningThreshold = policy.bluetoothScanThreshold;
        this.mAnomalyUtils = anomalyUtils;
    }

    public List<Anomaly> detectAnomalies(BatteryStatsHelper batteryStatsHelper) {
        return detectAnomalies(batteryStatsHelper, null);
    }

    public List<Anomaly> detectAnomalies(BatteryStatsHelper batteryStatsHelper, String targetPackageName) {
        List<BatterySipper> batterySippers = batteryStatsHelper.getUsageList();
        List<Anomaly> anomalies = new ArrayList();
        int targetUid = this.mBatteryUtils.getPackageUid(targetPackageName);
        long elapsedRealtimeMs = SystemClock.elapsedRealtime();
        int i = 0;
        int size = batterySippers.size();
        while (i < size) {
            List<BatterySipper> batterySippers2;
            BatterySipper sipper = (BatterySipper) batterySippers.get(i);
            Uid uid = sipper.uidObj;
            if (!(uid == null || this.mBatteryUtils.shouldHideSipper(sipper))) {
                if (targetUid == -1 || targetUid == uid.getUid()) {
                    long bluetoothTimeMs = getBluetoothUnoptimizedBgTimeMs(uid, elapsedRealtimeMs);
                    if (bluetoothTimeMs > this.mBluetoothScanningThreshold) {
                        String packageName = this.mBatteryUtils.getPackageName(uid.getUid());
                        batterySippers2 = batterySippers;
                        batterySippers = new Builder().setUid(uid.getUid()).setType(2).setDisplayName(Utils.getApplicationLabel(this.mContext, packageName)).setPackageName(packageName).setBluetoothScanningTimeMs(bluetoothTimeMs).build();
                        if (this.mAnomalyUtils.getAnomalyAction(batterySippers).isActionActive(batterySippers)) {
                            anomalies.add(batterySippers);
                        }
                        i++;
                        batterySippers = batterySippers2;
                    }
                } else {
                    batterySippers2 = batterySippers;
                    i++;
                    batterySippers = batterySippers2;
                }
            }
            batterySippers2 = batterySippers;
            i++;
            batterySippers = batterySippers2;
        }
        return anomalies;
    }

    @VisibleForTesting
    public long getBluetoothUnoptimizedBgTimeMs(Uid uid, long elapsedRealtimeMs) {
        Timer timer = uid.getBluetoothUnoptimizedScanBackgroundTimer();
        return timer != null ? timer.getTotalDurationMsLocked(elapsedRealtimeMs) : 0;
    }
}
