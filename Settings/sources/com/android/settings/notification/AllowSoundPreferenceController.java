package com.android.settings.notification;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedSwitchPreference;

public class AllowSoundPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_IMPORTANCE = "allow_sound";
    private static final String TAG = "AllowSoundPrefContr";
    private ImportanceListener mImportanceListener;

    public AllowSoundPreferenceController(Context context, ImportanceListener importanceListener, NotificationBackend backend) {
        super(context, backend);
        this.mImportanceListener = importanceListener;
    }

    public String getPreferenceKey() {
        return KEY_IMPORTANCE;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (!super.isAvailable()) {
            return false;
        }
        if (this.mChannel != null && "miscellaneous".equals(this.mChannel.getId())) {
            z = true;
        }
        return z;
    }

    public void updateState(Preference preference) {
        if (this.mChannel != null) {
            RestrictedSwitchPreference pref = (RestrictedSwitchPreference) preference;
            pref.setDisabledByAdmin(this.mAdmin);
            boolean z = false;
            boolean z2 = isChannelConfigurable() && !pref.isDisabledByAdmin();
            pref.setEnabled(z2);
            if (this.mChannel.getImportance() >= 3 || this.mChannel.getImportance() == NotificationManagerCompat.IMPORTANCE_UNSPECIFIED) {
                z = true;
            }
            pref.setChecked(z);
            return;
        }
        Log.i(TAG, "tried to updatestate on a null channel?!");
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mChannel != null) {
            this.mChannel.setImportance(((Boolean) newValue).booleanValue() ? NotificationManagerCompat.IMPORTANCE_UNSPECIFIED : 2);
            this.mChannel.lockFields(4);
            saveChannel();
            this.mImportanceListener.onImportanceChanged();
        }
        return true;
    }
}
