package com.android.settings.deviceinfo;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.SpannedString;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.bluetooth.BluetoothLengthDeviceNameFilter;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.ValidatedEditTextPreference;
import com.android.settings.widget.ValidatedEditTextPreference.Validator;
import com.android.settings.wifi.tether.WifiDeviceNameTextValidator;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;

public class DeviceNamePreferenceController extends BasePreferenceController implements Validator, OnPreferenceChangeListener, LifecycleObserver, OnSaveInstanceState, OnCreate {
    public static final int DEVICE_NAME_SET_WARNING_ID = 1;
    private static final String KEY_PENDING_DEVICE_NAME = "key_pending_device_name";
    private static final String PREF_KEY = "device_name";
    private LocalBluetoothManager mBluetoothManager;
    private String mDeviceName;
    private DeviceNamePreferenceHost mHost;
    private String mPendingDeviceName;
    private ValidatedEditTextPreference mPreference;
    private final WifiDeviceNameTextValidator mWifiDeviceNameTextValidator = new WifiDeviceNameTextValidator();
    protected WifiManager mWifiManager;

    public interface DeviceNamePreferenceHost {
        void showDeviceNameWarningDialog(String str);
    }

    public DeviceNamePreferenceController(Context context) {
        super(context, PREF_KEY);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        initializeDeviceName();
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (ValidatedEditTextPreference) screen.findPreference(PREF_KEY);
        CharSequence deviceName = getSummary();
        this.mPreference.setSummary(deviceName);
        this.mPreference.setText(deviceName.toString());
        this.mPreference.setValidator(this);
    }

    private void initializeDeviceName() {
        this.mDeviceName = System.getString(this.mContext.getContentResolver(), "oem_oneplus_devicename");
        if (this.mDeviceName == null) {
            this.mDeviceName = Build.MODEL;
        }
    }

    public CharSequence getSummary() {
        return this.mDeviceName;
    }

    public int getAvailabilityStatus() {
        return 3;
    }

    public String getPreferenceKey() {
        return PREF_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String newName = (String) newValue;
        if (newName.chars().mapToObj(-$$Lambda$DeviceNamePreferenceController$Lr3uWPHQr48vBIPFtS5k7uGE1G4.INSTANCE).anyMatch(-$$Lambda$lwniU_peSTkeyPRoDzcAe5p0U-0.INSTANCE) || "null".equalsIgnoreCase(newName)) {
            Toast.makeText(this.mContext, this.mContext.getResources().getString(R.string.wifi_p2p_failed_rename_message), 0).show();
            return false;
        }
        this.mPendingDeviceName = (String) newValue;
        if (this.mHost != null) {
            this.mHost.showDeviceNameWarningDialog(this.mPendingDeviceName);
        }
        return true;
    }

    public boolean isTextValid(String deviceName) {
        return this.mWifiDeviceNameTextValidator.isTextValid(deviceName);
    }

    public void setLocalBluetoothManager(LocalBluetoothManager localBluetoothManager) {
        this.mBluetoothManager = localBluetoothManager;
    }

    public void confirmDeviceName() {
        if (this.mPendingDeviceName != null) {
            setDeviceName(this.mPendingDeviceName);
        }
    }

    public void cancelDeviceName() {
        this.mPreference.setSummary((CharSequence) this.mDeviceName);
        this.mPreference.setText(this.mDeviceName);
    }

    public void setHost(DeviceNamePreferenceHost host) {
        this.mHost = host;
    }

    private void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
        setSettingsGlobalDeviceName(deviceName);
        setBluetoothDeviceName(deviceName);
        setTetherSsidName(deviceName);
        this.mPreference.setSummary(getSummary());
    }

    private void setSettingsGlobalDeviceName(String deviceName) {
        Global.putString(this.mContext.getContentResolver(), PREF_KEY, deviceName);
        System.putString(this.mContext.getContentResolver(), "oem_oneplus_devicename", this.mDeviceName);
    }

    private void setBluetoothDeviceName(String deviceName) {
        if (this.mBluetoothManager != null) {
            LocalBluetoothAdapter localBluetoothAdapter = this.mBluetoothManager.getBluetoothAdapter();
            if (localBluetoothAdapter != null) {
                localBluetoothAdapter.setName(getFilteredBluetoothString(deviceName));
            }
        }
    }

    private static final String getFilteredBluetoothString(String deviceName) {
        CharSequence filteredSequence = new BluetoothLengthDeviceNameFilter().filter(deviceName, 0, deviceName.length(), new SpannedString(""), 0, 0);
        if (filteredSequence == null) {
            return deviceName;
        }
        return filteredSequence.toString();
    }

    private void setTetherSsidName(String deviceName) {
        WifiConfiguration config = this.mWifiManager.getWifiApConfiguration();
        config.SSID = deviceName;
        this.mWifiManager.setWifiApConfiguration(config);
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mPendingDeviceName = savedInstanceState.getString(KEY_PENDING_DEVICE_NAME, null);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_PENDING_DEVICE_NAME, this.mPendingDeviceName);
    }
}
