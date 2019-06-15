package com.android.settings.connecteddevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.bluetooth.OPBluetoothCarKitDeviceUpdater;
import com.android.settings.bluetooth.OPPairedBluetoothDeviceUpdater;
import com.android.settings.bluetooth.OPRecognizedBluetoothCarKitDeviceUpdater;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class OPRecognizedBluetoothCarKitNoDevicesPreferenceController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop, DevicePreferenceCallback {
    static final String KEY_RECOGNIZED_BLUETOOTH_CAR_KITS_NO_DEVICES = "recognized_bluetooth_car_kits_no_devices";
    private OPBluetoothCarKitDeviceUpdater mOPBluetoothCarKitDeviceUpdater;
    private Preference mPreference;
    private int mPreferenceSize;
    private BroadcastReceiver mStatusReceive = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Object obj = (action.hashCode() == 140676821 && action.equals("oneplus.action.intent.UpdateBluetoothCarkitDevice")) ? null : -1;
            if (obj == null) {
                OPRecognizedBluetoothCarKitNoDevicesPreferenceController.this.mOPBluetoothCarKitDeviceUpdater.forceUpdate();
            }
        }
    };

    public OPRecognizedBluetoothCarKitNoDevicesPreferenceController(Context context, String preferenceKey) {
        super(context, KEY_RECOGNIZED_BLUETOOTH_CAR_KITS_NO_DEVICES);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            return 0;
        }
        return 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mPreference = screen.findPreference(getPreferenceKey());
            this.mOPBluetoothCarKitDeviceUpdater.setPrefContext(screen.getContext());
        }
    }

    public void onStart() {
        this.mOPBluetoothCarKitDeviceUpdater.registerCallback();
        updatePreferenceOnSizeChanged();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("oneplus.action.intent.UpdateBluetoothCarkitDevice");
        this.mContext.registerReceiver(this.mStatusReceive, intentFilter);
    }

    public void onStop() {
        this.mOPBluetoothCarKitDeviceUpdater.unregisterCallback();
        this.mContext.unregisterReceiver(this.mStatusReceive);
    }

    public void init(DashboardFragment fragment) {
        this.mOPBluetoothCarKitDeviceUpdater = new OPRecognizedBluetoothCarKitDeviceUpdater(fragment.getContext(), fragment, (DevicePreferenceCallback) this);
    }

    public void onDeviceAdded(Preference preference) {
        this.mPreferenceSize++;
        updatePreferenceOnSizeChanged();
    }

    public void onDeviceRemoved(Preference preference) {
        this.mPreferenceSize--;
        updatePreferenceOnSizeChanged();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setBluetoothDeviceUpdater(OPPairedBluetoothDeviceUpdater oppairedBluetoothDeviceUpdater) {
        this.mOPBluetoothCarKitDeviceUpdater = oppairedBluetoothDeviceUpdater;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setPreferenceSize(int size) {
        this.mPreferenceSize = size;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setPreference(Preference preference) {
        this.mPreference = preference;
    }

    private void updatePreferenceOnSizeChanged() {
        if (isAvailable()) {
            this.mPreference.setVisible(this.mPreferenceSize == 0);
        }
    }
}
