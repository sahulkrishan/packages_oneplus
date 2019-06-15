package com.oneplus.settings.timer.timepower;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;

public class ReceiverAfterBoot extends BroadcastReceiver {
    private static final boolean DEBUG = false;
    private static final String KEY_INTERNAL_SDCARD_STATE = "persist.sys.oppo.iSdCardState";
    private static final String TAG = "ReceiverAfterBoot";

    public void onReceive(Context context, Intent intent) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("==========ReceiverAfterBoot :[");
        stringBuilder.append(intent.getAction());
        stringBuilder.append("] =====");
        Log.i("BOOTCOMPLETED", stringBuilder.toString());
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.i("BOOTCOMPLETED", "==========ReceiverAfterBoot : ACTION_BOOT_COMPLETED=====");
            writeUsingThemeFlag(context, false);
            statisticsPowerOnTimes(context);
            setInternalSdState(context);
        }
        long[] nearestTime = new long[2];
        if (SystemProperties.getInt("persist.sys.quick.test.mode", 0) != 4) {
            nearestTime = SettingsUtil.getNearestTime(System.getString(context.getContentResolver(), "def_timepower_config"));
        } else {
            long CurrentTime = System.currentTimeMillis();
            nearestTime[1] = 60000 + CurrentTime;
            nearestTime[0] = 120000 + CurrentTime;
            SharedPreferences sp = context.getSharedPreferences("sp_count", 0);
            int count = sp.getInt("count", 1);
            sp.edit().putInt("count", count + 1).commit();
            String str = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("-------Total test times:");
            stringBuilder2.append(count);
            stringBuilder2.append("--------");
            Log.d(str, stringBuilder2.toString());
        }
        Intent powerOnIntent = new Intent(SettingsUtil.ACTION_POWER_ON);
        if (nearestTime[0] != 0) {
            SettingsUtil.setAlarm(context, powerOnIntent, nearestTime[0], 0);
        } else {
            context.getPackageManager().hasSystemFeature("oppo.hw.manufacturer.mtk");
            SettingsUtil.cancelAlarm(context, powerOnIntent, 0);
        }
        if (!"android.fpd.boot_completed".equals(intent.getAction())) {
            Intent powerOffIntent = new Intent(SettingsUtil.ACTION_POWER_OFF);
            if (nearestTime[1] != 0) {
                Bundle bundle = new Bundle();
                bundle.putLong(SettingsUtil.TRIGGER_TIME, nearestTime[1]);
                powerOffIntent.putExtras(bundle);
                SettingsUtil.setAlarm(context, powerOffIntent, nearestTime[1], 1);
            } else {
                SettingsUtil.cancelAlarm(context, powerOffIntent, 1);
            }
        }
        if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            "android.intent.action.TIME_SET".equals(intent.getAction());
        }
    }

    private void statisticsPowerOnTimes(Context context) {
        System.putInt(context.getContentResolver(), "oem_pwoer_on_times", System.getInt(context.getContentResolver(), "oem_pwoer_on_times", 0) + 1);
    }

    private void writeUsingThemeFlag(Context context, boolean isUsingTheme) {
        if (System.getInt(context.getContentResolver(), "oem_is_using_theme", 0) != 0) {
            System.putInt(context.getContentResolver(), "oem_is_using_theme", isUsingTheme);
        }
    }

    private void setInternalSdState(Context context) {
        String state = System.getString(context.getContentResolver(), KEY_INTERNAL_SDCARD_STATE);
        if (state != null && !state.isEmpty() && !"mounted".equals(state)) {
            System.putString(context.getContentResolver(), KEY_INTERNAL_SDCARD_STATE, "mounted");
        }
    }
}
