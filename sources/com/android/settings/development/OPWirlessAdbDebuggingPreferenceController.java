package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.oneplus.settings.SettingsBaseApplication;

public class OPWirlessAdbDebuggingPreferenceController extends DeveloperOptionsPreferenceController {
    private static final String KEY_WIRELESS_ADB_DEBUGGING = "op_wireless_adb_debugging";
    private static final String OP_WIRELESS_ADB_DEBUGGING_PROPERTY = "service.adb.tcp.port";
    private static final String OP_WIRELESS_ADB_DEBUGGING_PROPERTY_DISENABLE = "-1";
    private static final String OP_WIRELESS_ADB_DEBUGGING_PROPERTY_ENABLE = "5555";

    public OPWirlessAdbDebuggingPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return KEY_WIRELESS_ADB_DEBUGGING;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (this.mPreference != null && !isAdminUser()) {
            this.mPreference.setEnabled(false);
        }
    }

    public boolean isAvailable() {
        return SettingsBaseApplication.mApplication.getPackageManager().hasSystemFeature("oem.service.adb.tcp.port.support");
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_WIRELESS_ADB_DEBUGGING) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        String str;
        String str2 = OP_WIRELESS_ADB_DEBUGGING_PROPERTY;
        if (((SwitchPreference) preference).isChecked()) {
            str = OP_WIRELESS_ADB_DEBUGGING_PROPERTY_ENABLE;
        } else {
            str = OP_WIRELESS_ADB_DEBUGGING_PROPERTY_DISENABLE;
        }
        SystemProperties.set(str2, str);
        return true;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            ((SwitchPreference) preference).setChecked(OP_WIRELESS_ADB_DEBUGGING_PROPERTY_ENABLE.equals(SystemProperties.get(OP_WIRELESS_ADB_DEBUGGING_PROPERTY)));
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isAdminUser() {
        return ((UserManager) this.mContext.getSystemService("user")).isAdminUser();
    }
}
