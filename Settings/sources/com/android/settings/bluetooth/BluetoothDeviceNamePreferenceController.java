package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class BluetoothDeviceNamePreferenceController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop {
    private static final String TAG = "BluetoothNamePrefCtrl";
    protected LocalBluetoothAdapter mLocalAdapter;
    private LocalBluetoothManager mLocalManager;
    @VisibleForTesting
    Preference mPreference;
    @VisibleForTesting
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, "android.bluetooth.adapter.action.LOCAL_NAME_CHANGED")) {
                if (BluetoothDeviceNamePreferenceController.this.mPreference != null && BluetoothDeviceNamePreferenceController.this.mLocalAdapter != null && BluetoothDeviceNamePreferenceController.this.mLocalAdapter.isEnabled()) {
                    BluetoothDeviceNamePreferenceController.this.updatePreferenceState(BluetoothDeviceNamePreferenceController.this.mPreference);
                }
            } else if (TextUtils.equals(action, "android.bluetooth.adapter.action.STATE_CHANGED")) {
                BluetoothDeviceNamePreferenceController.this.updatePreferenceState(BluetoothDeviceNamePreferenceController.this.mPreference);
            }
        }
    };

    public BluetoothDeviceNamePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        this.mLocalManager = Utils.getLocalBtManager(context);
        if (this.mLocalManager == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
        } else {
            this.mLocalAdapter = this.mLocalManager.getBluetoothAdapter();
        }
    }

    @VisibleForTesting
    BluetoothDeviceNamePreferenceController(Context context, LocalBluetoothAdapter localAdapter, String preferenceKey) {
        super(context, preferenceKey);
        this.mLocalAdapter = localAdapter;
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mPreference = screen.findPreference(getPreferenceKey());
        super.displayPreference(screen);
    }

    public void onStart() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    public void onStop() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public int getAvailabilityStatus() {
        return this.mLocalAdapter != null ? 0 : 2;
    }

    public void updateState(Preference preference) {
        updatePreferenceState(preference);
    }

    public CharSequence getSummary() {
        if (TextUtils.isEmpty(System.getString(this.mContext.getContentResolver(), "oem_oneplus_devicename"))) {
            return super.getSummary();
        }
        return TextUtils.expandTemplate(this.mContext.getText(R.string.bluetooth_device_name_summary), new CharSequence[]{BidiFormatter.getInstance().unicodeWrap(deviceName)}).toString();
    }

    public Preference createBluetoothDeviceNamePreference(PreferenceScreen screen, int order) {
        this.mPreference = new Preference(screen.getContext());
        this.mPreference.setOrder(order);
        this.mPreference.setKey(getPreferenceKey());
        screen.addPreference(this.mPreference);
        return this.mPreference;
    }

    /* Access modifiers changed, original: protected */
    public void updatePreferenceState(Preference preference) {
        preference.setSelectable(false);
        preference.setSummary(getSummary());
    }

    /* Access modifiers changed, original: protected */
    public String getDeviceName() {
        return this.mLocalAdapter.getName();
    }
}
