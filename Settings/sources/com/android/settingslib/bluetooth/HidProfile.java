package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidHost;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.Context;
import android.util.Log;
import com.android.settingslib.R;
import java.util.List;

public class HidProfile implements LocalBluetoothProfile {
    static final String NAME = "HID";
    private static final int ORDINAL = 3;
    private static final String TAG = "HidProfile";
    private static boolean V = true;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothHidHost mService;

    private final class HidHostServiceListener implements ServiceListener {
        private HidHostServiceListener() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (HidProfile.V) {
                Log.d(HidProfile.TAG, "Bluetooth service connected");
            }
            HidProfile.this.mService = (BluetoothHidHost) proxy;
            List<BluetoothDevice> deviceList = HidProfile.this.mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = (BluetoothDevice) deviceList.remove(0);
                CachedBluetoothDevice device = HidProfile.this.mDeviceManager.findDevice(nextDevice);
                if (device == null) {
                    String str = HidProfile.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("HidProfile found new device: ");
                    stringBuilder.append(nextDevice);
                    Log.w(str, stringBuilder.toString());
                    device = HidProfile.this.mDeviceManager.addDevice(HidProfile.this.mLocalAdapter, HidProfile.this.mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(HidProfile.this, 2);
                device.refresh();
            }
            HidProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            if (HidProfile.V) {
                Log.d(HidProfile.TAG, "Bluetooth service disconnected");
            }
            HidProfile.this.mIsProfileReady = false;
        }
    }

    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    public int getProfileId() {
        return 4;
    }

    HidProfile(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, LocalBluetoothProfileManager profileManager) {
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mProfileManager = profileManager;
        adapter.getProfileProxy(context, new HidHostServiceListener(), 4);
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return true;
    }

    public boolean connect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.connect(device);
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
        if (!deviceList.isEmpty() && ((BluetoothDevice) deviceList.get(0)).equals(device)) {
            i = this.mService.getConnectionState(device);
        }
        return i;
    }

    public boolean isPreferred(BluetoothDevice device) {
        boolean z = false;
        if (this.mService == null) {
            return false;
        }
        if (this.mService.getPriority(device) > 0) {
            z = true;
        }
        return z;
    }

    public int getPreferred(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getPriority(device);
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
        if (this.mService != null) {
            if (!preferred) {
                this.mService.setPriority(device, 0);
            } else if (this.mService.getPriority(device) < 100) {
                this.mService.setPriority(device, 100);
            }
        }
    }

    public String toString() {
        return NAME;
    }

    public int getOrdinal() {
        return 3;
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
        if (btClass == null) {
            return R.drawable.ic_lockscreen_ime;
        }
        return getHidClassDrawable(btClass);
    }

    public static int getHidClassDrawable(BluetoothClass btClass) {
        int deviceClass = btClass.getDeviceClass();
        if (deviceClass != 1344) {
            if (deviceClass == 1408) {
                return R.drawable.ic_bt_pointing_hid;
            }
            if (deviceClass != 1472) {
                return R.drawable.ic_bt_misc_hid;
            }
        }
        return R.drawable.ic_lockscreen_ime;
    }

    /* Access modifiers changed, original: protected */
    public void finalize() {
        if (V) {
            Log.d(TAG, "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(4, this.mService);
                this.mService = null;
            } catch (Throwable t) {
                Log.w(TAG, "Error cleaning up HID proxy", t);
            }
        }
    }
}
