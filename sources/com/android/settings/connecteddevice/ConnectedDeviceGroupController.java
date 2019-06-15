package com.android.settings.connecteddevice;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.bluetooth.BluetoothDeviceUpdater;
import com.android.settings.bluetooth.ConnectedBluetoothDeviceUpdater;
import com.android.settings.connecteddevice.dock.DockUpdater;
import com.android.settings.connecteddevice.usb.ConnectedUsbDeviceUpdater;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class ConnectedDeviceGroupController extends BasePreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart, OnStop, DevicePreferenceCallback {
    private static final String KEY = "connected_device_list";
    private BluetoothDeviceUpdater mBluetoothDeviceUpdater;
    private DockUpdater mConnectedDockUpdater;
    private ConnectedUsbDeviceUpdater mConnectedUsbDeviceUpdater;
    @VisibleForTesting
    PreferenceGroup mPreferenceGroup;

    public ConnectedDeviceGroupController(Context context) {
        super(context, KEY);
    }

    public void onStart() {
        this.mBluetoothDeviceUpdater.registerCallback();
        this.mConnectedUsbDeviceUpdater.registerCallback();
        this.mConnectedDockUpdater.registerCallback();
    }

    public void onStop() {
        this.mConnectedUsbDeviceUpdater.unregisterCallback();
        this.mBluetoothDeviceUpdater.unregisterCallback();
        this.mConnectedDockUpdater.unregisterCallback();
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mPreferenceGroup = (PreferenceGroup) screen.findPreference(KEY);
            this.mPreferenceGroup.setVisible(false);
            this.mBluetoothDeviceUpdater.setPrefContext(screen.getContext());
            this.mBluetoothDeviceUpdater.forceUpdate();
            this.mConnectedUsbDeviceUpdater.initUsbPreference(screen.getContext());
            this.mConnectedDockUpdater.forceUpdate();
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

    @VisibleForTesting
    public void init(BluetoothDeviceUpdater bluetoothDeviceUpdater, ConnectedUsbDeviceUpdater connectedUsbDeviceUpdater, DockUpdater connectedDockUpdater) {
        this.mBluetoothDeviceUpdater = bluetoothDeviceUpdater;
        this.mConnectedUsbDeviceUpdater = connectedUsbDeviceUpdater;
        this.mConnectedDockUpdater = connectedDockUpdater;
    }

    public void init(DashboardFragment fragment) {
        Context context = fragment.getContext();
        init(new ConnectedBluetoothDeviceUpdater(context, fragment, (DevicePreferenceCallback) this), new ConnectedUsbDeviceUpdater(context, fragment, this), FeatureFactory.getFactory(context).getDockUpdaterFeatureProvider().getConnectedDockUpdater(context, this));
    }
}
