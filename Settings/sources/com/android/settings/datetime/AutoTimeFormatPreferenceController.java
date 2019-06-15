package com.android.settings.datetime;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.text.format.DateFormat;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.Locale;

public class AutoTimeFormatPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_AUTO_24_HOUR = "auto_24hour";

    public AutoTimeFormatPreferenceController(Context context, UpdateTimeAndDateCallback callback) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_AUTO_24_HOUR;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            ((SwitchPreference) preference).setChecked(isAutoTimeFormatSelection(this.mContext));
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!(preference instanceof TwoStatePreference) || !TextUtils.equals(KEY_AUTO_24_HOUR, preference.getKey())) {
            return false;
        }
        Boolean is24Hour;
        if (((SwitchPreference) preference).isChecked()) {
            is24Hour = null;
        } else {
            is24Hour = Boolean.valueOf(is24HourLocale(this.mContext.getResources().getConfiguration().locale));
        }
        TimeFormatPreferenceController.update24HourFormat(this.mContext, is24Hour);
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean is24HourLocale(Locale locale) {
        return DateFormat.is24HourLocale(locale);
    }

    static boolean isAutoTimeFormatSelection(Context context) {
        return System.getString(context.getContentResolver(), "time_12_24") == null;
    }
}
