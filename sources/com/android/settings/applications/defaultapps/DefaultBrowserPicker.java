package com.android.settings.applications.defaultapps;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.ArraySet;
import com.android.settings.R;
import com.android.settingslib.applications.DefaultAppInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DefaultBrowserPicker extends DefaultAppPickerFragment {
    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.default_browser_settings;
    }

    public int getMetricsCategory() {
        return 785;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        return this.mPm.getDefaultBrowserPackageNameAsUser(this.mUserId);
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String packageName) {
        return this.mPm.setDefaultBrowserPackageNameAsUser(packageName, this.mUserId);
    }

    /* Access modifiers changed, original: protected */
    public List<DefaultAppInfo> getCandidates() {
        List<DefaultAppInfo> candidates = new ArrayList();
        Context context = getContext();
        List<ResolveInfo> list = this.mPm.queryIntentActivitiesAsUser(DefaultBrowserPreferenceController.BROWSE_PROBE, 131072, this.mUserId);
        int count = list.size();
        Set<String> addedPackages = new ArraySet();
        for (int i = 0; i < count; i++) {
            ResolveInfo info = (ResolveInfo) list.get(i);
            if (info.activityInfo != null && info.handleAllWebDataURI) {
                String packageName = info.activityInfo.packageName;
                if (!addedPackages.contains(packageName)) {
                    try {
                        candidates.add(new DefaultAppInfo(context, this.mPm, this.mPm.getApplicationInfoAsUser(packageName, 0, this.mUserId)));
                        addedPackages.add(packageName);
                    } catch (NameNotFoundException e) {
                    }
                }
            }
        }
        return candidates;
    }
}
