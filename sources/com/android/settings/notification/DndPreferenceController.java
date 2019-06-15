package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedSwitchPreference;

public class DndPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_BYPASS_DND = "bypass_dnd";

    public DndPreferenceController(Context context, NotificationBackend backend) {
        super(context, backend);
    }

    public String getPreferenceKey() {
        return KEY_BYPASS_DND;
    }

    public boolean isAvailable() {
        if (!super.isAvailable() || this.mChannel == null) {
            return false;
        }
        return true;
    }

    public void updateState(Preference preference) {
        if (this.mChannel != null) {
            RestrictedSwitchPreference pref = (RestrictedSwitchPreference) preference;
            pref.setDisabledByAdmin(this.mAdmin);
            boolean z = isChannelConfigurable() && !pref.isDisabledByAdmin();
            pref.setEnabled(z);
            pref.setChecked(this.mChannel.canBypassDnd());
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mChannel != null) {
            this.mChannel.setBypassDnd(((Boolean) newValue).booleanValue());
            this.mChannel.lockFields(1);
            saveChannel();
        }
        return true;
    }
}
