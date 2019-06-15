package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class AppStoreUtil {
    private static final String LOG_TAG = "AppStoreUtil";

    private static Intent resolveIntent(Context context, Intent i) {
        ResolveInfo result = context.getPackageManager().resolveActivity(i, 0);
        return result != null ? new Intent(i.getAction()).setClassName(result.activityInfo.packageName, result.activityInfo.name) : null;
    }

    public static String getInstallerPackageName(Context context, String packageName) {
        try {
            return context.getPackageManager().getInstallerPackageName(packageName);
        } catch (IllegalArgumentException e) {
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Exception while retrieving the package installer of ");
            stringBuilder.append(packageName);
            Log.e(str, stringBuilder.toString(), e);
            return null;
        }
    }

    public static Intent getAppStoreLink(Context context, String installerPackageName, String packageName) {
        Intent result = resolveIntent(context, new Intent("android.intent.action.SHOW_APP_INFO").setPackage(installerPackageName));
        if (result == null) {
            return null;
        }
        result.putExtra("android.intent.extra.PACKAGE_NAME", packageName);
        return result;
    }

    public static Intent getAppStoreLink(Context context, String packageName) {
        return getAppStoreLink(context, getInstallerPackageName(context, packageName), packageName);
    }
}
