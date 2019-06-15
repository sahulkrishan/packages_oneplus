package com.oneplus.settings.others;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class OPEmergencyRescueSettingsPreferenceController extends BasePreferenceController implements LifecycleObserver {
    private static final String KEY_EMERGENCY_RESCUE_SETTINGS = "emergency_rescue_settings";
    private Preference mPreference;

    public OPEmergencyRescueSettingsPreferenceController(Context context) {
        super(context, KEY_EMERGENCY_RESCUE_SETTINGS);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_EMERGENCY_RESCUE_SETTINGS;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_EMERGENCY_RESCUE_SETTINGS.equals(preference.getKey())) {
            return false;
        }
        try {
            this.mContext.startActivity(new Intent("oneplus.telephony.action.EMERGENCY_SETTING"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
}
