package com.android.settings.datetime;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;

public class AutoTimeZonePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_AUTO_TIME_ZONE = "auto_zone";
    private final UpdateTimeAndDateCallback mCallback;
    private final boolean mIsFromSUW;

    public AutoTimeZonePreferenceController(Context context, UpdateTimeAndDateCallback callback, boolean isFromSUW) {
        super(context);
        this.mCallback = callback;
        this.mIsFromSUW = isFromSUW;
    }

    public boolean isAvailable() {
        return (Utils.isWifiOnly(this.mContext) || this.mIsFromSUW) ? false : true;
    }

    public String getPreferenceKey() {
        return KEY_AUTO_TIME_ZONE;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            ((SwitchPreference) preference).setChecked(isEnabled());
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "auto_time_zone", ((Boolean) newValue).booleanValue());
        this.mCallback.updateTimeAndDateDisplay(this.mContext);
        return true;
    }

    public boolean isEnabled() {
        return isAvailable() && Global.getInt(this.mContext.getContentResolver(), "auto_time_zone", 0) > 0;
    }
}
