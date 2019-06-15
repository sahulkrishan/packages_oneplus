package com.oneplus.lib.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public abstract class AppUtils {
    public static final String SP_KEY_PREV_VER = "prev_install_versionCode";
    private static final String TAG = AppUtils.class.getSimpleName();

    public static void setCurrentVersion(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Get app version code failed! ");
            stringBuilder.append(e.toString());
            Log.d(str, stringBuilder.toString());
        }
        PreferenceUtils.applyInt(context, SP_KEY_PREV_VER, versionCode);
    }

    public static int getPrevVersion(Context context) {
        return PreferenceUtils.getInt(context, SP_KEY_PREV_VER, -1);
    }

    public static int getCurrentVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Get app version code failed! ");
            stringBuilder.append(e.toString());
            Log.d(str, stringBuilder.toString());
            return -1;
        }
    }

    public static String getCurrentVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Get app version code failed! ");
            stringBuilder.append(e.toString());
            Log.d(str, stringBuilder.toString());
            return null;
        }
    }

    public static boolean versionCodeChanged(Context context) {
        return getPrevVersion(context) != getCurrentVersion(context);
    }
}
