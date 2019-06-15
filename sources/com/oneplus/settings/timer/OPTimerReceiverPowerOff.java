package com.oneplus.settings.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.oneplus.settings.timer.timepower.OPPowerOffPromptActivity;
import com.oneplus.settings.timer.timepower.SettingsUtil;
import com.oneplus.settings.utils.OPUtils;
import java.lang.reflect.Array;
import java.util.Calendar;

public class OPTimerReceiverPowerOff extends BroadcastReceiver {
    private static final String ACTION_CANCEL_POWEROFF_ALARM = "org.codeaurora.poweroffalarm.action.CANCEL_ALARM";
    private static final String ACTION_SET_POWEROFF_ALARM = "org.codeaurora.poweroffalarm.action.SET_ALARM";
    private static final String POWER_OFF_ALARM_PACKAGE = "com.qualcomm.qti.poweroffalarm";
    private static final String TIME = "time";
    private WakeLock mLock = null;
    private PowerManager pm = null;

    static boolean intToBool(int i) {
        return i != 0;
    }

    public void onReceive(Context context, Intent intent) {
        Intent powerOFFIntent;
        Context context2 = context;
        String action = intent.getAction();
        long[] nearestTime = new long[2];
        nearestTime = SettingsUtil.getNearestTime(System.getString(context.getContentResolver(), "def_timepower_config"));
        this.pm = (PowerManager) context2.getSystemService("power");
        if (action.equals("com.android.settings.action.REQUEST_POWER_OFF") || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action) || "android.intent.action.BOOT_COMPLETED".equals(action)) {
            if (isPowerOffEnable(context)) {
                powerOFFIntent = new Intent(SettingsUtil.ACTION_POWER_OP_OFF);
                powerOFFIntent.setFlags(285212672);
                if (nearestTime[1] != 0) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(nearestTime[0]);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Power on alarm with flag set:");
                    stringBuilder.append(c.getTime().toString());
                    Log.d("boot", stringBuilder.toString());
                    ((AlarmManager) context2.getSystemService(NotificationCompat.CATEGORY_ALARM)).setExact(0, nearestTime[1], PendingIntent.getBroadcast(context2, 0, powerOFFIntent, 134217728));
                }
            }
        } else if (action.equals(SettingsUtil.ACTION_POWER_OP_OFF)) {
            long wrongTime = ((System.currentTimeMillis() - nearestTime[1]) - SettingsUtil.MILLIS_OF_DAY) % SettingsUtil.MILLIS_OF_DAY;
            if ((wrongTime < 0 || wrongTime <= 60000) && (wrongTime >= 0 || wrongTime <= -86340000)) {
                powerOFFIntent = new Intent(context2, OPPowerOffPromptActivity.class);
                powerOFFIntent.setFlags(268435456);
                context2.startActivity(powerOFFIntent);
            } else {
                return;
            }
        } else if (action.equals(SettingsUtil.ACTION_POWER_CONFIRM_OP_OFF)) {
            if (this.mLock != null) {
                this.mLock.release();
                this.mLock = null;
            }
            this.mLock = this.pm.newWakeLock(268435466, "TimepowerWakeLock");
            this.mLock.acquire();
            powerOFFIntent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
            powerOFFIntent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            powerOFFIntent.setFlags(268435456);
            context2.startActivity(powerOFFIntent);
        } else if (action.equals(SettingsUtil.ACTION_POWER_CANCEL_OP_OFF)) {
            powerOFFIntent = new Intent(SettingsUtil.ACTION_POWER_OP_OFF);
            powerOFFIntent.setFlags(285212672);
            ((AlarmManager) context2.getSystemService(NotificationCompat.CATEGORY_ALARM)).cancel(PendingIntent.getBroadcast(context2, 0, powerOFFIntent, 134217728));
        }
        if (action.equals(SettingsUtil.ACTION_POWER_OP_ON) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action) || "android.intent.action.BOOT_COMPLETED".equals(action)) {
            if (OPUtils.isSupportNewPlanPowerOffAlarm()) {
                powerOFFIntent = new Intent(SettingsUtil.ACTION_POWER_OP_ON);
                powerOFFIntent.setFlags(285212672);
                AlarmManager am = (AlarmManager) context2.getSystemService(NotificationCompat.CATEGORY_ALARM);
                PendingIntent sender = PendingIntent.getBroadcast(context2, 0, powerOFFIntent, 134217728);
                if (isPowerOnEnable(context)) {
                    cancleNewPlanLastPowerOn(context);
                    am.setExact(0, nearestTime[0], sender);
                    Intent powerOffIntent = new Intent(ACTION_SET_POWEROFF_ALARM);
                    powerOffIntent.addFlags(285212672);
                    powerOffIntent.setPackage(POWER_OFF_ALARM_PACKAGE);
                    powerOffIntent.putExtra(TIME, nearestTime[0]);
                    context2.sendBroadcast(powerOffIntent);
                } else {
                    am.cancel(sender);
                }
            } else {
                powerOFFIntent = new Intent(SettingsUtil.ACTION_POWER_OP_ON);
                powerOFFIntent.setFlags(285212672);
                AlarmManager am2 = (AlarmManager) context2.getSystemService(NotificationCompat.CATEGORY_ALARM);
                PendingIntent sender2 = PendingIntent.getBroadcast(context2, 1, powerOFFIntent, 0);
                if (isPowerOnEnable(context)) {
                    am2.setExact(5, nearestTime[0], sender2);
                } else {
                    am2.cancel(sender2);
                }
            }
        }
    }

    private void cancleNewPlanLastPowerOn(Context context) {
        long[] nearestTime = new long[2];
        nearestTime = SettingsUtil.getNearestTime(System.getString(context.getContentResolver(), "def_timepower_config"));
        Intent powerOffIntent = new Intent(ACTION_CANCEL_POWEROFF_ALARM);
        powerOffIntent.addFlags(285212672);
        powerOffIntent.putExtra(TIME, nearestTime[0]);
        powerOffIntent.setPackage(POWER_OFF_ALARM_PACKAGE);
        context.sendBroadcast(powerOffIntent);
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
}
