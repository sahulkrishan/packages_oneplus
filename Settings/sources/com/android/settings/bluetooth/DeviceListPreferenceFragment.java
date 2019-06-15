package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.text.BidiFormatter;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.dashboard.RestrictedDashboardFragment;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.BluetoothDeviceFilter;
import com.android.settingslib.bluetooth.BluetoothDeviceFilter.Filter;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import java.util.WeakHashMap;

public abstract class DeviceListPreferenceFragment extends RestrictedDashboardFragment implements BluetoothCallback {
    private static final String BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY = "persist.bluetooth.showdeviceswithoutnames";
    private static final String KEY_BT_SCAN = "bt_scan";
    private static final String TAG = "DeviceListPreferenceFragment";
    @VisibleForTesting
    PreferenceGroup mDeviceListGroup;
    final WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference> mDevicePreferenceMap = new WeakHashMap();
    private Filter mFilter = BluetoothDeviceFilter.ALL_FILTER;
    LocalBluetoothAdapter mLocalAdapter;
    LocalBluetoothManager mLocalManager;
    @VisibleForTesting
    boolean mScanEnabled;
    BluetoothDevice mSelectedDevice;
    boolean mShowDevicesWithoutNames;

    public abstract String getDeviceListKey();

    public abstract void initPreferencesFromPreferenceScreen();

    DeviceListPreferenceFragment(String restrictedKey) {
        super(restrictedKey);
    }

    /* Access modifiers changed, original: final */
    public final void setFilter(Filter filter) {
        this.mFilter = filter;
    }

    /* Access modifiers changed, original: final */
    public final void setFilter(int filterType) {
        this.mFilter = BluetoothDeviceFilter.getFilter(filterType);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLocalManager = Utils.getLocalBtManager(getActivity());
        if (this.mLocalManager == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return;
        }
        this.mLocalAdapter = this.mLocalManager.getBluetoothAdapter();
        this.mShowDevicesWithoutNames = SystemProperties.getBoolean(BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY, true);
        initPreferencesFromPreferenceScreen();
        this.mDeviceListGroup = (PreferenceCategory) findPreference(getDeviceListKey());
    }

    public void onStart() {
        super.onStart();
        if (this.mLocalManager != null && !isUiRestricted()) {
            this.mLocalManager.setForegroundActivity(getActivity());
            this.mLocalManager.getEventManager().registerCallback(this);
        }
    }

    public void onStop() {
        super.onStop();
        if (this.mLocalManager != null && !isUiRestricted()) {
            removeAllDevices();
            this.mLocalManager.setForegroundActivity(null);
            this.mLocalManager.getEventManager().unregisterCallback(this);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void removeAllDevices() {
        this.mDevicePreferenceMap.clear();
        this.mDeviceListGroup.removeAll();
    }

    /* Access modifiers changed, original: 0000 */
    public void addCachedDevices() {
        for (CachedBluetoothDevice cachedDevice : this.mLocalManager.getCachedDeviceManager().getCachedDevicesCopy()) {
            onDeviceAdded(cachedDevice);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (KEY_BT_SCAN.equals(preference.getKey())) {
            this.mLocalAdapter.startScanning(true);
            return true;
        } else if (!(preference instanceof BluetoothDevicePreference)) {
            return super.onPreferenceTreeClick(preference);
        } else {
            BluetoothDevicePreference btPreference = (BluetoothDevicePreference) preference;
            this.mSelectedDevice = btPreference.getCachedDevice().getDevice();
            onDevicePreferenceClick(btPreference);
            return true;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        btPreference.onClicked();
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        if (this.mDevicePreferenceMap.get(cachedDevice) == null && this.mLocalAdapter.getBluetoothState() == 12 && this.mFilter.matches(cachedDevice.getDevice())) {
            createDevicePreference(cachedDevice);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void createDevicePreference(CachedBluetoothDevice cachedDevice) {
        if (this.mDeviceListGroup == null) {
            Log.w(TAG, "Trying to create a device preference before the list group/category exists!");
            return;
        }
        String key = cachedDevice.getDevice().getAddress();
        BluetoothDevicePreference preference = (BluetoothDevicePreference) getCachedPreference(key);
        if (preference == null) {
            preference = new BluetoothDevicePreference(getPrefContext(), cachedDevice, this.mShowDevicesWithoutNames);
            preference.setKey(key);
            this.mDeviceListGroup.addPreference(preference);
        } else {
            preference.rebind();
        }
        initDevicePreference(preference);
        this.mDevicePreferenceMap.put(cachedDevice, preference);
    }

    /* Access modifiers changed, original: 0000 */
    public void initDevicePreference(BluetoothDevicePreference preference) {
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateFooterPreference(Preference myDevicePreference) {
        myDevicePreference.setTitle(getString(R.string.bluetooth_footer_mac_message, new Object[]{BidiFormatter.getInstance().unicodeWrap(this.mLocalAdapter.getAddress())}));
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        BluetoothDevicePreference preference = (BluetoothDevicePreference) this.mDevicePreferenceMap.remove(cachedDevice);
        if (preference != null) {
            this.mDeviceListGroup.removePreference(preference);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void enableScanning() {
        this.mLocalAdapter.startScanning(true);
        this.mScanEnabled = true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void disableScanning() {
        this.mLocalAdapter.stopScanning();
        this.mScanEnabled = false;
    }

    public void onScanningStateChanged(boolean started) {
        if (!started && this.mScanEnabled) {
            this.mLocalAdapter.startScanning(true);
        }
    }

    public void onBluetoothStateChanged(int bluetoothState) {
    }

    public void addDeviceCategory(PreferenceGroup preferenceGroup, int titleId, Filter filter, boolean addCachedDevices) {
        cacheRemoveAllPrefs(preferenceGroup);
        preferenceGroup.setTitle(titleId);
        this.mDeviceListGroup = preferenceGroup;
        setFilter(filter);
        if (addCachedDevices) {
            addCachedDevices();
        }
        preferenceGroup.setEnabled(true);
        removeCachedPrefs(preferenceGroup);
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
    }

    public void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
    }

    public void onAudioModeChanged() {
    }

    public boolean shouldShowDevicesWithoutNames() {
        return this.mShowDevicesWithoutNames;
    }
}
