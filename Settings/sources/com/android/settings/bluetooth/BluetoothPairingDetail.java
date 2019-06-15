package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.PointerIconCompat;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.search.Indexable;
import com.android.settingslib.bluetooth.BluetoothDeviceFilter;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.widget.FooterPreference;
import com.oneplus.settings.SettingsBaseApplication;

public class BluetoothPairingDetail extends DeviceListPreferenceFragment implements Indexable {
    @VisibleForTesting
    static final String KEY_AVAIL_DEVICES = "available_devices";
    @VisibleForTesting
    static final String KEY_FOOTER_PREF = "footer_preference";
    private static final String TAG = "BluetoothPairingDetail";
    @VisibleForTesting
    AlwaysDiscoverable mAlwaysDiscoverable;
    @VisibleForTesting
    BluetoothProgressCategory mAvailableDevicesCategory;
    @VisibleForTesting
    FooterPreference mFooterPreference;
    private boolean mInitialScanStarted;

    public BluetoothPairingDetail() {
        super("no_config_bluetooth");
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mInitialScanStarted = false;
        this.mAlwaysDiscoverable = new AlwaysDiscoverable(getContext(), this.mLocalAdapter);
    }

    public void onStart() {
        super.onStart();
        if (this.mLocalManager == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return;
        }
        updateBluetooth();
        this.mAvailableDevicesCategory.setProgress(this.mLocalAdapter.isDiscovering());
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((BluetoothDeviceRenamePreferenceController) use(BluetoothDeviceRenamePreferenceController.class)).setFragment(this);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateBluetooth() {
        if (this.mLocalAdapter.isEnabled()) {
            updateContent(this.mLocalAdapter.getBluetoothState());
        } else {
            this.mLocalAdapter.enable();
        }
    }

    public void onStop() {
        super.onStop();
        if (this.mLocalManager == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return;
        }
        this.mAlwaysDiscoverable.stop();
        disableScanning();
    }

    /* Access modifiers changed, original: 0000 */
    public void initPreferencesFromPreferenceScreen() {
        this.mAvailableDevicesCategory = (BluetoothProgressCategory) findPreference(KEY_AVAIL_DEVICES);
        this.mFooterPreference = (FooterPreference) findPreference("footer_preference");
        this.mFooterPreference.setSelectable(false);
    }

    public int getMetricsCategory() {
        return PointerIconCompat.TYPE_ZOOM_IN;
    }

    /* Access modifiers changed, original: 0000 */
    public void enableScanning() {
        if (!this.mInitialScanStarted) {
            if (this.mAvailableDevicesCategory != null) {
                removeAllDevices();
            }
            this.mLocalManager.getCachedDeviceManager().clearNonBondedDevices();
            this.mInitialScanStarted = true;
        }
        super.enableScanning();
    }

    /* Access modifiers changed, original: 0000 */
    public void onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        disableScanning();
        super.onDevicePreferenceClick(btPreference);
    }

    public void onScanningStateChanged(boolean started) {
        super.onScanningStateChanged(started);
        this.mAvailableDevicesCategory.setProgress(started | this.mScanEnabled);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateContent(int bluetoothState) {
        if (bluetoothState == 10) {
            finish();
        } else if (bluetoothState == 12) {
            this.mDevicePreferenceMap.clear();
            this.mLocalAdapter.setBluetoothEnabled(true);
            addDeviceCategory(this.mAvailableDevicesCategory, R.string.bluetooth_preference_found_media_devices, BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER, this.mInitialScanStarted);
            updateFooterPreference(this.mFooterPreference);
            this.mAlwaysDiscoverable.start();
            enableScanning();
            this.mLocalAdapter.setName(System.getString(SettingsBaseApplication.mApplication.getContentResolver(), "oem_oneplus_devicename"));
        }
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        super.onBluetoothStateChanged(bluetoothState);
        updateContent(bluetoothState);
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        if (bondState == 12) {
            finish();
            return;
        }
        if (!(this.mSelectedDevice == null || cachedDevice == null)) {
            BluetoothDevice device = cachedDevice.getDevice();
            if (device != null && this.mSelectedDevice.equals(device) && bondState == 10) {
                enableScanning();
            }
        }
    }

    public int getHelpResource() {
        return R.string.help_url_bluetooth;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.bluetooth_pairing_detail;
    }

    public String getDeviceListKey() {
        return KEY_AVAIL_DEVICES;
    }
}
