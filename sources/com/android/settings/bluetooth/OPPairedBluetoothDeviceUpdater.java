package com.android.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.connecteddevice.OPBluetoothCarKitDevicePreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class OPPairedBluetoothDeviceUpdater extends OPBluetoothCarKitDeviceUpdater {
    private static final boolean DBG = true;
    private static final String TAG = "OPPairedBluetoothDeviceUpdater";
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public OPPairedBluetoothDeviceUpdater(Context context, DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback) {
        super(context, fragment, devicePreferenceCallback);
    }

    @VisibleForTesting
    OPPairedBluetoothDeviceUpdater(DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback, LocalBluetoothManager localBluetoothManager) {
        super(fragment, devicePreferenceCallback, localBluetoothManager);
    }

    public void onAudioModeChanged() {
        forceUpdate();
    }

    public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state, int bluetoothProfile) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onProfileConnectionStateChanged() device: ");
        stringBuilder.append(cachedDevice.getName());
        stringBuilder.append(", state: ");
        stringBuilder.append(state);
        stringBuilder.append(", bluetoothProfile: ");
        stringBuilder.append(bluetoothProfile);
        Log.d(str, stringBuilder.toString());
        if (state == 2) {
            if (isFilterMatched(cachedDevice)) {
                addPreference(cachedDevice);
            } else {
                removePreference(cachedDevice);
            }
        } else if (state == 0) {
            removePreference(cachedDevice);
        }
    }

    public boolean isFilterMatched(CachedBluetoothDevice cachedDevice) {
        if (!isDeviceConnected(cachedDevice) || this.mBluetoothAdapter.isCarkit(cachedDevice.getDevice())) {
            return false;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("isFilterMatched() device : ");
        stringBuilder.append(cachedDevice.getName());
        stringBuilder.append(", isFilterMatched : ");
        stringBuilder.append(true);
        Log.d(str, stringBuilder.toString());
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void addPreference(CachedBluetoothDevice cachedDevice) {
        super.addPreference(cachedDevice);
        BluetoothDevice device = cachedDevice.getDevice();
        if (this.mPreferenceMap.containsKey(device)) {
            OPBluetoothCarKitDevicePreference oPBluetoothCarKitDevicePreference = (OPBluetoothCarKitDevicePreference) this.mPreferenceMap.get(device);
        }
    }
}
