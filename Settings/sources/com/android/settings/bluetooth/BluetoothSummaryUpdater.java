package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.widget.SummaryUpdater;
import com.android.settings.widget.SummaryUpdater.OnSummaryChangeListener;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import java.util.Set;

public final class BluetoothSummaryUpdater extends SummaryUpdater implements BluetoothCallback {
    private static final String TAG = "BluetoothSummaryUpdater";
    private final LocalBluetoothAdapter mBluetoothAdapter;
    private final LocalBluetoothManager mBluetoothManager;

    public BluetoothSummaryUpdater(Context context, OnSummaryChangeListener listener, LocalBluetoothManager bluetoothManager) {
        super(context, listener);
        this.mBluetoothManager = bluetoothManager;
        this.mBluetoothAdapter = this.mBluetoothManager != null ? this.mBluetoothManager.getBluetoothAdapter() : null;
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        notifyChangeIfNeeded();
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
        notifyChangeIfNeeded();
    }

    public void onScanningStateChanged(boolean started) {
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
    }

    public void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
    }

    public void onAudioModeChanged() {
    }

    public void register(boolean listening) {
        if (this.mBluetoothAdapter != null) {
            if (listening) {
                notifyChangeIfNeeded();
                this.mBluetoothManager.getEventManager().registerCallback(this);
            } else {
                this.mBluetoothManager.getEventManager().unregisterCallback(this);
            }
        }
    }

    public String getSummary() {
        if (this.mBluetoothAdapter == null || !this.mBluetoothAdapter.isEnabled()) {
            return this.mContext.getString(R.string.bluetooth_disabled);
        }
        switch (this.mBluetoothAdapter.getConnectionState()) {
            case 1:
                return this.mContext.getString(R.string.bluetooth_connecting);
            case 2:
                return getConnectedDeviceSummary();
            case 3:
                return this.mContext.getString(R.string.bluetooth_disconnecting);
            default:
                return this.mContext.getString(R.string.disconnected);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getConnectedDeviceSummary() {
        String deviceName = null;
        int count = 0;
        Set<BluetoothDevice> devices = this.mBluetoothAdapter.getBondedDevices();
        if (devices == null) {
            Log.e(TAG, "getConnectedDeviceSummary, bonded devices are null");
            return this.mContext.getString(R.string.bluetooth_disabled);
        } else if (devices.isEmpty()) {
            Log.e(TAG, "getConnectedDeviceSummary, no bonded devices");
            return this.mContext.getString(R.string.disconnected);
        } else {
            for (BluetoothDevice device : devices) {
                if (device.isConnected()) {
                    deviceName = device.getName();
                    count++;
                    if (count > 1) {
                        break;
                    }
                }
            }
            String str;
            if (deviceName == null) {
                str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("getConnectedDeviceSummary, deviceName is null, numBondedDevices=");
                stringBuilder.append(devices.size());
                Log.e(str, stringBuilder.toString());
                for (BluetoothDevice device2 : devices) {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("getConnectedDeviceSummary, device=");
                    stringBuilder2.append(device2.getName());
                    stringBuilder2.append("[");
                    stringBuilder2.append(device2.getAddress());
                    stringBuilder2.append("], isConnected=");
                    stringBuilder2.append(device2.isConnected());
                    Log.e(str2, stringBuilder2.toString());
                }
                return this.mContext.getString(R.string.disconnected);
            }
            if (count > 1) {
                str = this.mContext.getString(R.string.bluetooth_connected_multiple_devices_summary);
            } else {
                str = this.mContext.getString(R.string.bluetooth_connected_summary, new Object[]{deviceName});
            }
            return str;
        }
    }
}
