package com.android.settings.notification;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public abstract class RingtonePreferenceControllerBase extends AbstractPreferenceController implements PreferenceControllerMixin {
    public abstract int getRingtoneType();

    public RingtonePreferenceControllerBase(Context context) {
        super(context);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        CharSequence summary = Ringtone.getTitle(this.mContext, RingtoneManager.getActualDefaultRingtoneUri(this.mContext, getRingtoneType()), false, true);
        if (summary != null) {
            preference.setSummary(summary);
        }
    }
}
