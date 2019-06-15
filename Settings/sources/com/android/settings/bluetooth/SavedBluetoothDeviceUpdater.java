package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class SavedBluetoothDeviceUpdater extends BluetoothDeviceUpdater implements OnPreferenceClickListener {
    private static final String TAG = "SavedBluetoothDeviceUpdater";

    public SavedBluetoothDeviceUpdater(Context context, DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback) {
        super(context, fragment, devicePreferenceCallback);
    }

    @VisibleForTesting
    SavedBluetoothDeviceUpdater(DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback, LocalBluetoothManager localBluetoothManager) {
        super(fragment, devicePreferenceCallback, localBluetoothManager);
    }

    public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state, int bluetoothProfile) {
        if (state == 2) {
            removePreference(cachedDevice);
        } else if (state == 0) {
            addPreference(cachedDevice);
        }
    }

    public boolean isFilterMatched(CachedBluetoothDevice cachedDevice) {
        BluetoothDevice device = cachedDevice.getDevice();
        return (device.getBondState() != 12 || device.isConnected() || device.isTwsPlusDevice()) ? false : true;
    }

    public boolean onPreferenceClick(Preference preference) {
        ((BluetoothDevicePreference) preference).getBluetoothDevice().connect(true);
        return true;
    }
}
