package com.android.settingslib.bluetooth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class BluetoothDiscoverableTimeoutReceiver extends BroadcastReceiver {
    private static final String INTENT_DISCOVERABLE_TIMEOUT = "android.bluetooth.intent.DISCOVERABLE_TIMEOUT";
    private static final String TAG = "BluetoothDiscoverableTimeoutReceiver";

    public static void setDiscoverableAlarm(Context context, long alarmTime) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setDiscoverableAlarm(): alarmTime = ");
        stringBuilder.append(alarmTime);
        Log.d(str, stringBuilder.toString());
        Intent intent = new Intent(INTENT_DISCOVERABLE_TIMEOUT);
        intent.setClass(context, BluetoothDiscoverableTimeoutReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(NotificationCompat.CATEGORY_ALARM);
        if (pending != null) {
            alarmManager.cancel(pending);
            Log.d(TAG, "setDiscoverableAlarm(): cancel prev alarm");
        }
        alarmManager.set(0, alarmTime, PendingIntent.getBroadcast(context, 0, intent, 0));
    }

    public static void cancelDiscoverableAlarm(Context context) {
        Log.d(TAG, "cancelDiscoverableAlarm(): Enter");
        Intent intent = new Intent(INTENT_DISCOVERABLE_TIMEOUT);
        intent.setClass(context, BluetoothDiscoverableTimeoutReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, null, intent, 536870912);
        if (pending != null) {
            ((AlarmManager) context.getSystemService(NotificationCompat.CATEGORY_ALARM)).cancel(pending);
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(INTENT_DISCOVERABLE_TIMEOUT)) {
            LocalBluetoothAdapter localBluetoothAdapter = LocalBluetoothAdapter.getInstance();
            if (localBluetoothAdapter == null || localBluetoothAdapter.getState() != 12) {
                Log.e(TAG, "localBluetoothAdapter is NULL!!");
            } else {
                Log.d(TAG, "Disable discoverable...");
                localBluetoothAdapter.setScanMode(21);
            }
        }
    }
}
