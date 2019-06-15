package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class AppsNotRespondingPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;
    private static final String SHOW_ALL_ANRS_KEY = "show_all_anrs";

    public AppsNotRespondingPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return SHOW_ALL_ANRS_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), "anr_show_background", ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Secure.getInt(this.mContext.getContentResolver(), "anr_show_background", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Secure.putInt(this.mContext.getContentResolver(), "anr_show_background", 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
