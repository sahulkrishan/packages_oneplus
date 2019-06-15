package com.oneplus.settings.timer.timepower;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class SettingsUtil {
    public static final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
    public static final String ACTION_POWER_CANCEL_OP_OFF = "com.android.settings.POWER_CANCEL_OP_OFF";
    public static final String ACTION_POWER_CONFIRM_OP_OFF = "com.android.settings.POWER_CONFIRM_OP_OFF";
    public static final String ACTION_POWER_OFF = "com.android.settings.POWER_OFF";
    public static final String ACTION_POWER_ON = "com.android.settings.POWER_ON";
    public static final String ACTION_POWER_OP_OFF = "com.android.settings.POWER_OP_OFF";
    public static final String ACTION_POWER_OP_ON = "com.android.settings.POWER_OP_ON";
    public static final String ACTION_SET_CHANGED = "com.android.settings.SET_CHANGED";
    public static final long MILLIS_OF_DAY = 86400000;
    public static final int REQUEST_CODE_OFF = 1;
    public static final int REQUEST_CODE_ON = 0;
    private static final int RTC_POWERUP_MTK = 7;
    private static final int RTC_POWERUP_QCOM = 5;
    public static final String TAG = "SettingsUtil";
    public static final int TIMEOUT_MILLIS = 60000;
    private static final int TIME_POWER_COUNTS = 2;
    public static final String TRIGGER_TIME = "trigger_time";
    private static long mCurrentTime;

    public static long[] getNearestTime(String data) {
        mCurrentTime = System.currentTimeMillis();
        long[] timeArray = new long[]{0, 0};
        if (data == null) {
            return timeArray;
        }
        ArrayList<Long> list_poweron = new ArrayList();
        ArrayList<Long> list_poweroff = new ArrayList();
        for (int i = 1; i <= 2; i++) {
            if (1 == i) {
                list_poweron.add(Long.valueOf(getUTC(Integer.parseInt(data.substring((i * 6) - 6, (i * 6) - 4)), Integer.parseInt(data.substring((i * 6) - 4, (i * 6) - 2)))));
            } else if (2 == i) {
                list_poweroff.add(Long.valueOf(getUTC(Integer.parseInt(data.substring((i * 6) - 6, (i * 6) - 4)), Integer.parseInt(data.substring((i * 6) - 4, (i * 6) - 2)))));
            }
        }
        if (list_poweron.size() != 0) {
            list_poweron.add(Long.valueOf(mCurrentTime));
            Collections.sort(list_poweron);
            if (((Long) list_poweron.get(list_poweron.size() - 1)).longValue() == mCurrentTime) {
                timeArray[0] = ((Long) list_poweron.get(0)).longValue() + MILLIS_OF_DAY;
            } else {
                timeArray[0] = ((Long) list_poweron.get(list_poweron.lastIndexOf(Long.valueOf(mCurrentTime)) + 1)).longValue();
            }
        }
        if (list_poweroff.size() != 0) {
            list_poweroff.add(Long.valueOf(mCurrentTime));
            Collections.sort(list_poweroff);
            if (((Long) list_poweroff.get(list_poweroff.size() - 1)).longValue() == mCurrentTime) {
                timeArray[1] = ((Long) list_poweroff.get(0)).longValue() + MILLIS_OF_DAY;
            } else {
                timeArray[1] = ((Long) list_poweroff.get(list_poweroff.lastIndexOf(Long.valueOf(mCurrentTime)) + 1)).longValue();
            }
        }
        return timeArray;
    }

    private static long getUTC(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mCurrentTime);
        calendar.set(11, hourOfDay);
        calendar.set(12, minute);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTimeInMillis();
    }

    public static void setAlarm(Context context, Intent intent, long TriggerAtTime, int requestCode) {
        AlarmManager am = (AlarmManager) context.getSystemService(NotificationCompat.CATEGORY_ALARM);
        PendingIntent sender = PendingIntent.getBroadcast(context, requestCode, intent, 134217728);
        switch (requestCode) {
            case 0:
                am.setExact(5, TriggerAtTime, sender);
                return;
            case 1:
                am.setExact(0, TriggerAtTime, sender);
                return;
            default:
                return;
        }
    }

    public static void cancelAlarm(Context context, Intent intent, int requestCode) {
        ((AlarmManager) context.getSystemService(NotificationCompat.CATEGORY_ALARM)).cancel(PendingIntent.getBroadcast(context, requestCode, intent, 134217728));
    }

    public static boolean isPowerOnEnable(Context context) {
        return checkSwitch(context, true);
    }

    public static boolean isPowerOffEnable(Context context) {
        return checkSwitch(context, false);
    }

    public static boolean checkSwitch(Context context, boolean powerOnOrPowerOff) {
        String config = System.getString(context.getContentResolver(), "def_timepower_config");
        if (config == null) {
            return false;
        }
        int[][] mTimeArray = (int[][]) Array.newInstance(int.class, new int[]{2, 2});
        boolean[][] mStateArray = (boolean[][]) Array.newInstance(boolean.class, new int[]{2, 2});
        int i = 0;
        int j = 0;
        while (i <= 6) {
            String tmp = config.substring(i, i + 6);
            mTimeArray[j][0] = Integer.parseInt(tmp.substring(0, 2));
            mTimeArray[j][1] = Integer.parseInt(tmp.substring(2, 4));
            mStateArray[j][0] = intToBool(Integer.parseInt(tmp.substring(4, 5)));
            mStateArray[j][1] = intToBool(Integer.parseInt(tmp.substring(5, 6)));
            i += 6;
            j++;
        }
        if (powerOnOrPowerOff) {
            if (mStateArray[0][1]) {
                return true;
            }
        } else if (mStateArray[1][1]) {
            return true;
        }
        return false;
    }

    static boolean intToBool(int i) {
        return i != 0;
    }
}
