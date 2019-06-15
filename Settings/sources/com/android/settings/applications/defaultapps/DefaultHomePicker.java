package com.android.settings.applications.defaultapps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settingslib.applications.DefaultAppInfo;
import java.util.ArrayList;
import java.util.List;

public class DefaultHomePicker extends DefaultAppPickerFragment {
    private String mPackageName;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mPackageName = context.getPackageName();
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.default_home_settings;
    }

    public int getMetricsCategory() {
        return 787;
    }

    /* Access modifiers changed, original: protected */
    public List<DefaultAppInfo> getCandidates() {
        boolean mustSupportManagedProfile = hasManagedProfile();
        List<DefaultAppInfo> candidates = new ArrayList();
        List<ResolveInfo> homeActivities = new ArrayList();
        Context context = getContext();
        this.mPm.getHomeActivities(homeActivities);
        for (ResolveInfo resolveInfo : homeActivities) {
            ActivityInfo info = resolveInfo.activityInfo;
            ComponentName activityName = new ComponentName(info.packageName, info.name);
            if (!info.packageName.equals(this.mPackageName)) {
                String str;
                boolean enabled = true;
                if (!mustSupportManagedProfile || launcherHasManagedProfilesFeature(resolveInfo)) {
                    str = null;
                } else {
                    str = getContext().getString(R.string.home_work_profile_not_supported);
                    enabled = false;
                }
                String summary = str;
                Context context2 = context;
                candidates.add(new DefaultAppInfo(context2, this.mPm, this.mUserId, activityName, summary, enabled));
            }
        }
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        ComponentName currentDefaultHome = this.mPm.getHomeActivities(new ArrayList());
        if (currentDefaultHome != null) {
            return currentDefaultHome.flattenToString();
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        ComponentName component = ComponentName.unflattenFromString(key);
        List<ResolveInfo> homeActivities = new ArrayList();
        this.mPm.getHomeActivities(homeActivities);
        List<ComponentName> allComponents = new ArrayList();
        for (ResolveInfo info : homeActivities) {
            ActivityInfo appInfo = info.activityInfo;
            allComponents.add(new ComponentName(appInfo.packageName, appInfo.name));
        }
        this.mPm.replacePreferredActivity(DefaultHomePreferenceController.HOME_FILTER, 1048576, (ComponentName[]) allComponents.toArray(new ComponentName[0]), component);
        Context context = getContext();
        Intent i = new Intent("android.intent.action.MAIN");
        i.addCategory("android.intent.category.HOME");
        i.setFlags(268435456);
        context.startActivity(i);
        return true;
    }

    private boolean hasManagedProfile() {
        for (UserInfo userInfo : this.mUserManager.getProfiles(getContext().getUserId())) {
            if (userInfo.isManagedProfile() && userInfo.id != 999) {
                return true;
            }
        }
        return false;
    }

    private boolean launcherHasManagedProfilesFeature(ResolveInfo resolveInfo) {
        try {
            return versionNumberAtLeastL(this.mPm.getPackageManager().getApplicationInfo(resolveInfo.activityInfo.packageName, 0).targetSdkVersion);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private boolean versionNumberAtLeastL(int versionNumber) {
        return versionNumber >= 21;
    }
}
