package com.oneplus.settings.carcharger;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.BasePreferenceController;
import com.oneplus.settings.utils.OPUtils;

public class OPCarChargerPreferenceController extends BasePreferenceController implements OnPreferenceChangeListener {
    private static final String KEY_AUTO_TURN_ON_CAR_CHARGER = "car_charger_auto_turn_on";
    private static final String KEY_AUTO_TURN_ON_DND = "car_charger_auto_turn_on_dnd";
    private String KEY;

    public OPCarChargerPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        this.KEY = preferenceKey;
    }

    public final boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean settingsOn = ((Boolean) newValue).booleanValue();
        if (this.KEY.equals(KEY_AUTO_TURN_ON_DND)) {
            System.putInt(this.mContext.getContentResolver(), KEY_AUTO_TURN_ON_DND, settingsOn);
            OPUtils.sendAppTracker("charge_dnd", settingsOn ? "on" : "off");
        } else if (this.KEY.equals(KEY_AUTO_TURN_ON_CAR_CHARGER)) {
            System.putInt(this.mContext.getContentResolver(), KEY_AUTO_TURN_ON_CAR_CHARGER, settingsOn);
            OPUtils.sendAppTracker("charge_carmode", settingsOn ? "on" : "off");
        }
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = true;
        SwitchPreference switchPreference;
        if (this.KEY.equals(KEY_AUTO_TURN_ON_DND)) {
            switchPreference = (SwitchPreference) preference;
            if (System.getInt(this.mContext.getContentResolver(), KEY_AUTO_TURN_ON_DND, 0) != 1) {
                z = false;
            }
            switchPreference.setChecked(z);
        } else if (this.KEY.equals(KEY_AUTO_TURN_ON_CAR_CHARGER)) {
            switchPreference = (SwitchPreference) preference;
            if (System.getInt(this.mContext.getContentResolver(), KEY_AUTO_TURN_ON_CAR_CHARGER, 0) != 1) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
    }

    public int getAvailabilityStatus() {
        if (OPUtils.isO2() && this.KEY.equals(KEY_AUTO_TURN_ON_DND)) {
            return 0;
        }
        if (OPUtils.isO2() || !this.KEY.equals(KEY_AUTO_TURN_ON_CAR_CHARGER)) {
            return 1;
        }
        return 0;
    }
}
