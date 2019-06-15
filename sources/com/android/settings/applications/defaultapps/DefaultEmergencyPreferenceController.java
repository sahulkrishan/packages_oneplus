package com.android.settings.applications.defaultapps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.provider.Settings.Secure;
import com.android.settingslib.applications.DefaultAppInfo;
import java.util.List;

public class DefaultEmergencyPreferenceController extends DefaultAppPreferenceController {
    private static final boolean DEFAULT_EMERGENCY_APP_IS_CONFIGURABLE = false;
    public static final Intent QUERY_INTENT = new Intent("android.telephony.action.EMERGENCY_ASSISTANCE");

    public DefaultEmergencyPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return false;
    }

    public String getPreferenceKey() {
        return "default_emergency_app";
    }

    /* Access modifiers changed, original: protected */
    public DefaultAppInfo getDefaultAppInfo() {
        return null;
    }

    private boolean isCapable() {
        return this.mContext.getResources().getBoolean(17957079);
    }

    public static boolean hasEmergencyPreference(String pkg, Context context) {
        Intent i = new Intent(QUERY_INTENT);
        i.setPackage(pkg);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(i, 0);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isEmergencyDefault(String pkg, Context context) {
        String defaultPackage = Secure.getString(context.getContentResolver(), "emergency_assistance_application");
        return defaultPackage != null && defaultPackage.equals(pkg);
    }
}
