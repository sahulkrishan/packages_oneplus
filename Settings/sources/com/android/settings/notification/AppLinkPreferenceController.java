package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;

public class AppLinkPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_APP_LINK = "app_link";
    private static final String TAG = "AppLinkPrefContr";

    public AppLinkPreferenceController(Context context) {
        super(context, null);
    }

    public String getPreferenceKey() {
        return KEY_APP_LINK;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (!super.isAvailable()) {
            return false;
        }
        if (this.mAppRow.settingsIntent != null) {
            z = true;
        }
        return z;
    }

    public void updateState(Preference preference) {
        if (this.mAppRow != null) {
            preference.setIntent(this.mAppRow.settingsIntent);
        }
    }
}
