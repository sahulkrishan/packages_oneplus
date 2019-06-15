package com.android.settings.applications.defaultapps;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.os.UserManager;
import android.telecom.DefaultDialerManager;
import android.telephony.TelephonyManager;
import com.android.settingslib.applications.DefaultAppInfo;
import java.util.List;

public class DefaultPhonePreferenceController extends DefaultAppPreferenceController {
    public DefaultPhonePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        boolean z = false;
        if (!((TelephonyManager) this.mContext.getSystemService("phone")).isVoiceCapable() || ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_outgoing_calls")) {
            return false;
        }
        List<String> candidates = getCandidates();
        if (!(candidates == null || candidates.isEmpty())) {
            z = true;
        }
        return z;
    }

    public String getPreferenceKey() {
        return "default_phone_app";
    }

    /* Access modifiers changed, original: protected */
    public DefaultAppInfo getDefaultAppInfo() {
        try {
            return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mPackageManager.getPackageManager().getApplicationInfo(DefaultDialerManager.getDefaultDialerApplication(this.mContext, this.mUserId), 0));
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private List<String> getCandidates() {
        return DefaultDialerManager.getInstalledDialerApplications(this.mContext, this.mUserId);
    }

    public static boolean hasPhonePreference(String pkg, Context context) {
        return DefaultDialerManager.getInstalledDialerApplications(context, UserHandle.myUserId()).contains(pkg);
    }

    public static boolean isPhoneDefault(String pkg, Context context) {
        String def = DefaultDialerManager.getDefaultDialerApplication(context, UserHandle.myUserId());
        return def != null && def.equals(pkg);
    }
}
