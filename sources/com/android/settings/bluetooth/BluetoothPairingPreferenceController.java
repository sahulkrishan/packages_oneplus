package com.android.settings.bluetooth;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;

public class BluetoothPairingPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    public static final String KEY_PAIRING = "pref_bt_pairing";
    private static final String TAG = "BluetoothPairingPrefCtrl";
    private DashboardFragment mFragment;
    private Preference mPreference;

    public BluetoothPairingPreferenceController(Context context, DashboardFragment fragment) {
        super(context);
        this.mFragment = fragment;
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_PAIRING;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_PAIRING.equals(preference.getKey())) {
            return false;
        }
        new SubSettingLauncher(this.mContext).setDestination(BluetoothPairingDetail.class.getName()).setTitle((int) R.string.bluetooth_pairing_page_title).setSourceMetricsCategory(this.mFragment.getMetricsCategory()).launch();
        return true;
    }

    public Preference createBluetoothPairingPreference(int order) {
        this.mPreference = new Preference(this.mFragment.getPreferenceScreen().getContext());
        this.mPreference.setKey(KEY_PAIRING);
        this.mPreference.setIcon((int) R.drawable.ic_menu_add);
        this.mPreference.setOrder(order);
        this.mPreference.setTitle((int) R.string.bluetooth_pairing_pref_title);
        return this.mPreference;
    }
}
