package com.android.settings.connecteddevice;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.oneplus.settings.utils.OPUtils;

public class AddDevicePreferenceController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop {
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private IntentFilter mIntentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
    private Preference mPreference;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            AddDevicePreferenceController.this.updateState();
        }
    };

    public AddDevicePreferenceController(Context context, String key) {
        super(context, key);
    }

    public void onStart() {
        if (this.mBluetoothAdapter != null) {
            setBluetoothDiscoverableState();
            String mOPDeviceName = System.getString(this.mContext.getContentResolver(), "oem_oneplus_devicename");
            this.mBluetoothAdapter.setName(OPUtils.resetDeviceNameIfInvalid(this.mContext));
        }
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
    }

    public void onStop() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    private void setBluetoothDiscoverableState() {
        int mBluetoothScanMode = System.getInt(this.mContext.getContentResolver(), "bluetooth_default_scan_mode", 23);
        if (mBluetoothScanMode == 23) {
            this.mBluetoothAdapter.setScanMode(23);
        } else if (mBluetoothScanMode == 21) {
            this.mBluetoothAdapter.setScanMode(21);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mPreference = screen.findPreference(getPreferenceKey());
        }
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            return 0;
        }
        return 2;
    }

    public CharSequence getSummary() {
        if (this.mBluetoothAdapter == null || !this.mBluetoothAdapter.isEnabled()) {
            return this.mContext.getString(R.string.connected_device_add_device_summary);
        }
        return "";
    }

    /* Access modifiers changed, original: 0000 */
    public void updateState() {
        updateState(this.mPreference);
    }
}
