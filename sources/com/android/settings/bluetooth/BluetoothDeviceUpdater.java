package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.R;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.widget.GearPreference.OnGearClickListener;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager.ServiceListener;
import java.util.HashMap;
import java.util.Map;

public abstract class BluetoothDeviceUpdater implements BluetoothCallback, ServiceListener {
    private static final String BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY = "persist.bluetooth.showdeviceswithoutnames";
    private static final String TAG = "BluetoothDeviceUpdater";
    protected final DevicePreferenceCallback mDevicePreferenceCallback;
    @VisibleForTesting
    final OnGearClickListener mDeviceProfilesListener;
    protected DashboardFragment mFragment;
    protected final LocalBluetoothManager mLocalManager;
    protected Context mPrefContext;
    protected final Map<BluetoothDevice, Preference> mPreferenceMap;
    private final boolean mShowDeviceWithoutNames;

    public abstract boolean isFilterMatched(CachedBluetoothDevice cachedBluetoothDevice);

    public BluetoothDeviceUpdater(Context context, DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback) {
        this(fragment, devicePreferenceCallback, Utils.getLocalBtManager(context));
    }

    @VisibleForTesting
    BluetoothDeviceUpdater(DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback, LocalBluetoothManager localManager) {
        this.mDeviceProfilesListener = new -$$Lambda$BluetoothDeviceUpdater$9cHgqnTeqRHSfH6f9TvykmwcB28(this);
        this.mFragment = fragment;
        this.mDevicePreferenceCallback = devicePreferenceCallback;
        this.mShowDeviceWithoutNames = SystemProperties.getBoolean(BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY, true);
        this.mPreferenceMap = new HashMap();
        this.mLocalManager = localManager;
    }

    public void registerCallback() {
        this.mLocalManager.setForegroundActivity(this.mFragment.getContext());
        this.mLocalManager.getEventManager().registerCallback(this);
        this.mLocalManager.getProfileManager().addServiceListener(this);
        forceUpdate();
    }

    public void unregisterCallback() {
        this.mLocalManager.setForegroundActivity(null);
        this.mLocalManager.getEventManager().unregisterCallback(this);
        this.mLocalManager.getProfileManager().removeServiceListener(this);
    }

    public void forceUpdate() {
        for (CachedBluetoothDevice cachedBluetoothDevice : this.mLocalManager.getCachedDeviceManager().getCachedDevicesCopy()) {
            update(cachedBluetoothDevice);
        }
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        forceUpdate();
    }

    public void onScanningStateChanged(boolean started) {
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        update(cachedDevice);
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        removePreference(cachedDevice);
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        update(cachedDevice);
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
    }

    public void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
    }

    public void onAudioModeChanged() {
    }

    public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state, int bluetoothProfile) {
    }

    public void onServiceConnected() {
        forceUpdate();
    }

    public void onServiceDisconnected() {
    }

    public void setPrefContext(Context context) {
        this.mPrefContext = context;
    }

    /* Access modifiers changed, original: protected */
    public void update(CachedBluetoothDevice cachedBluetoothDevice) {
        if (isFilterMatched(cachedBluetoothDevice)) {
            addPreference(cachedBluetoothDevice);
        } else {
            removePreference(cachedBluetoothDevice);
        }
    }

    /* Access modifiers changed, original: protected */
    public void addPreference(CachedBluetoothDevice cachedDevice) {
        BluetoothDevice device = cachedDevice.getDevice();
        if (!this.mPreferenceMap.containsKey(device)) {
            BluetoothDevicePreference btPreference = new BluetoothDevicePreference(this.mPrefContext, cachedDevice, this.mShowDeviceWithoutNames);
            btPreference.setOnGearClickListener(this.mDeviceProfilesListener);
            if (this instanceof OnPreferenceClickListener) {
                btPreference.setOnPreferenceClickListener((OnPreferenceClickListener) this);
            }
            this.mPreferenceMap.put(device, btPreference);
            this.mDevicePreferenceCallback.onDeviceAdded(btPreference);
        }
    }

    /* Access modifiers changed, original: protected */
    public void removePreference(CachedBluetoothDevice cachedDevice) {
        BluetoothDevice device = cachedDevice.getDevice();
        if (this.mPreferenceMap.containsKey(device)) {
            this.mDevicePreferenceCallback.onDeviceRemoved((Preference) this.mPreferenceMap.get(device));
            this.mPreferenceMap.remove(device);
        }
    }

    /* Access modifiers changed, original: protected */
    public void launchDeviceDetails(Preference preference) {
        CachedBluetoothDevice device = ((BluetoothDevicePreference) preference).getBluetoothDevice();
        if (device != null) {
            Bundle args = new Bundle();
            args.putString("device_address", device.getDevice().getAddress());
            new SubSettingLauncher(this.mFragment.getContext()).setDestination(BluetoothDeviceDetailsFragment.class.getName()).setArguments(args).setTitle((int) R.string.device_details_title).setSourceMetricsCategory(this.mFragment.getMetricsCategory()).launch();
        }
    }

    public boolean isDeviceConnected(CachedBluetoothDevice cachedDevice) {
        boolean z = false;
        if (cachedDevice == null) {
            return false;
        }
        BluetoothDevice device = cachedDevice.getDevice();
        if (device.getBondState() == 12 && device.isConnected()) {
            z = true;
        }
        return z;
    }
}
