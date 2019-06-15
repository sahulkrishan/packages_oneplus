package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.VisibleForTesting;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class ConnectedBluetoothDeviceUpdater extends BluetoothDeviceUpdater {
    private static final boolean DBG = false;
    private static final String TAG = "ConnBluetoothDeviceUpdater";
    private final AudioManager mAudioManager;

    public ConnectedBluetoothDeviceUpdater(Context context, DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback) {
        super(context, fragment, devicePreferenceCallback);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    @VisibleForTesting
    ConnectedBluetoothDeviceUpdater(DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback, LocalBluetoothManager localBluetoothManager) {
        super(fragment, devicePreferenceCallback, localBluetoothManager);
        this.mAudioManager = (AudioManager) fragment.getContext().getSystemService("audio");
    }

    public void onAudioModeChanged() {
        forceUpdate();
    }

    public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state, int bluetoothProfile) {
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
        int audioMode = this.mAudioManager.getMode();
        int currentAudioProfile = 2;
        if (audioMode == 1 || audioMode == 2 || audioMode == 3) {
            currentAudioProfile = 1;
        }
        if (!isDeviceConnected(cachedDevice)) {
            return false;
        }
        switch (currentAudioProfile) {
            case 1:
                return 1 ^ cachedDevice.isHfpDevice();
            case 2:
                return 1 ^ cachedDevice.isA2dpDevice();
            default:
                return false;
        }
    }

    /* Access modifiers changed, original: protected */
    public void addPreference(CachedBluetoothDevice cachedDevice) {
        super.addPreference(cachedDevice);
        BluetoothDevice device = cachedDevice.getDevice();
        if (this.mPreferenceMap.containsKey(device)) {
            BluetoothDevicePreference btPreference = (BluetoothDevicePreference) this.mPreferenceMap.get(device);
            btPreference.setOnGearClickListener(null);
            btPreference.hideSecondTarget(true);
            btPreference.setOnPreferenceClickListener(new -$$Lambda$ConnectedBluetoothDeviceUpdater$cbcA1LEPXmJVOc_WhespijdA8X8(this));
        }
    }
}
