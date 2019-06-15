package com.android.settingslib.users;

import android.app.AppGlobals;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.IntentCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class AppRestrictionsHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "AppRestrictionsHelper";
    private final Context mContext;
    private final IPackageManager mIPm;
    private final Injector mInjector;
    private boolean mLeanback;
    private final PackageManager mPackageManager;
    private final boolean mRestrictedProfile;
    HashMap<String, Boolean> mSelectedPackages;
    private final UserHandle mUser;
    private final UserManager mUserManager;
    private List<SelectableAppInfo> mVisibleApps;

    private static class AppLabelComparator implements Comparator<SelectableAppInfo> {
        private AppLabelComparator() {
        }

        public int compare(SelectableAppInfo lhs, SelectableAppInfo rhs) {
            return lhs.activityName.toString().toLowerCase().compareTo(rhs.activityName.toString().toLowerCase());
        }
    }

    @VisibleForTesting
    static class Injector {
        private Context mContext;
        private UserHandle mUser;

        Injector(Context context, UserHandle user) {
            this.mContext = context;
            this.mUser = user;
        }

        /* Access modifiers changed, original: 0000 */
        public Context getContext() {
            return this.mContext;
        }

        /* Access modifiers changed, original: 0000 */
        public UserHandle getUser() {
            return this.mUser;
        }

        /* Access modifiers changed, original: 0000 */
        public PackageManager getPackageManager() {
            return this.mContext.getPackageManager();
        }

        /* Access modifiers changed, original: 0000 */
        public IPackageManager getIPackageManager() {
            return AppGlobals.getPackageManager();
        }

        /* Access modifiers changed, original: 0000 */
        public UserManager getUserManager() {
            return (UserManager) this.mContext.getSystemService(UserManager.class);
        }

        /* Access modifiers changed, original: 0000 */
        public List<InputMethodInfo> getInputMethodList() {
            return ((InputMethodManager) getContext().getSystemService("input_method")).getInputMethodList();
        }
    }

    public interface OnDisableUiForPackageListener {
        void onDisableUiForPackage(String str);
    }

    public static class SelectableAppInfo {
        public CharSequence activityName;
        public CharSequence appName;
        public Drawable icon;
        public SelectableAppInfo masterEntry;
        public String packageName;

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.packageName);
            stringBuilder.append(": appName=");
            stringBuilder.append(this.appName);
            stringBuilder.append("; activityName=");
            stringBuilder.append(this.activityName);
            stringBuilder.append("; icon=");
            stringBuilder.append(this.icon);
            stringBuilder.append("; masterEntry=");
            stringBuilder.append(this.masterEntry);
            return stringBuilder.toString();
        }
    }

    public AppRestrictionsHelper(Context context, UserHandle user) {
        this(new Injector(context, user));
    }

    @VisibleForTesting
    AppRestrictionsHelper(Injector injector) {
        this.mSelectedPackages = new HashMap();
        this.mInjector = injector;
        this.mContext = this.mInjector.getContext();
        this.mPackageManager = this.mInjector.getPackageManager();
        this.mIPm = this.mInjector.getIPackageManager();
        this.mUser = this.mInjector.getUser();
        this.mUserManager = this.mInjector.getUserManager();
        this.mRestrictedProfile = this.mUserManager.getUserInfo(this.mUser.getIdentifier()).isRestricted();
    }

    public void setPackageSelected(String packageName, boolean selected) {
        this.mSelectedPackages.put(packageName, Boolean.valueOf(selected));
    }

    public boolean isPackageSelected(String packageName) {
        return ((Boolean) this.mSelectedPackages.get(packageName)).booleanValue();
    }

    public void setLeanback(boolean isLeanback) {
        this.mLeanback = isLeanback;
    }

    public List<SelectableAppInfo> getVisibleApps() {
        return this.mVisibleApps;
    }

    public void applyUserAppsStates(OnDisableUiForPackageListener listener) {
        if (this.mRestrictedProfile || this.mUser.getIdentifier() == UserHandle.myUserId()) {
            for (Entry<String, Boolean> entry : this.mSelectedPackages.entrySet()) {
                applyUserAppState((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue(), listener);
            }
            return;
        }
        Log.e(TAG, "Cannot apply application restrictions on another user!");
    }

    public void applyUserAppState(String packageName, boolean enabled, OnDisableUiForPackageListener listener) {
        int userId = this.mUser.getIdentifier();
        if (enabled) {
            try {
                ApplicationInfo info = this.mIPm.getApplicationInfo(packageName, 4194304, userId);
                if (info == null || !info.enabled || (info.flags & 8388608) == 0) {
                    this.mIPm.installExistingPackageAsUser(packageName, this.mUser.getIdentifier(), 0, 0);
                }
                if (info != null && (1 & info.privateFlags) != 0 && (info.flags & 8388608) != 0) {
                    listener.onDisableUiForPackage(packageName);
                    this.mIPm.setApplicationHiddenSettingAsUser(packageName, false, userId);
                    return;
                }
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        try {
            if (this.mIPm.getApplicationInfo(packageName, 0, userId) == null) {
                return;
            }
            if (this.mRestrictedProfile) {
                this.mPackageManager.deletePackageAsUser(packageName, null, 4, this.mUser.getIdentifier());
                return;
            }
            listener.onDisableUiForPackage(packageName);
            this.mIPm.setApplicationHiddenSettingAsUser(packageName, true, userId);
        } catch (RemoteException e2) {
        }
    }

    public void fetchAndMergeApps() {
        this.mVisibleApps = new ArrayList();
        PackageManager pm = this.mPackageManager;
        IPackageManager ipm = this.mIPm;
        HashSet<String> excludePackages = new HashSet();
        addSystemImes(excludePackages);
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        if (this.mLeanback) {
            launcherIntent.addCategory(IntentCompat.CATEGORY_LEANBACK_LAUNCHER);
        } else {
            launcherIntent.addCategory("android.intent.category.LAUNCHER");
        }
        addSystemApps(this.mVisibleApps, launcherIntent, excludePackages);
        addSystemApps(this.mVisibleApps, new Intent("android.appwidget.action.APPWIDGET_UPDATE"), excludePackages);
        for (ApplicationInfo app : pm.getInstalledApplications(4194304)) {
            if ((8388608 & app.flags) != 0) {
                if ((app.flags & 1) == 0 && (app.flags & 128) == 0) {
                    SelectableAppInfo info = new SelectableAppInfo();
                    info.packageName = app.packageName;
                    info.appName = app.loadLabel(pm);
                    info.activityName = info.appName;
                    info.icon = app.loadIcon(pm);
                    this.mVisibleApps.add(info);
                } else {
                    try {
                        PackageInfo pi = pm.getPackageInfo(app.packageName, 0);
                        if (this.mRestrictedProfile && pi.requiredAccountType != null && pi.restrictedAccountType == null) {
                            this.mSelectedPackages.put(app.packageName, Boolean.valueOf(false));
                        }
                    } catch (NameNotFoundException e) {
                    }
                }
            }
        }
        List<ApplicationInfo> userApps = null;
        try {
            ParceledListSlice<ApplicationInfo> listSlice = ipm.getInstalledApplications(8192, this.mUser.getIdentifier());
            if (listSlice != null) {
                userApps = listSlice.getList();
            }
        } catch (RemoteException e2) {
        }
        if (userApps != null) {
            for (ApplicationInfo app2 : userApps) {
                if ((app2.flags & 8388608) != 0) {
                    if ((app2.flags & 1) == 0 && (app2.flags & 128) == 0) {
                        SelectableAppInfo info2 = new SelectableAppInfo();
                        info2.packageName = app2.packageName;
                        info2.appName = app2.loadLabel(pm);
                        info2.activityName = info2.appName;
                        info2.icon = app2.loadIcon(pm);
                        this.mVisibleApps.add(info2);
                    }
                }
            }
        }
        Collections.sort(this.mVisibleApps, new AppLabelComparator());
        Set<String> dedupPackageSet = new HashSet();
        for (int i = this.mVisibleApps.size() - 1; i >= 0; i--) {
            SelectableAppInfo info3 = (SelectableAppInfo) this.mVisibleApps.get(i);
            String both = new StringBuilder();
            both.append(info3.packageName);
            both.append("+");
            both.append(info3.activityName);
            both = both.toString();
            if (TextUtils.isEmpty(info3.packageName) || TextUtils.isEmpty(info3.activityName) || !dedupPackageSet.contains(both)) {
                dedupPackageSet.add(both);
            } else {
                this.mVisibleApps.remove(i);
            }
        }
        HashMap<String, SelectableAppInfo> packageMap = new HashMap();
        for (SelectableAppInfo info4 : this.mVisibleApps) {
            if (packageMap.containsKey(info4.packageName)) {
                info4.masterEntry = (SelectableAppInfo) packageMap.get(info4.packageName);
            } else {
                packageMap.put(info4.packageName, info4);
            }
        }
    }

    private void addSystemImes(Set<String> excludePackages) {
        for (InputMethodInfo imi : this.mInjector.getInputMethodList()) {
            try {
                if (imi.isDefault(this.mContext) && isSystemPackage(imi.getPackageName())) {
                    excludePackages.add(imi.getPackageName());
                }
            } catch (NotFoundException e) {
            }
        }
    }

    private void addSystemApps(List<SelectableAppInfo> visibleApps, Intent intent, Set<String> excludePackages) {
        PackageManager pm = this.mPackageManager;
        for (ResolveInfo app : pm.queryIntentActivities(intent, 8704)) {
            if (!(app.activityInfo == null || app.activityInfo.applicationInfo == null)) {
                String packageName = app.activityInfo.packageName;
                int flags = app.activityInfo.applicationInfo.flags;
                if (!((flags & 1) == 0 && (flags & 128) == 0)) {
                    if (!excludePackages.contains(packageName)) {
                        int enabled = pm.getApplicationEnabledSetting(packageName);
                        if (enabled == 4 || enabled == 2) {
                            ApplicationInfo targetUserAppInfo = getAppInfoForUser(packageName, null, this.mUser);
                            if (targetUserAppInfo != null) {
                                if ((targetUserAppInfo.flags & 8388608) == 0) {
                                }
                            }
                        }
                        SelectableAppInfo info = new SelectableAppInfo();
                        info.packageName = app.activityInfo.packageName;
                        info.appName = app.activityInfo.applicationInfo.loadLabel(pm);
                        info.icon = app.activityInfo.loadIcon(pm);
                        info.activityName = app.activityInfo.loadLabel(pm);
                        if (info.activityName == null) {
                            info.activityName = info.appName;
                        }
                        visibleApps.add(info);
                    }
                }
            }
        }
    }

    private boolean isSystemPackage(String packageName) {
        try {
            PackageInfo pi = this.mPackageManager.getPackageInfo(packageName, 0);
            if (pi.applicationInfo == null) {
                return false;
            }
            int flags = pi.applicationInfo.flags;
            if (!((flags & 1) == 0 && (flags & 128) == 0)) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
        }
    }

    private ApplicationInfo getAppInfoForUser(String packageName, int flags, UserHandle user) {
        try {
            return this.mIPm.getApplicationInfo(packageName, flags, user.getIdentifier());
        } catch (RemoteException e) {
            return null;
        }
    }
}
