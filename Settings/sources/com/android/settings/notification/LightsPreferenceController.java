package com.android.settings.notification;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedSwitchPreference;

public class LightsPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_LIGHTS = "lights";

    public LightsPreferenceController(Context context, NotificationBackend backend) {
        super(context, backend);
    }

    public String getPreferenceKey() {
        return KEY_LIGHTS;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (!super.isAvailable() || this.mChannel == null) {
            return false;
        }
        if (checkCanBeVisible(3) && canPulseLight() && !isDefaultChannel()) {
            z = true;
        }
        return z;
    }

    public void updateState(Preference preference) {
        if (this.mChannel != null) {
            RestrictedSwitchPreference pref = (RestrictedSwitchPreference) preference;
            pref.setDisabledByAdmin(this.mAdmin);
            boolean z = isChannelConfigurable() && !pref.isDisabledByAdmin();
            pref.setEnabled(z);
            pref.setChecked(this.mChannel.shouldShowLights());
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mChannel != null) {
            this.mChannel.enableLights(((Boolean) newValue).booleanValue());
            saveChannel();
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean canPulseLight() {
        boolean z = false;
        if (!this.mContext.getResources().getBoolean(17956988)) {
            return false;
        }
        if (System.getInt(this.mContext.getContentResolver(), "notification_light_pulse", 0) == 1) {
            z = true;
        }
        return z;
    }
}
