package com.android.settings.network;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class ProxyPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_PROXY_SETTINGS = "proxy_settings";

    public ProxyPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return false;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(KEY_PROXY_SETTINGS);
        if (pref != null) {
            pref.setEnabled(((DevicePolicyManager) this.mContext.getSystemService("device_policy")).getGlobalProxyAdmin() == null);
        }
    }

    public String getPreferenceKey() {
        return KEY_PROXY_SETTINGS;
    }
}
