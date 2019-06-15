package com.android.settings.bluetooth;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import com.android.settings.R;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;

public class LocalDeviceNameDialogFragment extends BluetoothNameDialogFragment {
    public static final String TAG = "LocalAdapterName";
    private LocalBluetoothAdapter mLocalAdapter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED".equals(action) || ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action) && intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE) == 12)) {
                LocalDeviceNameDialogFragment.this.updateDeviceName();
            }
        }
    };

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

    public /* bridge */ /* synthetic */ void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    public /* bridge */ /* synthetic */ void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        super.onTextChanged(charSequence, i, i2, i3);
    }

    public static LocalDeviceNameDialogFragment newInstance() {
        return new LocalDeviceNameDialogFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLocalAdapter = Utils.getLocalBtManager(getActivity()).getBluetoothAdapter();
    }

    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        filter.addAction("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
        getActivity().registerReceiver(this.mReceiver, filter);
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
    }

    public int getMetricsCategory() {
        return 538;
    }

    /* Access modifiers changed, original: protected */
    public int getDialogTitle() {
        return R.string.bluetooth_rename_device;
    }

    /* Access modifiers changed, original: protected */
    public String getDeviceName() {
        if (this.mLocalAdapter == null || !this.mLocalAdapter.isEnabled()) {
            return null;
        }
        return this.mLocalAdapter.getName();
    }

    /* Access modifiers changed, original: protected */
    public void setDeviceName(String deviceName) {
        this.mLocalAdapter.setName(deviceName);
    }
}
