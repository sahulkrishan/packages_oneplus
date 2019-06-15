package com.android.settings.wifi;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class CellularFallbackPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_CELLULAR_FALLBACK = "wifi_cellular_data_fallback";

    public CellularFallbackPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return avoidBadWifiConfig() ^ 1;
    }

    public String getPreferenceKey() {
        return KEY_CELLULAR_FALLBACK;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_CELLULAR_FALLBACK) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        Global.putString(this.mContext.getContentResolver(), "network_avoid_bad_wifi", ((SwitchPreference) preference).isChecked() ? "1" : null);
        return true;
    }

    public void updateState(Preference preference) {
        boolean currentSetting = avoidBadWifiCurrentSettings();
        if (preference != null) {
            ((SwitchPreference) preference).setChecked(currentSetting);
        }
    }

    private boolean avoidBadWifiConfig() {
        return this.mContext.getResources().getInteger(17694826) == 1;
    }

    private boolean avoidBadWifiCurrentSettings() {
        return "1".equals(Global.getString(this.mContext.getContentResolver(), "network_avoid_bad_wifi"));
    }
}
