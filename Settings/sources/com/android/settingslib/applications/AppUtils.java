package com.android.settingslib.applications;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.usb.IUsbManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.settingslib.R;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.instantapps.InstantAppDataProvider;
import java.util.ArrayList;
import java.util.List;

public class AppUtils {
    private static final String TAG = "AppUtils";
    private static InstantAppDataProvider sInstantAppDataProvider = null;

    public static CharSequence getLaunchByDefaultSummary(AppEntry appEntry, IUsbManager usbManager, PackageManager pm, Context context) {
        int i;
        String packageName = appEntry.info.packageName;
        boolean hasDomainURLsPreference = true;
        boolean hasPreferred = hasPreferredActivities(pm, packageName) || hasUsbDefaults(usbManager, packageName);
        if (pm.getIntentVerificationStatusAsUser(packageName, UserHandle.myUserId()) == 0) {
            hasDomainURLsPreference = false;
        }
        if (hasPreferred || hasDomainURLsPreference) {
            i = R.string.launch_defaults_some;
        } else {
            i = R.string.launch_defaults_none;
        }
        return context.getString(i);
    }

    public static boolean hasUsbDefaults(IUsbManager usbManager, String packageName) {
        if (usbManager != null) {
            try {
                return usbManager.hasDefaults(packageName, UserHandle.myUserId());
            } catch (RemoteException e) {
                Log.e(TAG, "mUsbManager.hasDefaults", e);
            }
        }
        return false;
    }

    public static boolean hasPreferredActivities(PackageManager pm, String packageName) {
        List<ComponentName> prefActList = new ArrayList();
        pm.getPreferredActivities(new ArrayList(), prefActList, packageName);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Have ");
        stringBuilder.append(prefActList.size());
        stringBuilder.append(" number of activities in preferred list");
        Log.d(str, stringBuilder.toString());
        return prefActList.size() > 0;
    }

    public static boolean isInstant(ApplicationInfo info) {
        if (sInstantAppDataProvider != null) {
            if (sInstantAppDataProvider.isInstantApp(info)) {
                return true;
            }
        } else if (info.isInstantApp()) {
            return true;
        }
        String propVal = SystemProperties.get("settingsdebug.instant.packages");
        if (!(propVal == null || propVal.isEmpty() || info.packageName == null)) {
            String[] searchTerms = propVal.split(",");
            if (searchTerms != null) {
                for (String term : searchTerms) {
                    if (info.packageName.contains(term)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static CharSequence getApplicationLabel(PackageManager packageManager, String packageName) {
        try {
            return packageManager.getApplicationInfo(packageName, 4194816).loadLabel(packageManager);
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to find info for package: ");
            stringBuilder.append(packageName);
            Log.w(str, stringBuilder.toString());
            return null;
        }
    }
}
