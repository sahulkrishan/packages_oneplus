package com.android.settings.display;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class BatteryPercentagePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_BATTERY_PERCENTAGE = "battery_percentage";

    public BatteryPercentagePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_BATTERY_PERCENTAGE;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) preference;
        if (System.getInt(this.mContext.getContentResolver(), "status_bar_show_battery_percent", 0) == 1) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        System.putInt(this.mContext.getContentResolver(), "status_bar_show_battery_percent", ((Boolean) newValue).booleanValue());
        return true;
    }
}
