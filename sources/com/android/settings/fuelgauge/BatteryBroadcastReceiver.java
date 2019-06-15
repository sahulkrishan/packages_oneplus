package com.android.settings.fuelgauge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.VisibleForTesting;
import com.android.settings.Utils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    @VisibleForTesting
    String mBatteryLevel;
    private OnBatteryChangedListener mBatteryListener;
    @VisibleForTesting
    String mBatteryStatus;
    private Context mContext;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BatteryUpdateType {
        public static final int BATTERY_LEVEL = 1;
        public static final int BATTERY_SAVER = 2;
        public static final int BATTERY_STATUS = 3;
        public static final int MANUAL = 0;
    }

    public interface OnBatteryChangedListener {
        void onBatteryChanged(int i);
    }

    public BatteryBroadcastReceiver(Context context) {
        this.mContext = context;
    }

    public void onReceive(Context context, Intent intent) {
        updateBatteryStatus(intent, false);
    }

    public void setBatteryChangedListener(OnBatteryChangedListener lsn) {
        this.mBatteryListener = lsn;
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        updateBatteryStatus(this.mContext.registerReceiver(this, intentFilter), true);
    }

    public void unRegister() {
        this.mContext.unregisterReceiver(this);
    }

    private void updateBatteryStatus(Intent intent, boolean forceUpdate) {
        if (intent != null && this.mBatteryListener != null) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                String batteryLevel = Utils.getBatteryPercentage(intent);
                String batteryStatus = com.android.settingslib.Utils.getBatteryStatus(this.mContext.getResources(), intent);
                if (forceUpdate) {
                    this.mBatteryListener.onBatteryChanged(0);
                } else if (!batteryLevel.equals(this.mBatteryLevel)) {
                    this.mBatteryListener.onBatteryChanged(1);
                } else if (!batteryStatus.equals(this.mBatteryStatus)) {
                    this.mBatteryListener.onBatteryChanged(3);
                }
                this.mBatteryLevel = batteryLevel;
                this.mBatteryStatus = batteryStatus;
            } else if ("android.os.action.POWER_SAVE_MODE_CHANGED".equals(intent.getAction())) {
                this.mBatteryListener.onBatteryChanged(2);
            }
        }
    }
}
