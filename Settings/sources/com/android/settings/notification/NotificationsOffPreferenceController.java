package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;

public class NotificationsOffPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_BLOCKED_DESC = "block_desc";

    public NotificationsOffPreferenceController(Context context) {
        super(context, null);
    }

    public String getPreferenceKey() {
        return KEY_BLOCKED_DESC;
    }

    public boolean isAvailable() {
        if (this.mAppRow == null) {
            return false;
        }
        return super.isAvailable() ^ 1;
    }

    public void updateState(Preference preference) {
        if (this.mAppRow != null) {
            if (this.mChannel != null) {
                preference.setTitle((int) R.string.channel_notifications_off_desc);
            } else if (this.mChannelGroup != null) {
                preference.setTitle((int) R.string.channel_group_notifications_off_desc);
            } else {
                preference.setTitle((int) R.string.app_notifications_off_desc);
            }
        }
        preference.setSelectable(false);
    }
}
