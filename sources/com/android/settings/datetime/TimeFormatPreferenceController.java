package com.android.settings.datetime;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.text.format.DateFormat;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.Calendar;

public class TimeFormatPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    static final String HOURS_12 = "12";
    static final String HOURS_24 = "24";
    private static final String KEY_TIME_FORMAT = "24 hour";
    private final Calendar mDummyDate = Calendar.getInstance();
    private final boolean mIsFromSUW;
    private final UpdateTimeAndDateCallback mUpdateTimeAndDateCallback;

    public TimeFormatPreferenceController(Context context, UpdateTimeAndDateCallback callback, boolean isFromSUW) {
        super(context);
        this.mIsFromSUW = isFromSUW;
        this.mUpdateTimeAndDateCallback = callback;
    }

    public boolean isAvailable() {
        return this.mIsFromSUW ^ 1;
    }

    public void updateState(Preference preference) {
        if (preference instanceof TwoStatePreference) {
            preference.setEnabled(true);
            ((TwoStatePreference) preference).setChecked(is24Hour());
            Calendar now = Calendar.getInstance();
            this.mDummyDate.setTimeZone(now.getTimeZone());
            this.mDummyDate.set(now.get(1), 11, 31, 13, 0, 0);
            preference.setSummary(DateFormat.getTimeFormat(this.mContext).format(this.mDummyDate.getTime()));
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!(preference instanceof TwoStatePreference) || !TextUtils.equals(KEY_TIME_FORMAT, preference.getKey())) {
            return false;
        }
        update24HourFormat(this.mContext, Boolean.valueOf(((SwitchPreference) preference).isChecked()));
        this.mUpdateTimeAndDateCallback.updateTimeAndDateDisplay(this.mContext);
        return true;
    }

    public String getPreferenceKey() {
        return KEY_TIME_FORMAT;
    }

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(this.mContext);
    }

    static void update24HourFormat(Context context, Boolean is24Hour) {
        set24Hour(context, is24Hour);
        timeUpdated(context, is24Hour);
    }

    static void timeUpdated(Context context, Boolean is24Hour) {
        int timeFormatPreference;
        Intent timeChanged = new Intent("android.intent.action.TIME_SET");
        timeChanged.addFlags(16777216);
        if (is24Hour == null) {
            timeFormatPreference = 2;
        } else if (is24Hour.booleanValue()) {
            timeFormatPreference = 1;
        } else {
            timeFormatPreference = 0;
        }
        timeChanged.putExtra("android.intent.extra.TIME_PREF_24_HOUR_FORMAT", timeFormatPreference);
        context.sendBroadcast(timeChanged);
    }

    static void set24Hour(Context context, Boolean is24Hour) {
        String value = is24Hour == null ? null : is24Hour.booleanValue() ? HOURS_24 : HOURS_12;
        System.putString(context.getContentResolver(), "time_12_24", value);
    }
}
