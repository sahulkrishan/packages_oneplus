package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.SettingsBaseApplication;

public class OPBluetoothDiscoverablePreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_DISCOVERABLE_DEVICE = "discoverable_device";
    private static final String SETTINGS_SYSTEM_BLUETOOTH_DEFAULT_SCAN_MODE = "bluetooth_default_scan_mode";
    private int mBluetoothScanMode = 23;
    private Context mContext;
    LocalBluetoothAdapter mLocalAdapter;
    private SettingObserver mSettingObserver;
    private BroadcastReceiver mStatusReceive = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int i = (action.hashCode() == -1530327060 && action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) ? 0 : -1;
            if (i == 0) {
                switch (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 0)) {
                    case 10:
                        OPBluetoothDiscoverablePreferenceController.this.mSwitchPreference.setEnabled(false);
                        return;
                    case 12:
                        OPBluetoothDiscoverablePreferenceController.this.mSwitchPreference.setEnabled(true);
                        return;
                    default:
                        return;
                }
            }
        }
    };
    SwitchPreference mSwitchPreference;

    class SettingObserver extends ContentObserver {
        private final Uri SETTINGS_SYSTEM_BLUETOOTH_DEFAULT_SCAN_MODE_URI = System.getUriFor(OPBluetoothDiscoverablePreferenceController.SETTINGS_SYSTEM_BLUETOOTH_DEFAULT_SCAN_MODE);
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.SETTINGS_SYSTEM_BLUETOOTH_DEFAULT_SCAN_MODE_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.SETTINGS_SYSTEM_BLUETOOTH_DEFAULT_SCAN_MODE_URI.equals(uri)) {
                OPBluetoothDiscoverablePreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public OPBluetoothDiscoverablePreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_DISCOVERABLE_DEVICE);
        this.mContext = context;
        this.mLocalAdapter = Utils.getLocalBtManager(SettingsBaseApplication.mApplication).getBluetoothAdapter();
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSwitchPreference = (SwitchPreference) screen.findPreference(getPreferenceKey());
        this.mSettingObserver = new SettingObserver(screen.findPreference(KEY_DISCOVERABLE_DEVICE));
    }

    public void onResume() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), true);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        this.mContext.registerReceiver(this.mStatusReceive, intentFilter);
    }

    public void onPause() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), false);
        }
        this.mContext.unregisterReceiver(this.mStatusReceive);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    private void saveScanModeToSettingsProvider(int scanMode) {
        System.putInt(this.mContext.getContentResolver(), SETTINGS_SYSTEM_BLUETOOTH_DEFAULT_SCAN_MODE, scanMode);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_DISCOVERABLE_DEVICE) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        if (((SwitchPreference) preference).isChecked()) {
            this.mLocalAdapter.setScanMode(23);
            this.mLocalAdapter.setDiscoverableTimeout(120);
            saveScanModeToSettingsProvider(23);
        } else {
            this.mLocalAdapter.setScanMode(21);
            saveScanModeToSettingsProvider(21);
        }
        return true;
    }

    public String getPreferenceKey() {
        return KEY_DISCOVERABLE_DEVICE;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference enableSwitch = (SwitchPreference) preference;
            this.mBluetoothScanMode = System.getInt(this.mContext.getContentResolver(), SETTINGS_SYSTEM_BLUETOOTH_DEFAULT_SCAN_MODE, 21);
            if (this.mBluetoothScanMode == 23) {
                this.mLocalAdapter.setScanMode(23);
                enableSwitch.setSummary((int) R.string.bluetooth_is_discoverable_always);
                enableSwitch.setChecked(true);
            } else if (this.mBluetoothScanMode == 21) {
                this.mLocalAdapter.setScanMode(21);
                enableSwitch.setSummary((int) R.string.bluetooth_not_visible_to_other_devices);
                enableSwitch.setChecked(false);
            }
            enableSwitch.setEnabled(this.mLocalAdapter.isEnabled());
        }
    }
}
