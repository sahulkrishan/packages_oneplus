package com.android.settings.datetime;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.core.AbstractPreferenceController;

public class AutoTimePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_AUTO_TIME = "auto_time";
    private final UpdateTimeAndDateCallback mCallback;

    public AutoTimePreferenceController(Context context, UpdateTimeAndDateCallback callback) {
        super(context);
        this.mCallback = callback;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        if (preference instanceof RestrictedSwitchPreference) {
            if (!((RestrictedSwitchPreference) preference).isDisabledByAdmin()) {
                ((RestrictedSwitchPreference) preference).setDisabledByAdmin(getEnforcedAdminProperty());
            }
            ((RestrictedSwitchPreference) preference).setChecked(isEnabled());
        }
    }

    public String getPreferenceKey() {
        return KEY_AUTO_TIME;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), KEY_AUTO_TIME, ((Boolean) newValue).booleanValue());
        this.mCallback.updateTimeAndDateDisplay(this.mContext);
        return true;
    }

    public boolean isEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), KEY_AUTO_TIME, 0) > 0;
    }

    private EnforcedAdmin getEnforcedAdminProperty() {
        return RestrictedLockUtils.checkIfAutoTimeRequired(this.mContext);
    }
}
