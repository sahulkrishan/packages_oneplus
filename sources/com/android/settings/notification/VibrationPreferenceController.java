package com.android.settings.notification;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedSwitchPreference;

public class VibrationPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_VIBRATE = "vibrate";
    private final Vibrator mVibrator;

    public VibrationPreferenceController(Context context, NotificationBackend backend) {
        super(context, backend);
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
    }

    public String getPreferenceKey() {
        return KEY_VIBRATE;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (!super.isAvailable() || this.mChannel == null) {
            return false;
        }
        if (checkCanBeVisible(3) && !isDefaultChannel() && this.mVibrator != null && this.mVibrator.hasVibrator()) {
            z = true;
        }
        return z;
    }

    public void updateState(Preference preference) {
        if (this.mChannel != null) {
            RestrictedSwitchPreference pref = (RestrictedSwitchPreference) preference;
            pref.setDisabledByAdmin(this.mAdmin);
            boolean z = !pref.isDisabledByAdmin() && isChannelConfigurable();
            pref.setEnabled(z);
            pref.setChecked(this.mChannel.shouldVibrate());
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mChannel != null) {
            this.mChannel.enableVibration(((Boolean) newValue).booleanValue());
            saveChannel();
        }
        return true;
    }
}
