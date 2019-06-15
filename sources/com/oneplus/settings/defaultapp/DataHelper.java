package com.oneplus.settings.defaultapp;

import android.content.Context;
import android.provider.Settings.System;

public class DataHelper {
    public static final String DEFAULT_APP_INIT = "op_default_app_init";

    public static String getDefaultAppPackageName(Context ctx, String appType) {
        return System.getString(ctx.getContentResolver(), appType);
    }

    public static void setDefaultAppPackageName(Context ctx, String appType, String pkgName) {
        System.putString(ctx.getContentResolver(), appType, pkgName);
    }

    public static boolean isDefaultAppInited(Context ctx) {
        if (System.getInt(ctx.getContentResolver(), DEFAULT_APP_INIT, 0) != 0) {
            return true;
        }
        return false;
    }

    public static void setDefaultAppInited(Context ctx) {
        System.putInt(ctx.getContentResolver(), DEFAULT_APP_INIT, 1);
    }
}
