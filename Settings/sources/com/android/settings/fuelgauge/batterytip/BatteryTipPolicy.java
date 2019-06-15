package com.android.settings.fuelgauge.batterytip;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.util.KeyValueListParser;
import android.util.Log;
import java.time.Duration;

public class BatteryTipPolicy {
    private static final String KEY_APP_RESTRICTION_ENABLED = "app_restriction_enabled";
    private static final String KEY_BATTERY_SAVER_TIP_ENABLED = "battery_saver_tip_enabled";
    private static final String KEY_BATTERY_TIP_ENABLED = "battery_tip_enabled";
    private static final String KEY_DATA_HISTORY_RETAIN_DAY = "data_history_retain_day";
    private static final String KEY_EXCESSIVE_BG_DRAIN_PERCENTAGE = "excessive_bg_drain_percentage";
    private static final String KEY_HIGH_USAGE_APP_COUNT = "high_usage_app_count";
    private static final String KEY_HIGH_USAGE_BATTERY_DRAINING = "high_usage_battery_draining";
    private static final String KEY_HIGH_USAGE_ENABLED = "high_usage_enabled";
    private static final String KEY_HIGH_USAGE_PERIOD_MS = "high_usage_period_ms";
    private static final String KEY_LOW_BATTERY_ENABLED = "low_battery_enabled";
    private static final String KEY_LOW_BATTERY_HOUR = "low_battery_hour";
    private static final String KEY_REDUCED_BATTERY_ENABLED = "reduced_battery_enabled";
    private static final String KEY_REDUCED_BATTERY_PERCENT = "reduced_battery_percent";
    private static final String KEY_SUMMARY_ENABLED = "summary_enabled";
    private static final String KEY_TEST_BATTERY_SAVER_TIP = "test_battery_saver_tip";
    private static final String KEY_TEST_HIGH_USAGE_TIP = "test_high_usage_tip";
    private static final String KEY_TEST_LOW_BATTERY_TIP = "test_low_battery_tip";
    private static final String KEY_TEST_SMART_BATTERY_TIP = "test_smart_battery_tip";
    public static final String TAG = "BatteryTipPolicy";
    public final boolean appRestrictionEnabled;
    public final boolean batterySaverTipEnabled;
    public final boolean batteryTipEnabled;
    public final int dataHistoryRetainDay;
    public final int excessiveBgDrainPercentage;
    public final int highUsageAppCount;
    public final int highUsageBatteryDraining;
    public final boolean highUsageEnabled;
    public final long highUsagePeriodMs;
    public final boolean lowBatteryEnabled;
    public final int lowBatteryHour;
    private final KeyValueListParser mParser;
    public final boolean reducedBatteryEnabled;
    public final int reducedBatteryPercent;
    public final boolean summaryEnabled;
    public final boolean testBatterySaverTip;
    public final boolean testHighUsageTip;
    public final boolean testLowBatteryTip;
    public final boolean testSmartBatteryTip;

    public BatteryTipPolicy(Context context) {
        this(context, new KeyValueListParser(','));
    }

    @VisibleForTesting
    BatteryTipPolicy(Context context, KeyValueListParser parser) {
        this.mParser = parser;
        try {
            this.mParser.setString(Global.getString(context.getContentResolver(), "battery_tip_constants"));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Bad battery tip constants");
        }
        this.batteryTipEnabled = this.mParser.getBoolean(KEY_BATTERY_TIP_ENABLED, true);
        this.summaryEnabled = this.mParser.getBoolean(KEY_SUMMARY_ENABLED, true);
        this.batterySaverTipEnabled = this.mParser.getBoolean(KEY_BATTERY_SAVER_TIP_ENABLED, true);
        this.highUsageEnabled = this.mParser.getBoolean(KEY_HIGH_USAGE_ENABLED, true);
        this.highUsageAppCount = this.mParser.getInt(KEY_HIGH_USAGE_APP_COUNT, 3);
        this.highUsagePeriodMs = this.mParser.getLong(KEY_HIGH_USAGE_PERIOD_MS, Duration.ofHours(2).toMillis());
        this.highUsageBatteryDraining = this.mParser.getInt(KEY_HIGH_USAGE_BATTERY_DRAINING, 25);
        this.appRestrictionEnabled = this.mParser.getBoolean(KEY_APP_RESTRICTION_ENABLED, true);
        this.reducedBatteryEnabled = this.mParser.getBoolean(KEY_REDUCED_BATTERY_ENABLED, false);
        this.reducedBatteryPercent = this.mParser.getInt(KEY_REDUCED_BATTERY_PERCENT, 50);
        this.lowBatteryEnabled = this.mParser.getBoolean(KEY_LOW_BATTERY_ENABLED, true);
        this.lowBatteryHour = this.mParser.getInt(KEY_LOW_BATTERY_HOUR, 3);
        this.dataHistoryRetainDay = this.mParser.getInt(KEY_DATA_HISTORY_RETAIN_DAY, 30);
        this.excessiveBgDrainPercentage = this.mParser.getInt(KEY_EXCESSIVE_BG_DRAIN_PERCENTAGE, 10);
        this.testBatterySaverTip = this.mParser.getBoolean(KEY_TEST_BATTERY_SAVER_TIP, false);
        this.testHighUsageTip = this.mParser.getBoolean(KEY_TEST_HIGH_USAGE_TIP, false);
        this.testSmartBatteryTip = this.mParser.getBoolean(KEY_TEST_SMART_BATTERY_TIP, false);
        this.testLowBatteryTip = this.mParser.getBoolean(KEY_TEST_LOW_BATTERY_TIP, false);
    }
}
