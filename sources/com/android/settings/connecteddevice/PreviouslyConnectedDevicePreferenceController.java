package com.android.settings.connecteddevice;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.bluetooth.BluetoothDeviceUpdater;
import com.android.settings.bluetooth.SavedBluetoothDeviceUpdater;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class PreviouslyConnectedDevicePreferenceController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop, DevicePreferenceCallback {
    private BluetoothDeviceUpdater mBluetoothDeviceUpdater;
    private Preference mPreference;
    private int mPreferenceSize;

    public PreviouslyConnectedDevicePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
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
            this.mBluetoothDeviceUpdater.setPrefContext(screen.getContext());
        }
    }

    public void onStart() {
        this.mBluetoothDeviceUpdater.registerCallback();
        updatePreferenceOnSizeChanged();
    }

    public void onStop() {
        this.mBluetoothDeviceUpdater.unregisterCallback();
    }

    public void init(DashboardFragment fragment) {
        this.mBluetoothDeviceUpdater = new SavedBluetoothDeviceUpdater(fragment.getContext(), fragment, (DevicePreferenceCallback) this);
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
    public void setBluetoothDeviceUpdater(BluetoothDeviceUpdater bluetoothDeviceUpdater) {
        this.mBluetoothDeviceUpdater = bluetoothDeviceUpdater;
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
            this.mPreference.setEnabled(this.mPreferenceSize != 0);
        }
    }
}
