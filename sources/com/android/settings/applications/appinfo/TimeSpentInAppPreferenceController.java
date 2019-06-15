package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.core.BasePreferenceController;
import java.util.List;

public class TimeSpentInAppPreferenceController extends BasePreferenceController {
    @VisibleForTesting
    static final Intent SEE_TIME_IN_APP_TEMPLATE = new Intent("com.android.settings.action.TIME_SPENT_IN_APP");
    private Intent mIntent;
    private final PackageManager mPackageManager;
    private String mPackageName;

    public TimeSpentInAppPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        this.mPackageManager = context.getPackageManager();
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
        this.mIntent = new Intent(SEE_TIME_IN_APP_TEMPLATE).putExtra("android.intent.extra.PACKAGE_NAME", this.mPackageName);
    }

    public int getAvailabilityStatus() {
        if (TextUtils.isEmpty(this.mPackageName)) {
            return 2;
        }
        List<ResolveInfo> resolved = this.mPackageManager.queryIntentActivities(this.mIntent, 0);
        if (resolved == null || resolved.isEmpty()) {
            return 2;
        }
        for (ResolveInfo info : resolved) {
            if (isSystemApp(info)) {
                return 0;
            }
        }
        return 2;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref != null) {
            pref.setIntent(this.mIntent);
        }
    }

    private boolean isSystemApp(ResolveInfo info) {
        return (info == null || info.activityInfo == null || info.activityInfo.applicationInfo == null || (info.activityInfo.applicationInfo.flags & 1) == 0) ? false : true;
    }
}
