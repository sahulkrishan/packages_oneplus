package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.connecteddevice.OPBluetoothCarKitDevicePreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager.ServiceListener;
import java.util.HashMap;
import java.util.Map;

public abstract class OPBluetoothCarKitDeviceUpdater implements BluetoothCallback, ServiceListener {
    private static final String BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY = "persist.bluetooth.showdeviceswithoutnames";
    private static final String TAG = "BluetoothDeviceUpdater";
    protected final DevicePreferenceCallback mDevicePreferenceCallback;
    protected DashboardFragment mFragment;
    protected final LocalBluetoothManager mLocalManager;
    protected Context mPrefContext;
    protected final Map<BluetoothDevice, Preference> mPreferenceMap;
    private final boolean mShowDeviceWithoutNames;

    public abstract boolean isFilterMatched(CachedBluetoothDevice cachedBluetoothDevice);

    public OPBluetoothCarKitDeviceUpdater(Context context, DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback) {
        this(fragment, devicePreferenceCallback, Utils.getLocalBtManager(context));
    }

    @VisibleForTesting
    OPBluetoothCarKitDeviceUpdater(DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback, LocalBluetoothManager localManager) {
        this.mFragment = fragment;
        this.mDevicePreferenceCallback = devicePreferenceCallback;
        this.mShowDeviceWithoutNames = SystemProperties.getBoolean(BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY, true);
        this.mPreferenceMap = new HashMap();
        this.mLocalManager = localManager;
    }

    public void registerCallback() {
        this.mLocalManager.setForegroundActivity(this.mFragment.getContext());
        this.mLocalManager.getEventManager().registerCallback(this);
        this.mLocalManager.getProfileManager().addServiceListener(this);
        forceUpdate();
    }

    public void unregisterCallback() {
        this.mLocalManager.setForegroundActivity(null);
        this.mLocalManager.getEventManager().unregisterCallback(this);
        this.mLocalManager.getProfileManager().removeServiceListener(this);
    }

    public void forceUpdate() {
        for (CachedBluetoothDevice cachedBluetoothDevice : this.mLocalManager.getCachedDeviceManager().getCachedDevicesCopy()) {
            update(cachedBluetoothDevice);
        }
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        forceUpdate();
    }

    public void onScanningStateChanged(boolean started) {
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        update(cachedDevice);
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        removePreference(cachedDevice);
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        update(cachedDevice);
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
    }

    public void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
    }

    public void onAudioModeChanged() {
    }

    public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state, int bluetoothProfile) {
    }

    public void onServiceConnected() {
        forceUpdate();
    }

    public void onServiceDisconnected() {
    }

    public void setPrefContext(Context context) {
        this.mPrefContext = context;
    }

    /* Access modifiers changed, original: protected */
    public void update(CachedBluetoothDevice cachedBluetoothDevice) {
        if (isFilterMatched(cachedBluetoothDevice)) {
            addPreference(cachedBluetoothDevice);
        } else {
            removePreference(cachedBluetoothDevice);
        }
    }

    /* Access modifiers changed, original: protected */
    public void addPreference(CachedBluetoothDevice cachedDevice) {
        BluetoothDevice device = cachedDevice.getDevice();
        if (!this.mPreferenceMap.containsKey(device)) {
            OPBluetoothCarKitDevicePreference CarKitDevicePreference = new OPBluetoothCarKitDevicePreference(this.mPrefContext, cachedDevice, this.mShowDeviceWithoutNames);
            this.mPreferenceMap.put(device, CarKitDevicePreference);
            this.mDevicePreferenceCallback.onDeviceAdded(CarKitDevicePreference);
        }
    }

    /* Access modifiers changed, original: protected */
    public void removePreference(CachedBluetoothDevice cachedDevice) {
        BluetoothDevice device = cachedDevice.getDevice();
        if (this.mPreferenceMap.containsKey(device)) {
            this.mDevicePreferenceCallback.onDeviceRemoved((Preference) this.mPreferenceMap.get(device));
            this.mPreferenceMap.remove(device);
        }
    }

    public boolean isDeviceConnected(CachedBluetoothDevice cachedDevice) {
        boolean z = false;
        if (cachedDevice == null) {
            return false;
        }
        if (cachedDevice.getDevice().getBondState() == 12) {
            z = true;
        }
        return z;
    }
}
