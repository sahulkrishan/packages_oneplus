package com.android.settings.applications.defaultapps;

import android.content.ComponentName;
import android.content.Context;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import com.android.settingslib.applications.DefaultAppInfo;

public class DefaultSmsPreferenceController extends DefaultAppPreferenceController {
    public DefaultSmsPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return !this.mUserManager.getUserInfo(this.mUserId).isRestricted() && ((TelephonyManager) this.mContext.getSystemService("phone")).isSmsCapable();
    }

    public String getPreferenceKey() {
        return "default_sms_app";
    }

    /* Access modifiers changed, original: protected */
    public DefaultAppInfo getDefaultAppInfo() {
        ComponentName app = SmsApplication.getDefaultSmsApplication(this.mContext, true);
        if (app != null) {
            return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mUserId, app);
        }
        return null;
    }

    public static boolean hasSmsPreference(String pkg, Context context) {
        for (SmsApplicationData data : SmsApplication.getApplicationCollection(context)) {
            if (data.mPackageName.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSmsDefault(String pkg, Context context) {
        ComponentName appName = SmsApplication.getDefaultSmsApplication(context, true);
        if (appName == null || !appName.getPackageName().equals(pkg)) {
            return false;
        }
        return true;
    }
}
