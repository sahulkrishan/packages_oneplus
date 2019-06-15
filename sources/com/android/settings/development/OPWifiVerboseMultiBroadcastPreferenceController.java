package com.android.settings.development;

import android.content.Context;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class OPWifiVerboseMultiBroadcastPreferenceController extends DeveloperOptionsPreferenceController {
    private static final String KEY_WIFI_VERBOSE_MULTI_BROADCAST = "op_wifi_verbose_multi_broadcast";
    private static final String OP_WIFI_VERBOSE_MULTI_BROADCAST = "op_enable_wifi_multi_broadcast";

    public OPWifiVerboseMultiBroadcastPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return KEY_WIFI_VERBOSE_MULTI_BROADCAST;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (this.mPreference != null && !isAdminUser()) {
            this.mPreference.setEnabled(false);
        }
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_WIFI_VERBOSE_MULTI_BROADCAST) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        Global.putInt(this.mContext.getContentResolver(), OP_WIFI_VERBOSE_MULTI_BROADCAST, ((SwitchPreference) preference).isChecked());
        return true;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference enableSwitch = (SwitchPreference) preference;
            boolean z = true;
            if (Global.getInt(this.mContext.getContentResolver(), OP_WIFI_VERBOSE_MULTI_BROADCAST, 1) != 1) {
                z = false;
            }
            enableSwitch.setChecked(z);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isAdminUser() {
        return ((UserManager) this.mContext.getSystemService("user")).isAdminUser();
    }
}
