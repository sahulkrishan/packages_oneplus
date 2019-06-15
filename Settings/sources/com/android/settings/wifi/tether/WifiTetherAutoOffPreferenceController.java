package com.android.settings.wifi.tether;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.OpFeatures;
import com.android.settings.core.BasePreferenceController;

public class WifiTetherAutoOffPreferenceController extends BasePreferenceController implements OnPreferenceChangeListener {
    public WifiTetherAutoOffPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public int getAvailabilityStatus() {
        return OpFeatures.isSupport(new int[]{85}) ? 2 : 0;
    }

    public void updateState(Preference preference) {
        boolean z = true;
        if (Global.getInt(this.mContext.getContentResolver(), "soft_ap_timeout_enabled", 1) == 0) {
            z = false;
        }
        ((SwitchPreference) preference).setChecked(z);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "soft_ap_timeout_enabled", ((Boolean) newValue).booleanValue());
        return true;
    }
}
