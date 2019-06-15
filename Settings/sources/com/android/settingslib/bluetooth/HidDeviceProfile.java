package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.Context;
import android.util.Log;
import com.android.settingslib.R;
import java.util.List;

public class HidDeviceProfile implements LocalBluetoothProfile {
    private static final boolean DEBUG = true;
    static final String NAME = "HID DEVICE";
    private static final int ORDINAL = 18;
    private static final int PREFERRED_VALUE = -1;
    private static final String TAG = "HidDeviceProfile";
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothHidDevice mService;

    private final class HidDeviceServiceListener implements ServiceListener {
        private HidDeviceServiceListener() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(HidDeviceProfile.TAG, "Bluetooth service connected :-)");
            HidDeviceProfile.this.mService = (BluetoothHidDevice) proxy;
            for (BluetoothDevice nextDevice : HidDeviceProfile.this.mService.getConnectedDevices()) {
                String str;
                StringBuilder stringBuilder;
                CachedBluetoothDevice device = HidDeviceProfile.this.mDeviceManager.findDevice(nextDevice);
                if (device == null) {
                    str = HidDeviceProfile.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("HidProfile found new device: ");
                    stringBuilder.append(nextDevice);
                    Log.w(str, stringBuilder.toString());
                    device = HidDeviceProfile.this.mDeviceManager.addDevice(HidDeviceProfile.this.mLocalAdapter, HidDeviceProfile.this.mProfileManager, nextDevice);
                }
                str = HidDeviceProfile.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Connection status changed: ");
                stringBuilder.append(device);
                Log.d(str, stringBuilder.toString());
                device.onProfileStateChanged(HidDeviceProfile.this, 2);
                device.refresh();
            }
            HidDeviceProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            Log.d(HidDeviceProfile.TAG, "Bluetooth service disconnected");
            HidDeviceProfile.this.mIsProfileReady = false;
        }
    }

    HidDeviceProfile(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, LocalBluetoothProfileManager profileManager) {
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mProfileManager = profileManager;
        adapter.getProfileProxy(context, new HidDeviceServiceListener(), 19);
    }

    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    public int getProfileId() {
        return 19;
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.disconnect(device);
    }

    public int getConnectionStatus(BluetoothDevice device) {
        int i = 0;
        if (this.mService == null) {
            return 0;
        }
        List<BluetoothDevice> deviceList = this.mService.getConnectedDevices();
        if (!deviceList.isEmpty() && deviceList.contains(device)) {
            i = this.mService.getConnectionState(device);
        }
        return i;
    }

    public boolean isPreferred(BluetoothDevice device) {
        return getConnectionStatus(device) != 0;
    }

    public int getPreferred(BluetoothDevice device) {
        return -1;
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
        if (!preferred) {
            this.mService.disconnect(device);
        }
    }

    public String toString() {
        return NAME;
    }

    public int getOrdinal() {
        return 18;
    }

    public int getNameResource(BluetoothDevice device) {
        return R.string.bluetooth_profile_hid;
    }

    public int getSummaryResourceForDevice(BluetoothDevice device) {
        int state = getConnectionStatus(device);
        if (state == 0) {
            return R.string.bluetooth_hid_profile_summary_use_for;
        }
        if (state != 2) {
            return Utils.getConnectionStateSummary(state);
        }
        return R.string.bluetooth_hid_profile_summary_connected;
    }

    public int getDrawableResource(BluetoothClass btClass) {
        return R.drawable.ic_bt_misc_hid;
    }

    /* Access modifiers changed, original: protected */
    public void finalize() {
        Log.d(TAG, "finalize()");
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(19, this.mService);
                this.mService = null;
            } catch (Throwable t) {
                Log.w(TAG, "Error cleaning up HID proxy", t);
            }
        }
    }
}
