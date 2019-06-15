package com.android.settings;

import android.util.EventLog;

public class EventLogTags {
    public static final int EXP_DET_DEVICE_ADMIN_ACTIVATED_BY_USER = 90201;
    public static final int EXP_DET_DEVICE_ADMIN_DECLINED_BY_USER = 90202;
    public static final int EXP_DET_DEVICE_ADMIN_UNINSTALLED_BY_USER = 90203;
    public static final int LOCK_SCREEN_TYPE = 90200;
    public static final int SETTINGS_LATENCY = 90204;

    private EventLogTags() {
    }

    public static void writeLockScreenType(String type) {
        EventLog.writeEvent(LOCK_SCREEN_TYPE, type);
    }

    public static void writeExpDetDeviceAdminActivatedByUser(String appSignature) {
        EventLog.writeEvent(EXP_DET_DEVICE_ADMIN_ACTIVATED_BY_USER, appSignature);
    }

    public static void writeExpDetDeviceAdminDeclinedByUser(String appSignature) {
        EventLog.writeEvent(EXP_DET_DEVICE_ADMIN_DECLINED_BY_USER, appSignature);
    }

    public static void writeExpDetDeviceAdminUninstalledByUser(String appSignature) {
        EventLog.writeEvent(EXP_DET_DEVICE_ADMIN_UNINSTALLED_BY_USER, appSignature);
    }

    public static void writeSettingsLatency(int action, int latency) {
        EventLog.writeEvent(SETTINGS_LATENCY, new Object[]{Integer.valueOf(action), Integer.valueOf(latency)});
    }
}
