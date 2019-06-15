package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.view.Menu;
import android.view.MenuInflater;
import com.android.settings.R;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPUtils;
import java.util.List;

public final class DevicePickerFragment extends DeviceListPreferenceFragment {
    private static final String KEY_BT_DEVICE_LIST = "bt_device_list";
    private static final String TAG = "DevicePickerFragment";
    @VisibleForTesting
    BluetoothProgressCategory mAvailableDevicesCategory;
    private String mLaunchClass;
    private String mLaunchPackage;
    private boolean mNeedAuth;
    private boolean mScanAllowed;

    public DevicePickerFragment() {
        super(null);
    }

    /* Access modifiers changed, original: 0000 */
    public void initPreferencesFromPreferenceScreen() {
        Intent intent = getActivity().getIntent();
        this.mNeedAuth = intent.getBooleanExtra("android.bluetooth.devicepicker.extra.NEED_AUTH", false);
        setFilter(intent.getIntExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 0));
        this.mLaunchPackage = intent.getStringExtra("android.bluetooth.devicepicker.extra.LAUNCH_PACKAGE");
        this.mLaunchClass = intent.getStringExtra("android.bluetooth.devicepicker.extra.DEVICE_PICKER_LAUNCH_CLASS");
        this.mAvailableDevicesCategory = (BluetoothProgressCategory) findPreference(KEY_BT_DEVICE_LIST);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public int getMetricsCategory() {
        return 25;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (OPUtils.isWhiteModeOn(getContentResolver())) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        getActivity().setTitle(getString(R.string.device_picker));
        this.mScanAllowed = ((UserManager) getSystemService("user")).hasUserRestriction("no_config_bluetooth") ^ 1;
        setHasOptionsMenu(true);
    }

    public void onStart() {
        super.onStart();
        addCachedDevices();
        this.mSelectedDevice = null;
        if (this.mScanAllowed) {
            enableScanning();
            this.mAvailableDevicesCategory.setProgress(this.mLocalAdapter.isDiscovering());
        }
    }

    public void onStop() {
        disableScanning();
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mSelectedDevice == null) {
            sendDevicePickedIntent(null);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        disableScanning();
        LocalBluetoothPreferences.persistSelectedDeviceInPicker(getActivity(), this.mSelectedDevice.getAddress());
        if (btPreference.getCachedDevice().getBondState() == 12 || !this.mNeedAuth) {
            sendDevicePickedIntent(this.mSelectedDevice);
            finish();
            return;
        }
        super.onDevicePreferenceClick(btPreference);
    }

    public void onScanningStateChanged(boolean started) {
        super.onScanningStateChanged(started);
        this.mAvailableDevicesCategory.setProgress(started | this.mScanEnabled);
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        BluetoothDevice device = cachedDevice.getDevice();
        if (device.equals(this.mSelectedDevice)) {
            if (bondState == 12) {
                sendDevicePickedIntent(device);
                finish();
            } else if (bondState == 10) {
                enableScanning();
            }
        }
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        super.onBluetoothStateChanged(bluetoothState);
        if (bluetoothState == 12) {
            enableScanning();
        }
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.device_picker;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return null;
    }

    public String getDeviceListKey() {
        return KEY_BT_DEVICE_LIST;
    }

    private void sendDevicePickedIntent(BluetoothDevice device) {
        Intent intent = new Intent("android.bluetooth.devicepicker.action.DEVICE_SELECTED");
        intent.putExtra("android.bluetooth.device.extra.DEVICE", device);
        if (!(this.mLaunchPackage == null || this.mLaunchClass == null)) {
            intent.setClassName(this.mLaunchPackage, this.mLaunchClass);
        }
        getActivity().sendBroadcast(intent);
    }
}
