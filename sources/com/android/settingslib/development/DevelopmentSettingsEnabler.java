package com.android.settingslib.development;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.support.v4.content.LocalBroadcastManager;

public class DevelopmentSettingsEnabler {
    public static final String DEVELOPMENT_SETTINGS_CHANGED_ACTION = "com.android.settingslib.development.DevelopmentSettingsEnabler.SETTINGS_CHANGED";

    private DevelopmentSettingsEnabler() {
    }

    public static void setDevelopmentSettingsEnabled(Context context, boolean enable) {
        Global.putInt(context.getContentResolver(), "development_settings_enabled", enable);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(DEVELOPMENT_SETTINGS_CHANGED_ACTION));
    }

    public static boolean isDevelopmentSettingsEnabled(Context context) {
        UserManager um = (UserManager) context.getSystemService("user");
        boolean settingEnabled = Global.getInt(context.getContentResolver(), "development_settings_enabled", Build.TYPE.equals("eng")) != 0;
        boolean hasRestriction = um.hasUserRestriction("no_debugging_features");
        boolean isAdminOrDemo = um.isAdminUser() || um.isDemoUser();
        if (isAdminOrDemo && !hasRestriction && settingEnabled) {
            return true;
        }
        return false;
    }
}
