package com.android.settings.connecteddevice;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.bluetooth.BluetoothDeviceUpdater;
import com.android.settings.bluetooth.SavedBluetoothTwsDeviceUpdater;
import com.android.settings.connecteddevice.dock.DockUpdater;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class SavedTwsDeviceGroupController extends BasePreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart, OnStop, DevicePreferenceCallback {
    private static final String KEY = "saved_tws_device_list";
    private BluetoothDeviceUpdater mBluetoothDeviceUpdater;
    PreferenceGroup mPreferenceGroup;
    private DockUpdater mSavedDockUpdater;

    public SavedTwsDeviceGroupController(Context context) {
        super(context, KEY);
        this.mSavedDockUpdater = FeatureFactory.getFactory(context).getDockUpdaterFeatureProvider().getSavedDockUpdater(context, this);
    }

    public void onStart() {
        this.mBluetoothDeviceUpdater.registerCallback();
        this.mSavedDockUpdater.registerCallback();
    }

    public void onStop() {
        this.mBluetoothDeviceUpdater.unregisterCallback();
        this.mSavedDockUpdater.unregisterCallback();
    }

    public void displayPreference(PreferenceScreen screen) {
        if (isAvailable()) {
            this.mPreferenceGroup = (PreferenceGroup) screen.findPreference(KEY);
            this.mPreferenceGroup.setVisible(false);
            this.mBluetoothDeviceUpdater.setPrefContext(screen.getContext());
            this.mBluetoothDeviceUpdater.forceUpdate();
            this.mSavedDockUpdater.forceUpdate();
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
        if (this.mPreferenceGroup.getPreferenceCount() == 0) {
            this.mPreferenceGroup.setVisible(false);
        }
    }

    public void init(DashboardFragment fragment) {
        this.mBluetoothDeviceUpdater = new SavedBluetoothTwsDeviceUpdater(fragment.getContext(), fragment, (DevicePreferenceCallback) this);
    }

    public void setBluetoothDeviceUpdater(BluetoothDeviceUpdater bluetoothDeviceUpdater) {
        this.mBluetoothDeviceUpdater = bluetoothDeviceUpdater;
    }

    public void setSavedDockUpdater(DockUpdater savedDockUpdater) {
        this.mSavedDockUpdater = savedDockUpdater;
    }
}
