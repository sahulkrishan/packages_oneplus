package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.VisibleForTesting;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;

public class AlwaysDiscoverable extends BroadcastReceiver {
    private static final String TAG = "AlwaysDiscoverable";
    private Context mContext;
    private IntentFilter mIntentFilter = new IntentFilter();
    private LocalBluetoothAdapter mLocalAdapter;
    @VisibleForTesting
    boolean mStarted;

    public AlwaysDiscoverable(Context context, LocalBluetoothAdapter localAdapter) {
        this.mContext = context;
        this.mLocalAdapter = localAdapter;
        this.mIntentFilter.addAction("android.bluetooth.adapter.action.SCAN_MODE_CHANGED");
    }

    public void start() {
        if (!this.mStarted) {
            this.mContext.registerReceiver(this, this.mIntentFilter);
            this.mStarted = true;
        }
    }

    public void stop() {
        if (this.mStarted) {
            this.mContext.unregisterReceiver(this);
            this.mStarted = false;
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == "android.bluetooth.adapter.action.SCAN_MODE_CHANGED") {
        }
    }
}
