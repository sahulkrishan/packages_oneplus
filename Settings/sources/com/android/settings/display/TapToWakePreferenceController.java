package com.android.settings.display;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class TapToWakePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_TAP_TO_WAKE = "tap_to_wake";

    public TapToWakePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return KEY_TAP_TO_WAKE;
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(17957041);
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) preference;
        if (Secure.getInt(this.mContext.getContentResolver(), "double_tap_to_wake", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), "double_tap_to_wake", ((Boolean) newValue).booleanValue());
        return true;
    }
}
