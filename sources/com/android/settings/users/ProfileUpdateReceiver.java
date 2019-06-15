package com.android.settings.users;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settings.Utils;

public class ProfileUpdateReceiver extends BroadcastReceiver {
    private static final String KEY_PROFILE_NAME_COPIED_ONCE = "name_copied_once";

    public void onReceive(final Context context, Intent intent) {
        new Thread() {
            public void run() {
                UserSettings.copyMeProfilePhoto(context, null);
                ProfileUpdateReceiver.copyProfileName(context);
            }
        }.start();
    }

    private static void copyProfileName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", 0);
        if (!prefs.contains(KEY_PROFILE_NAME_COPIED_ONCE)) {
            int userId = UserHandle.myUserId();
            UserManager um = (UserManager) context.getSystemService("user");
            String profileName = Utils.getMeProfileName(context, false);
            if (profileName != null && profileName.length() > 0) {
                um.setUserName(userId, profileName);
                prefs.edit().putBoolean(KEY_PROFILE_NAME_COPIED_ONCE, true).commit();
            }
        }
    }
}
