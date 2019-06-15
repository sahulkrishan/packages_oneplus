package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class WifiDisplayCertificationPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;
    private static final String WIFI_DISPLAY_CERTIFICATION_KEY = "wifi_display_certification";

    public WifiDisplayCertificationPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return WIFI_DISPLAY_CERTIFICATION_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_certification_on", ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_display_certification_on", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Global.putInt(this.mContext.getContentResolver(), "wifi_display_certification_on", 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
