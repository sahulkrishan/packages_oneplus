package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.Context;
import android.util.Log;
import com.android.settingslib.R;
import java.util.HashMap;
import java.util.List;

public class PanProfile implements LocalBluetoothProfile {
    static final String NAME = "PAN";
    private static final int ORDINAL = 4;
    private static final String TAG = "PanProfile";
    private static boolean V = true;
    private final HashMap<BluetoothDevice, Integer> mDeviceRoleMap = new HashMap();
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private BluetoothPan mService;

    private final class PanServiceListener implements ServiceListener {
        private PanServiceListener() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (PanProfile.V) {
                Log.d(PanProfile.TAG, "Bluetooth service connected");
            }
            PanProfile.this.mService = (BluetoothPan) proxy;
            PanProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            if (PanProfile.V) {
                Log.d(PanProfile.TAG, "Bluetooth service disconnected");
            }
            PanProfile.this.mIsProfileReady = false;
        }
    }

    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    public int getProfileId() {
        return 5;
    }

    PanProfile(Context context, LocalBluetoothAdapter adapter) {
        this.mLocalAdapter = adapter;
        this.mLocalAdapter.getProfileProxy(context, new PanServiceListener(), 5);
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> sinks = this.mService.getConnectedDevices();
        if (sinks != null) {
            for (BluetoothDevice sink : sinks) {
                this.mService.disconnect(sink);
            }
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
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getConnectionState(device);
    }

    public boolean isPreferred(BluetoothDevice device) {
        return true;
    }

    public int getPreferred(BluetoothDevice device) {
        return -1;
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
    }

    public String toString() {
        return NAME;
    }

    public int getOrdinal() {
        return 4;
    }

    public int getNameResource(BluetoothDevice device) {
        if (isLocalRoleNap(device)) {
            return R.string.bluetooth_profile_pan_nap;
        }
        return R.string.bluetooth_profile_pan;
    }

    public int getSummaryResourceForDevice(BluetoothDevice device) {
        int state = getConnectionStatus(device);
        if (state == 0) {
            return R.string.bluetooth_pan_profile_summary_use_for;
        }
        if (state != 2) {
            return Utils.getConnectionStateSummary(state);
        }
        if (isLocalRoleNap(device)) {
            return R.string.bluetooth_pan_nap_profile_summary_connected;
        }
        return R.string.bluetooth_pan_user_profile_summary_connected;
    }

    public int getDrawableResource(BluetoothClass btClass) {
        return R.drawable.ic_bt_network_pan;
    }

    /* Access modifiers changed, original: 0000 */
    public void setLocalRole(BluetoothDevice device, int role) {
        this.mDeviceRoleMap.put(device, Integer.valueOf(role));
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isLocalRoleNap(BluetoothDevice device) {
        boolean z = false;
        if (!this.mDeviceRoleMap.containsKey(device)) {
            return false;
        }
        if (((Integer) this.mDeviceRoleMap.get(device)).intValue() == 1) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: protected */
    public void finalize() {
        if (V) {
            Log.d(TAG, "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(5, this.mService);
                this.mService = null;
            } catch (Throwable t) {
                Log.w(TAG, "Error cleaning up PAN proxy", t);
            }
        }
    }
}
