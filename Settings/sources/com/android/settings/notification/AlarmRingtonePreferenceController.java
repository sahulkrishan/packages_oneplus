package com.android.settings.notification;

import android.content.Context;

public class AlarmRingtonePreferenceController extends RingtonePreferenceControllerBase {
    private static final String KEY_ALARM_RINGTONE = "alarm_ringtone";

    public AlarmRingtonePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return KEY_ALARM_RINGTONE;
    }

    public int getRingtoneType() {
        return 4;
    }
}
