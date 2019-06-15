package com.android.settings.connecteddevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.bluetooth.OPBluetoothCarKitDeviceUpdater;
import com.android.settings.bluetooth.OPRecognizedBluetoothCarKitDeviceUpdater;
import com.android.settings.bluetooth.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class OPRecognizedBluetoothCarKitsDeviceGroupController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop, DevicePreferenceCallback, BluetoothCallback {
    private static final String KEY = "recognized_bluetooth_car_kits";
    private final LocalBluetoothManager mLocalBluetoothManager = Utils.getLocalBtManager(this.mContext);
    private OPBluetoothCarKitDeviceUpdater mOPBluetoothCarKitDeviceUpdater;
    @VisibleForTesting
    PreferenceGroup mPreferenceGroup;
    private BroadcastReceiver mStatusReceive = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Object obj = (action.hashCode() == 140676821 && action.equals("oneplus.action.intent.UpdateBluetoothCarkitDevice")) ? null : -1;
            if (obj == null) {
                OPRecognizedBluetoothCarKitsDeviceGroupController.this.mOPBluetoothCarKitDeviceUpdater.forceUpdate();
            }
        }
    };

    public OPRecognizedBluetoothCarKitsDeviceGroupController(Context context) {
        super(context, KEY);
    }

    public void onStart() {
        this.mOPBluetoothCarKitDeviceUpdater.registerCallback();
        this.mLocalBluetoothManager.getEventManager().registerCallback(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("oneplus.action.intent.UpdateBluetoothCarkitDevice");
        this.mContext.registerReceiver(this.mStatusReceive, intentFilter);
    }

    public void onStop() {
        this.mOPBluetoothCarKitDeviceUpdater.unregisterCallback();
        this.mLocalBluetoothManager.getEventManager().unregisterCallback(this);
        this.mContext.unregisterReceiver(this.mStatusReceive);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mPreferenceGroup = (PreferenceGroup) screen.findPreference(KEY);
            this.mOPBluetoothCarKitDeviceUpdater.setPrefContext(screen.getContext());
            this.mOPBluetoothCarKitDeviceUpdater.forceUpdate();
        }
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            return 0;
        }
        return 2;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public void onDeviceAdded(Preference preference) {
        if (this.mPreferenceGroup.getPreferenceCount() == 0) {
            this.mPreferenceGroup.setVisible(true);
        }
        this.mPreferenceGroup.addPreference(preference);
    }

    public void onDeviceRemoved(Preference preference) {
        this.mPreferenceGroup.removePreference(preference);
        this.mPreferenceGroup.getPreferenceCount();
    }

    public void init(DashboardFragment fragment) {
        this.mOPBluetoothCarKitDeviceUpdater = new OPRecognizedBluetoothCarKitDeviceUpdater(fragment.getContext(), fragment, (DevicePreferenceCallback) this);
    }

    @VisibleForTesting
    public void setBluetoothDeviceUpdater(OPBluetoothCarKitDeviceUpdater bluetoothDeviceUpdater) {
        this.mOPBluetoothCarKitDeviceUpdater = bluetoothDeviceUpdater;
    }

    public void onBluetoothStateChanged(int bluetoothState) {
    }

    public void onScanningStateChanged(boolean started) {
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
    }

    public void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
    }

    public void onAudioModeChanged() {
    }
}
