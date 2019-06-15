package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothMapClient;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import com.android.settingslib.R;
import java.util.ArrayList;
import java.util.List;

public final class MapClientProfile implements LocalBluetoothProfile {
    static final String NAME = "MAP Client";
    private static final int ORDINAL = 0;
    private static final String TAG = "MapClientProfile";
    static final ParcelUuid[] UUIDS = new ParcelUuid[]{BluetoothUuid.MAP, BluetoothUuid.MNS, BluetoothUuid.MAS};
    private static boolean V = false;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothMapClient mService;

    private final class MapClientServiceListener implements ServiceListener {
        private MapClientServiceListener() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (MapClientProfile.V) {
                Log.d(MapClientProfile.TAG, "Bluetooth service connected");
            }
            MapClientProfile.this.mService = (BluetoothMapClient) proxy;
            List<BluetoothDevice> deviceList = MapClientProfile.this.mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = (BluetoothDevice) deviceList.remove(0);
                CachedBluetoothDevice device = MapClientProfile.this.mDeviceManager.findDevice(nextDevice);
                if (device == null) {
                    String str = MapClientProfile.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("MapProfile found new device: ");
                    stringBuilder.append(nextDevice);
                    Log.w(str, stringBuilder.toString());
                    device = MapClientProfile.this.mDeviceManager.addDevice(MapClientProfile.this.mLocalAdapter, MapClientProfile.this.mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(MapClientProfile.this, 2);
                device.refresh();
            }
            MapClientProfile.this.mProfileManager.callServiceConnectedListeners();
            MapClientProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            if (MapClientProfile.V) {
                Log.d(MapClientProfile.TAG, "Bluetooth service disconnected");
            }
            MapClientProfile.this.mProfileManager.callServiceDisconnectedListeners();
            MapClientProfile.this.mIsProfileReady = false;
        }
    }

    public boolean isProfileReady() {
        if (V) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("isProfileReady(): ");
            stringBuilder.append(this.mIsProfileReady);
            Log.d(str, stringBuilder.toString());
        }
        return this.mIsProfileReady;
    }

    public int getProfileId() {
        return 18;
    }

    MapClientProfile(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, LocalBluetoothProfileManager profileManager) {
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mProfileManager = profileManager;
        this.mLocalAdapter.getProfileProxy(context, new MapClientServiceListener(), 18);
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
        List<BluetoothDevice> connectedDevices = getConnectedDevices();
        if (connectedDevices == null || !connectedDevices.contains(device)) {
            return this.mService.connect(device);
        }
        Log.d(TAG, "Ignoring Connect");
        return true;
    }

    public boolean disconnect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        if (this.mService.getPriority(device) > 100) {
            this.mService.setPriority(device, 100);
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

    public List<BluetoothDevice> getConnectedDevices() {
        if (this.mService == null) {
            return new ArrayList(0);
        }
        return this.mService.getDevicesMatchingConnectionStates(new int[]{2, 1, 3});
    }

    public String toString() {
        return NAME;
    }

    public int getOrdinal() {
        return 0;
    }

    public int getNameResource(BluetoothDevice device) {
        return R.string.bluetooth_profile_map;
    }

    public int getSummaryResourceForDevice(BluetoothDevice device) {
        int state = getConnectionStatus(device);
        if (state == 0) {
            return R.string.bluetooth_map_profile_summary_use_for;
        }
        if (state != 2) {
            return Utils.getConnectionStateSummary(state);
        }
        return R.string.bluetooth_map_profile_summary_connected;
    }

    public int getDrawableResource(BluetoothClass btClass) {
        return R.drawable.ic_bt_cellphone;
    }

    /* Access modifiers changed, original: protected */
    public void finalize() {
        if (V) {
            Log.d(TAG, "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(18, this.mService);
                this.mService = null;
            } catch (Throwable t) {
                Log.w(TAG, "Error cleaning up MAP Client proxy", t);
            }
        }
    }
}
