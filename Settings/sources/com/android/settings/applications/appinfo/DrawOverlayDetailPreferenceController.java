package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.UserManager;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class DrawOverlayDetailPreferenceController extends AppInfoPreferenceControllerBase {
    public DrawOverlayDetailPreferenceController(Context context, String key) {
        super(context, key);
    }

    public int getAvailabilityStatus() {
        if (Utils.isManagedProfile(UserManager.get(this.mContext))) {
            return 3;
        }
        PackageInfo packageInfo = this.mParent.getPackageInfo();
        if (packageInfo == null || packageInfo.requestedPermissions == null) {
            return 3;
        }
        for (String equals : packageInfo.requestedPermissions) {
            if (equals.equals("android.permission.SYSTEM_ALERT_WINDOW")) {
                return 0;
            }
        }
        return 3;
    }

    /* Access modifiers changed, original: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return DrawOverlayDetails.class;
    }

    public CharSequence getSummary() {
        return DrawOverlayDetails.getSummary(this.mContext, this.mParent.getAppEntry());
    }
}
