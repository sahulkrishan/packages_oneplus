package com.android.settings.connecteddevice;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.bluetooth.AvailableMediaBluetoothDeviceUpdater;
import com.android.settings.bluetooth.BluetoothDeviceUpdater;
import com.android.settings.bluetooth.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class AvailableMediaDeviceGroupController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop, DevicePreferenceCallback, BluetoothCallback {
    private static final String KEY = "available_device_list";
    private BluetoothDeviceUpdater mBluetoothDeviceUpdater;
    private final LocalBluetoothManager mLocalBluetoothManager = Utils.getLocalBtManager(this.mContext);
    @VisibleForTesting
    PreferenceGroup mPreferenceGroup;

    public AvailableMediaDeviceGroupController(Context context) {
        super(context, KEY);
    }

    public void onStart() {
        this.mBluetoothDeviceUpdater.registerCallback();
        this.mLocalBluetoothManager.getEventManager().registerCallback(this);
    }

    public void onStop() {
        this.mBluetoothDeviceUpdater.unregisterCallback();
        this.mLocalBluetoothManager.getEventManager().unregisterCallback(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mPreferenceGroup = (PreferenceGroup) screen.findPreference(KEY);
            this.mPreferenceGroup.setVisible(false);
            updateTitle();
            this.mBluetoothDeviceUpdater.setPrefContext(screen.getContext());
            this.mBluetoothDeviceUpdater.forceUpdate();
        }
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            return 0;
        }
        return 2;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public void onDeviceAdded(Preference preference) {
        if (this.mPreferenceGroup.getPreferenceCount() == 0) {
            this.mPreferenceGroup.setVisible(true);
        }
        this.mPreferenceGroup.addPreference(preference);
    }

    public void onDeviceRemoved(Preference preference) {
        this.mPreferenceGroup.removePreference(preference);
        if (this.mPreferenceGroup.getPreferenceCount() == 0) {
            this.mPreferenceGroup.setVisible(false);
        }
    }

    public void init(DashboardFragment fragment) {
        this.mBluetoothDeviceUpdater = new AvailableMediaBluetoothDeviceUpdater(fragment.getContext(), fragment, (DevicePreferenceCallback) this);
    }

    @VisibleForTesting
    public void setBluetoothDeviceUpdater(BluetoothDeviceUpdater bluetoothDeviceUpdater) {
        this.mBluetoothDeviceUpdater = bluetoothDeviceUpdater;
    }

    public void onBluetoothStateChanged(int bluetoothState) {
    }

    public void onScanningStateChanged(boolean started) {
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
    }

    public void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
    }

    public void onAudioModeChanged() {
        updateTitle();
    }

    private void updateTitle() {
        if (com.android.settingslib.Utils.isAudioModeOngoingCall(this.mContext)) {
            this.mPreferenceGroup.setTitle((CharSequence) this.mContext.getString(R.string.connected_device_available_call_title));
        } else {
            this.mPreferenceGroup.setTitle((CharSequence) this.mContext.getString(R.string.connected_device_available_media_title));
        }
    }
}
