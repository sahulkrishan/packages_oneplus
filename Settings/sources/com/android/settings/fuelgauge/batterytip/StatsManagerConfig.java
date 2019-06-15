package com.android.settings.fuelgauge.batterytip;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StatsManagerConfig {
    public static final long ANOMALY_CONFIG_KEY = 1;
    public static final long SUBSCRIBER_ID = 1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AnomalyType {
        public static final int BACKGROUND_ANR = 20;
        public static final int BACKGROUND_CRASH_RATE = 21;
        public static final int EXCESSIVE_ANRS = 23;
        public static final int EXCESSIVE_ANR_LOOPING = 22;
        public static final int EXCESSIVE_BACKGROUND_SERVICE = 4;
        public static final int EXCESSIVE_BACKGROUND_SYNCS = 13;
        public static final int EXCESSIVE_CRASH_LOOPING = 25;
        public static final int EXCESSIVE_CRASH_RATE = 24;
        public static final int EXCESSIVE_DAVEY_RATE = 8;
        public static final int EXCESSIVE_FLASH_WRITES = 6;
        public static final int EXCESSIVE_GPS_SCANS_IN_BACKGROUND = 14;
        public static final int EXCESSIVE_JANKY_FRAMES = 9;
        public static final int EXCESSIVE_JOB_SCHEDULING = 15;
        public static final int EXCESSIVE_MEMORY_IN_BACKGROUND = 7;
        public static final int EXCESSIVE_MOBILE_NETWORK_IN_BACKGROUND = 16;
        public static final int EXCESSIVE_UNOPTIMIZED_BLE_SCAN = 3;
        public static final int EXCESSIVE_WAKELOCK_ALL_SCREEN_OFF = 1;
        public static final int EXCESSIVE_WAKEUPS_IN_BACKGROUND = 2;
        public static final int EXCESSIVE_WIFI_LOCK_TIME = 17;
        public static final int EXCESSIVE_WIFI_SCAN = 5;
        public static final int JOB_TIMED_OUT = 18;
        public static final int LONG_UNOPTIMIZED_BLE_SCAN = 19;
        public static final int NULL = -1;
        public static final int NUMBER_OF_OPEN_FILES = 26;
        public static final int SLOW_COLD_START_TIME = 10;
        public static final int SLOW_HOT_START_TIME = 11;
        public static final int SLOW_WARM_START_TIME = 12;
        public static final int UNKNOWN_REASON = 0;
    }
}
