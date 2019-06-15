package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.UserManager;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class WriteSystemSettingsPreferenceController extends AppInfoPreferenceControllerBase {
    public WriteSystemSettingsPreferenceController(Context context, String prefKey) {
        super(context, prefKey);
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
            if (equals.equals("android.permission.WRITE_SETTINGS")) {
                return 0;
            }
        }
        return 3;
    }

    /* Access modifiers changed, original: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return WriteSettingsDetails.class;
    }

    public CharSequence getSummary() {
        return WriteSettingsDetails.getSummary(this.mContext, this.mParent.getAppEntry());
    }
}
