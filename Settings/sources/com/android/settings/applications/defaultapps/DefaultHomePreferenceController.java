package com.android.settings.applications.defaultapps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import com.android.settings.R;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class DefaultHomePreferenceController extends DefaultAppPreferenceController {
    static final IntentFilter HOME_FILTER = new IntentFilter("android.intent.action.MAIN");
    private final String mPackageName = this.mContext.getPackageName();

    static {
        HOME_FILTER.addCategory("android.intent.category.HOME");
        HOME_FILTER.addCategory("android.intent.category.DEFAULT");
    }

    public DefaultHomePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return "default_home";
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_default_home);
    }

    /* Access modifiers changed, original: protected */
    public DefaultAppInfo getDefaultAppInfo() {
        ArrayList<ResolveInfo> homeActivities = new ArrayList();
        ComponentName currentDefaultHome = this.mPackageManager.getHomeActivities(homeActivities);
        if (currentDefaultHome != null) {
            OPUtils.sendAppTrackerForDefaultHomeAppByComponentName(currentDefaultHome.toString());
        }
        if (currentDefaultHome != null) {
            return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mUserId, currentDefaultHome);
        }
        ActivityInfo onlyAppInfo = getOnlyAppInfo(homeActivities);
        if (onlyAppInfo != null) {
            return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mUserId, onlyAppInfo.getComponentName());
        }
        return null;
    }

    private ActivityInfo getOnlyAppInfo(List<ResolveInfo> homeActivities) {
        List<ActivityInfo> appLabels = new ArrayList();
        this.mPackageManager.getHomeActivities(homeActivities);
        for (ResolveInfo candidate : homeActivities) {
            ActivityInfo info = candidate.activityInfo;
            if (!info.packageName.equals(this.mPackageName)) {
                appLabels.add(info);
            }
        }
        if (appLabels.size() == 1) {
            return (ActivityInfo) appLabels.get(0);
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public Intent getSettingIntent(DefaultAppInfo info) {
        Intent intent = null;
        if (info == null) {
            return null;
        }
        String packageName;
        if (info.componentName != null) {
            packageName = info.componentName.getPackageName();
        } else if (info.packageItemInfo == null) {
            return null;
        } else {
            packageName = info.packageItemInfo.packageName;
        }
        Intent intent2 = new Intent("android.intent.action.APPLICATION_PREFERENCES").setPackage(packageName).addFlags(268468224);
        if (this.mPackageManager.queryIntentActivities(intent2, 0).size() == 1) {
            intent = intent2;
        }
        return intent;
    }

    public static boolean hasHomePreference(String pkg, Context context) {
        ArrayList<ResolveInfo> homeActivities = new ArrayList();
        context.getPackageManager().getHomeActivities(homeActivities);
        for (int i = 0; i < homeActivities.size(); i++) {
            if (((ResolveInfo) homeActivities.get(i)).activityInfo.packageName.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHomeDefault(String pkg, PackageManagerWrapper pm) {
        ComponentName def = pm.getHomeActivities(new ArrayList());
        return def == null || def.getPackageName().equals(pkg);
    }
}
