package com.android.settings.bluetooth;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class AvailableMediaBluetoothDeviceUpdater extends BluetoothDeviceUpdater implements OnPreferenceClickListener {
    private static final boolean DBG = true;
    private static final String TAG = "AvailableMediaBluetoothDeviceUpdater";
    private final AudioManager mAudioManager;

    public AvailableMediaBluetoothDeviceUpdater(Context context, DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback) {
        super(context, fragment, devicePreferenceCallback);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    @VisibleForTesting
    AvailableMediaBluetoothDeviceUpdater(DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback, LocalBluetoothManager localBluetoothManager) {
        super(fragment, devicePreferenceCallback, localBluetoothManager);
        this.mAudioManager = (AudioManager) fragment.getContext().getSystemService("audio");
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
        int audioMode = this.mAudioManager.getMode();
        int currentAudioProfile = 2;
        if (audioMode == 1 || audioMode == 2 || audioMode == 3) {
            currentAudioProfile = 1;
        }
        boolean isFilterMatched = false;
        if (isDeviceConnected(cachedDevice)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("isFilterMatched() current audio profile : ");
            stringBuilder.append(currentAudioProfile);
            Log.d(str, stringBuilder.toString());
            switch (currentAudioProfile) {
                case 1:
                    isFilterMatched = cachedDevice.isHfpDevice();
                    break;
                case 2:
                    isFilterMatched = cachedDevice.isA2dpDevice();
                    break;
            }
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("isFilterMatched() device : ");
            stringBuilder.append(cachedDevice.getName());
            stringBuilder.append(", isFilterMatched : ");
            stringBuilder.append(isFilterMatched);
            Log.d(str, stringBuilder.toString());
        }
        return isFilterMatched;
    }

    public boolean onPreferenceClick(Preference preference) {
        return ((BluetoothDevicePreference) preference).getBluetoothDevice().setActive();
    }
}
