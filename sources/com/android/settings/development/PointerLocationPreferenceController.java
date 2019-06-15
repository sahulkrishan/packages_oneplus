package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class PointerLocationPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String POINTER_LOCATION_KEY = "pointer_location";
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;

    public PointerLocationPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return POINTER_LOCATION_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        System.putInt(this.mContext.getContentResolver(), POINTER_LOCATION_KEY, ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (System.getInt(this.mContext.getContentResolver(), POINTER_LOCATION_KEY, 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        System.putInt(this.mContext.getContentResolver(), POINTER_LOCATION_KEY, 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
