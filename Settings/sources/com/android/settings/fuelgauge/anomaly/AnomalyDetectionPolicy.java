package com.android.settings.fuelgauge.anomaly;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.util.KeyValueListParser;
import android.util.Log;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AnomalyDetectionPolicy {
    @VisibleForTesting
    static final String KEY_ANOMALY_DETECTION_ENABLED = "anomaly_detection_enabled";
    @VisibleForTesting
    static final String KEY_BLUETOOTH_SCAN_DETECTION_ENABLED = "bluetooth_scan_enabled";
    @VisibleForTesting
    static final String KEY_BLUETOOTH_SCAN_THRESHOLD = "bluetooth_scan_threshold";
    @VisibleForTesting
    static final String KEY_WAKELOCK_DETECTION_ENABLED = "wakelock_enabled";
    @VisibleForTesting
    static final String KEY_WAKELOCK_THRESHOLD = "wakelock_threshold";
    @VisibleForTesting
    static final String KEY_WAKEUP_ALARM_DETECTION_ENABLED = "wakeup_alarm_enabled";
    @VisibleForTesting
    static final String KEY_WAKEUP_ALARM_THRESHOLD = "wakeup_alarm_threshold";
    @VisibleForTesting
    static final String KEY_WAKEUP_BLACKLISTED_TAGS = "wakeup_blacklisted_tags";
    public static final String TAG = "AnomalyDetectionPolicy";
    final boolean anomalyDetectionEnabled;
    final boolean bluetoothScanDetectionEnabled;
    public final long bluetoothScanThreshold;
    private final KeyValueListParser mParser = new KeyValueListParser(',');
    final boolean wakeLockDetectionEnabled;
    public final long wakeLockThreshold;
    final boolean wakeupAlarmDetectionEnabled;
    public final long wakeupAlarmThreshold;
    public final Set<String> wakeupBlacklistedTags;

    public AnomalyDetectionPolicy(Context context) {
        try {
            this.mParser.setString(Global.getString(context.getContentResolver(), "anomaly_detection_constants"));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Bad anomaly detection constants");
        }
        this.anomalyDetectionEnabled = this.mParser.getBoolean(KEY_ANOMALY_DETECTION_ENABLED, false);
        this.wakeLockDetectionEnabled = this.mParser.getBoolean(KEY_WAKELOCK_DETECTION_ENABLED, false);
        this.wakeupAlarmDetectionEnabled = this.mParser.getBoolean(KEY_WAKEUP_ALARM_DETECTION_ENABLED, false);
        this.bluetoothScanDetectionEnabled = this.mParser.getBoolean(KEY_BLUETOOTH_SCAN_DETECTION_ENABLED, false);
        this.wakeLockThreshold = this.mParser.getLong(KEY_WAKELOCK_THRESHOLD, 3600000);
        this.wakeupAlarmThreshold = this.mParser.getLong(KEY_WAKEUP_ALARM_THRESHOLD, 10);
        this.wakeupBlacklistedTags = parseStringSet(KEY_WAKEUP_BLACKLISTED_TAGS, null);
        this.bluetoothScanThreshold = this.mParser.getLong(KEY_BLUETOOTH_SCAN_THRESHOLD, 1800000);
    }

    public boolean isAnomalyDetectionEnabled() {
        return this.anomalyDetectionEnabled;
    }

    public boolean isAnomalyDetectorEnabled(int type) {
        switch (type) {
            case 0:
                return this.wakeLockDetectionEnabled;
            case 1:
                return this.wakeupAlarmDetectionEnabled;
            case 2:
                return this.bluetoothScanDetectionEnabled;
            default:
                return false;
        }
    }

    private Set<String> parseStringSet(String key, Set<String> defaultSet) {
        String value = this.mParser.getString(key, null);
        if (value != null) {
            return (Set) Arrays.stream(value.split(":")).map(-$$Lambda$AnomalyDetectionPolicy$MGZTkxm_LWhWFo0-u65o5bz97bA.INSTANCE).map(-$$Lambda$AnomalyDetectionPolicy$xFZhNZfuK_aveGITeM1VIXBhSVQ.INSTANCE).collect(Collectors.toSet());
        }
        return defaultSet;
    }
}
