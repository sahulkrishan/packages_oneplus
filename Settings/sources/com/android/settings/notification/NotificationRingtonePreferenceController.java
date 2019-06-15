package com.android.settings.notification;

import android.content.Context;
import com.android.settings.R;

public class NotificationRingtonePreferenceController extends RingtonePreferenceControllerBase {
    private static final String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";

    public NotificationRingtonePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_notification_ringtone);
    }

    public String getPreferenceKey() {
        return KEY_NOTIFICATION_RINGTONE;
    }

    public int getRingtoneType() {
        return 2;
    }
}
