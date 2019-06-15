package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class SavedBluetoothTwsDeviceUpdater extends BluetoothDeviceUpdater {
    public SavedBluetoothTwsDeviceUpdater(Context context, DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback) {
        super(context, fragment, devicePreferenceCallback);
    }

    SavedBluetoothTwsDeviceUpdater(DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback, LocalBluetoothManager localBluetoothManager) {
        super(fragment, devicePreferenceCallback, localBluetoothManager);
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
        if (state == 2) {
            removePreference(cachedDevice);
        } else if (state == 0) {
            addPreference(cachedDevice);
        }
    }

    public boolean isFilterMatched(CachedBluetoothDevice cachedDevice) {
        BluetoothDevice device = cachedDevice.getDevice();
        return device.getBondState() == 12 && !device.isConnected() && device.isTwsPlusDevice();
    }
}
