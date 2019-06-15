package com.android.settingslib.deviceinfo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settingslib.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public abstract class AbstractBluetoothAddressPreferenceController extends AbstractConnectivityPreferenceController {
    private static final String[] CONNECTIVITY_INTENTS = new String[]{"android.bluetooth.adapter.action.STATE_CHANGED"};
    @VisibleForTesting
    static final String KEY_BT_ADDRESS = "bt_address";
    private Preference mBtAddress;

    public AbstractBluetoothAddressPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
    }

    public boolean isAvailable() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public String getPreferenceKey() {
        return KEY_BT_ADDRESS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mBtAddress = screen.findPreference(KEY_BT_ADDRESS);
        updateConnectivity();
    }

    /* Access modifiers changed, original: protected */
    public String[] getConnectivityIntents() {
        return CONNECTIVITY_INTENTS;
    }

    /* Access modifiers changed, original: protected */
    @SuppressLint({"HardwareIds"})
    public void updateConnectivity() {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth != null && this.mBtAddress != null) {
            String address = bluetooth.isEnabled() ? bluetooth.getAddress() : null;
            if (TextUtils.isEmpty(address)) {
                this.mBtAddress.setSummary(R.string.status_unavailable);
            } else {
                this.mBtAddress.setSummary(address.toLowerCase());
            }
        }
    }
}
