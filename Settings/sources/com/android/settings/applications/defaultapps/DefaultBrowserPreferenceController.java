package com.android.settings.applications.defaultapps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.IconDrawableFactory;
import android.util.Log;
import com.android.settingslib.applications.DefaultAppInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DefaultBrowserPreferenceController extends DefaultAppPreferenceController {
    static final Intent BROWSE_PROBE = new Intent().setAction("android.intent.action.VIEW").addCategory("android.intent.category.BROWSABLE").setData(Uri.parse("http:"));
    private static final String TAG = "BrowserPrefCtrl";

    public DefaultBrowserPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        List<ResolveInfo> candidates = getCandidates();
        return (candidates == null || candidates.isEmpty()) ? false : true;
    }

    public String getPreferenceKey() {
        return "default_browser";
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        CharSequence defaultAppLabel = getDefaultAppLabel();
        if (!TextUtils.isEmpty(defaultAppLabel)) {
            preference.setSummary(defaultAppLabel);
        }
    }

    /* Access modifiers changed, original: protected */
    public DefaultAppInfo getDefaultAppInfo() {
        try {
            String packageName = this.mPackageManager.getDefaultBrowserPackageNameAsUser(this.mUserId);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Get default browser package: ");
            stringBuilder.append(packageName);
            Log.d(str, stringBuilder.toString());
            return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mPackageManager.getPackageManager().getApplicationInfo(packageName, 0));
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public CharSequence getDefaultAppLabel() {
        CharSequence defaultAppLabel = null;
        if (!isAvailable()) {
            return null;
        }
        DefaultAppInfo defaultApp = getDefaultAppInfo();
        if (defaultApp != null) {
            defaultAppLabel = defaultApp.loadLabel();
        }
        if (TextUtils.isEmpty(defaultAppLabel)) {
            return getOnlyAppLabel();
        }
        return defaultAppLabel;
    }

    public Drawable getDefaultAppIcon() {
        if (!isAvailable()) {
            return null;
        }
        DefaultAppInfo defaultApp = getDefaultAppInfo();
        if (defaultApp != null) {
            return defaultApp.loadIcon();
        }
        return getOnlyAppIcon();
    }

    private List<ResolveInfo> getCandidates() {
        List<ResolveInfo> candidates = new ArrayList();
        List<ResolveInfo> list = this.mPackageManager.queryIntentActivitiesAsUser(BROWSE_PROBE, 131072, this.mUserId);
        int count = list.size();
        Set<String> addedPackages = new ArraySet();
        for (int i = 0; i < count; i++) {
            ResolveInfo info = (ResolveInfo) list.get(i);
            if (info.activityInfo != null && info.handleAllWebDataURI) {
                String packageName = info.activityInfo.packageName;
                if (!addedPackages.contains(packageName)) {
                    candidates.add(info);
                    addedPackages.add(packageName);
                }
            }
        }
        return candidates;
    }

    private String getOnlyAppLabel() {
        List<ResolveInfo> list = getCandidates();
        String packageName = null;
        if (list == null || list.size() != 1) {
            return null;
        }
        ResolveInfo info = (ResolveInfo) list.get(0);
        String label = info.loadLabel(this.mPackageManager.getPackageManager()).toString();
        ComponentInfo cn = info.getComponentInfo();
        if (cn != null) {
            packageName = cn.packageName;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Getting label for the only browser app: ");
        stringBuilder.append(packageName);
        stringBuilder.append(label);
        Log.d(str, stringBuilder.toString());
        return label;
    }

    private Drawable getOnlyAppIcon() {
        List<ResolveInfo> list = getCandidates();
        if (list == null || list.size() != 1) {
            return null;
        }
        ComponentInfo cn = ((ResolveInfo) list.get(0)).getComponentInfo();
        String packageName = cn == null ? null : cn.packageName;
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        try {
            ApplicationInfo appInfo = this.mPackageManager.getPackageManager().getApplicationInfo(packageName, 0);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Getting icon for the only browser app: ");
            stringBuilder.append(packageName);
            Log.d(str, stringBuilder.toString());
            return IconDrawableFactory.newInstance(this.mContext).getBadgedIcon(cn, appInfo, this.mUserId);
        } catch (NameNotFoundException e) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Error getting app info for ");
            stringBuilder2.append(packageName);
            Log.w(str2, stringBuilder2.toString());
            return null;
        }
    }

    public static boolean hasBrowserPreference(String pkg, Context context) {
        Intent intent = new Intent(BROWSE_PROBE);
        intent.setPackage(pkg);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return false;
        }
        return true;
    }

    public boolean isBrowserDefault(String pkg, int userId) {
        String defaultPackage = this.mPackageManager.getDefaultBrowserPackageNameAsUser(userId);
        if (defaultPackage != null) {
            return defaultPackage.equals(pkg);
        }
        List<ResolveInfo> list = this.mPackageManager.queryIntentActivitiesAsUser(BROWSE_PROBE, 131072, userId);
        boolean z = true;
        if (list == null || list.size() != 1) {
            z = false;
        }
        return z;
    }
}
