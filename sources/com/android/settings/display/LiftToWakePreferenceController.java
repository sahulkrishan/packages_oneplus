package com.android.settings.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class LiftToWakePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_LIFT_TO_WAKE = "lift_to_wake";

    public LiftToWakePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        SensorManager sensors = (SensorManager) this.mContext.getSystemService("sensor");
        return (sensors == null || sensors.getDefaultSensor(23) == null) ? false : true;
    }

    public String getPreferenceKey() {
        return KEY_LIFT_TO_WAKE;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), "wake_gesture_enabled", ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) preference;
        if (Secure.getInt(this.mContext.getContentResolver(), "wake_gesture_enabled", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }
}
