package com.android.settings.location;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class WifiScanningPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_WIFI_SCAN_ALWAYS_AVAILABLE = "wifi_always_scanning";

    public WifiScanningPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_WIFI_SCAN_ALWAYS_AVAILABLE;
    }

    public void updateState(Preference preference) {
        SwitchPreference switchPreference = (SwitchPreference) preference;
        boolean z = true;
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) != 1) {
            z = false;
        }
        switchPreference.setChecked(z);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_WIFI_SCAN_ALWAYS_AVAILABLE.equals(preference.getKey())) {
            return false;
        }
        Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", ((SwitchPreference) preference).isChecked());
        return true;
    }
}
