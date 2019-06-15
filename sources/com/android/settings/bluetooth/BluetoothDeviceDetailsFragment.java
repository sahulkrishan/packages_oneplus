package com.android.settings.bluetooth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.PointerIconCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.dashboard.RestrictedDashboardFragment;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceDetailsFragment extends RestrictedDashboardFragment {
    @VisibleForTesting
    static int EDIT_DEVICE_NAME_ITEM_ID = 1;
    public static final String KEY_DEVICE_ADDRESS = "device_address";
    private static final String TAG = "BTDeviceDetailsFrg";
    @VisibleForTesting
    static TestDataFactory sTestDataFactory;
    private CachedBluetoothDevice mCachedDevice;
    private String mDeviceAddress;
    private LocalBluetoothManager mManager;

    @VisibleForTesting
    interface TestDataFactory {
        CachedBluetoothDevice getDevice(String str);

        LocalBluetoothManager getManager(Context context);
    }

    public BluetoothDeviceDetailsFragment() {
        super("no_config_bluetooth");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public LocalBluetoothManager getLocalBluetoothManager(Context context) {
        if (sTestDataFactory != null) {
            return sTestDataFactory.getManager(context);
        }
        return Utils.getLocalBtManager(context);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public CachedBluetoothDevice getCachedDevice(String deviceAddress) {
        if (sTestDataFactory != null) {
            return sTestDataFactory.getDevice(deviceAddress);
        }
        return this.mManager.getCachedDeviceManager().findDevice(this.mManager.getBluetoothAdapter().getRemoteDevice(deviceAddress));
    }

    public static BluetoothDeviceDetailsFragment newInstance(String deviceAddress) {
        Bundle args = new Bundle(1);
        args.putString("device_address", deviceAddress);
        BluetoothDeviceDetailsFragment fragment = new BluetoothDeviceDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(Context context) {
        this.mDeviceAddress = getArguments().getString("device_address");
        this.mManager = getLocalBluetoothManager(context);
        this.mCachedDevice = getCachedDevice(this.mDeviceAddress);
        super.onAttach(context);
    }

    public int getMetricsCategory() {
        return PointerIconCompat.TYPE_VERTICAL_TEXT;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.bluetooth_device_details_fragment;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(0, EDIT_DEVICE_NAME_ITEM_ID, 0, R.string.bluetooth_rename_button);
        item.setIcon(R.drawable.ic_menu_edit);
        item.setShowAsAction(2);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != EDIT_DEVICE_NAME_ITEM_ID) {
            return super.onOptionsItemSelected(menuItem);
        }
        RemoteDeviceNameDialogFragment.newInstance(this.mCachedDevice).show(getFragmentManager(), RemoteDeviceNameDialogFragment.TAG);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        ArrayList<AbstractPreferenceController> controllers = new ArrayList();
        if (this.mCachedDevice != null) {
            Lifecycle lifecycle = getLifecycle();
            controllers.add(new BluetoothDetailsHeaderController(context, this, this.mCachedDevice, lifecycle, this.mManager));
            controllers.add(new BluetoothDetailsButtonsController(context, this, this.mCachedDevice, lifecycle));
            controllers.add(new BluetoothDetailsProfilesController(context, this, this.mManager, this.mCachedDevice, lifecycle));
            controllers.add(new BluetoothDetailsMacAddressController(context, this, this.mCachedDevice, lifecycle));
        }
        return controllers;
    }
}
