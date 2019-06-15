package com.android.settings.bluetooth;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceScreen;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.Utils;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class BluetoothDetailsHeaderController extends BluetoothDetailsController {
    private static final String KEY_DEVICE_HEADER = "bluetooth_device_header";
    private CachedBluetoothDeviceManager mDeviceManager = this.mLocalManager.getCachedDeviceManager();
    private EntityHeaderController mHeaderController;
    private LocalBluetoothManager mLocalManager;

    public BluetoothDetailsHeaderController(Context context, PreferenceFragment fragment, CachedBluetoothDevice device, Lifecycle lifecycle, LocalBluetoothManager bluetoothManager) {
        super(context, fragment, device, lifecycle);
        this.mLocalManager = bluetoothManager;
    }

    /* Access modifiers changed, original: protected */
    public void init(PreferenceScreen screen) {
        LayoutPreference headerPreference = (LayoutPreference) screen.findPreference(KEY_DEVICE_HEADER);
        this.mHeaderController = EntityHeaderController.newInstance(this.mFragment.getActivity(), this.mFragment, headerPreference.findViewById(R.id.entity_header));
        screen.addPreference(headerPreference);
    }

    /* Access modifiers changed, original: protected */
    public void setHeaderProperties() {
        Pair<Drawable, String> pair = Utils.getBtClassDrawableWithDescription(this.mContext, this.mCachedDevice, this.mContext.getResources().getFraction(R.fraction.bt_battery_scale_fraction, 1, 1));
        CharSequence summaryText = this.mCachedDevice.getConnectionSummary();
        CharSequence pairDeviceSummary = this.mDeviceManager.getHearingAidPairDeviceSummary(this.mCachedDevice);
        if (pairDeviceSummary != null) {
            this.mHeaderController.setSecondSummary(pairDeviceSummary);
        }
        this.mHeaderController.setLabel(this.mCachedDevice.getName());
        this.mHeaderController.setIcon((Drawable) pair.first);
        this.mHeaderController.setIconContentDescription((String) pair.second);
        this.mHeaderController.setSummary(summaryText);
    }

    /* Access modifiers changed, original: protected */
    public void refresh() {
        setHeaderProperties();
        this.mHeaderController.done(this.mFragment.getActivity(), true);
    }

    public String getPreferenceKey() {
        return KEY_DEVICE_HEADER;
    }
}
