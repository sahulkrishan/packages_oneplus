package com.android.settings.bluetooth;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.PointerIconCompat;
import android.text.Editable;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class RemoteDeviceNameDialogFragment extends BluetoothNameDialogFragment {
    private static final String KEY_CACHED_DEVICE_ADDRESS = "cached_device";
    public static final String TAG = "RemoteDeviceName";
    private CachedBluetoothDevice mDevice;

    public /* bridge */ /* synthetic */ void afterTextChanged(Editable editable) {
        super.afterTextChanged(editable);
    }

    public /* bridge */ /* synthetic */ void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        super.beforeTextChanged(charSequence, i, i2, i3);
    }

    public /* bridge */ /* synthetic */ void onConfigurationChanged(Configuration configuration, CharSequence charSequence) {
        super.onConfigurationChanged(configuration, charSequence);
    }

    public /* bridge */ /* synthetic */ Dialog onCreateDialog(Bundle bundle) {
        return super.onCreateDialog(bundle);
    }

    public /* bridge */ /* synthetic */ void onDestroy() {
        super.onDestroy();
    }

    public /* bridge */ /* synthetic */ void onResume() {
        super.onResume();
    }

    public /* bridge */ /* synthetic */ void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    public /* bridge */ /* synthetic */ void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        super.onTextChanged(charSequence, i, i2, i3);
    }

    public static RemoteDeviceNameDialogFragment newInstance(CachedBluetoothDevice device) {
        Bundle args = new Bundle(1);
        args.putString(KEY_CACHED_DEVICE_ADDRESS, device.getDevice().getAddress());
        RemoteDeviceNameDialogFragment fragment = new RemoteDeviceNameDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public CachedBluetoothDevice getDevice(Context context) {
        String deviceAddress = getArguments().getString(KEY_CACHED_DEVICE_ADDRESS);
        LocalBluetoothManager manager = Utils.getLocalBtManager(context);
        return manager.getCachedDeviceManager().findDevice(manager.getBluetoothAdapter().getRemoteDevice(deviceAddress));
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mDevice = getDevice(context);
    }

    public int getMetricsCategory() {
        return PointerIconCompat.TYPE_VERTICAL_DOUBLE_ARROW;
    }

    /* Access modifiers changed, original: protected */
    public int getDialogTitle() {
        return R.string.bluetooth_device_name;
    }

    /* Access modifiers changed, original: protected */
    public String getDeviceName() {
        if (this.mDevice != null) {
            return this.mDevice.getName();
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public void setDeviceName(String deviceName) {
        if (this.mDevice != null) {
            this.mDevice.setName(deviceName);
        }
    }
}
