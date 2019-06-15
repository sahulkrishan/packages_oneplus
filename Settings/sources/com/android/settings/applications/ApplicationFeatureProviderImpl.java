package com.android.settings.applications;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.RemoteException;
import android.os.UserManager;
import android.telecom.DefaultDialerManager;
import android.text.TextUtils;
import android.util.ArraySet;
import com.android.internal.telephony.SmsApplication;
import com.android.settings.applications.ApplicationFeatureProvider.ListOfAppsCallback;
import com.android.settings.applications.ApplicationFeatureProvider.NumberOfAppsCallback;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ApplicationFeatureProviderImpl implements ApplicationFeatureProvider {
    private final Context mContext;
    private final DevicePolicyManager mDpm;
    private final PackageManagerWrapper mPm;
    private final IPackageManager mPms;
    private final UserManager mUm = UserManager.get(this.mContext);

    private static class CurrentUserAndManagedProfileAppWithAdminGrantedPermissionsCounter extends AppWithAdminGrantedPermissionsCounter {
        private NumberOfAppsCallback mCallback;

        CurrentUserAndManagedProfileAppWithAdminGrantedPermissionsCounter(Context context, String[] permissions, PackageManagerWrapper packageManager, IPackageManager packageManagerService, DevicePolicyManager devicePolicyManager, NumberOfAppsCallback callback) {
            super(context, permissions, packageManager, packageManagerService, devicePolicyManager);
            this.mCallback = callback;
        }

        /* Access modifiers changed, original: protected */
        public void onCountComplete(int num) {
            this.mCallback.onNumberOfAppsResult(num);
        }
    }

    private static class CurrentUserAndManagedProfilePolicyInstalledAppCounter extends InstalledAppCounter {
        private NumberOfAppsCallback mCallback;

        CurrentUserAndManagedProfilePolicyInstalledAppCounter(Context context, PackageManagerWrapper packageManager, NumberOfAppsCallback callback) {
            super(context, 1, packageManager);
            this.mCallback = callback;
        }

        /* Access modifiers changed, original: protected */
        public void onCountComplete(int num) {
            this.mCallback.onNumberOfAppsResult(num);
        }
    }

    private static class CurrentUserAppWithAdminGrantedPermissionsLister extends AppWithAdminGrantedPermissionsLister {
        private ListOfAppsCallback mCallback;

        CurrentUserAppWithAdminGrantedPermissionsLister(String[] permissions, PackageManagerWrapper packageManager, IPackageManager packageManagerService, DevicePolicyManager devicePolicyManager, UserManager userManager, ListOfAppsCallback callback) {
            super(permissions, packageManager, packageManagerService, devicePolicyManager, userManager);
            this.mCallback = callback;
        }

        /* Access modifiers changed, original: protected */
        public void onAppListBuilt(List<UserAppInfo> list) {
            this.mCallback.onListOfAppsResult(list);
        }
    }

    private static class CurrentUserPolicyInstalledAppLister extends InstalledAppLister {
        private ListOfAppsCallback mCallback;

        CurrentUserPolicyInstalledAppLister(PackageManagerWrapper packageManager, UserManager userManager, ListOfAppsCallback callback) {
            super(packageManager, userManager);
            this.mCallback = callback;
        }

        /* Access modifiers changed, original: protected */
        public void onAppListBuilt(List<UserAppInfo> list) {
            this.mCallback.onListOfAppsResult(list);
        }
    }

    public ApplicationFeatureProviderImpl(Context context, PackageManagerWrapper pm, IPackageManager pms, DevicePolicyManager dpm) {
        this.mContext = context.getApplicationContext();
        this.mPm = pm;
        this.mPms = pms;
        this.mDpm = dpm;
    }

    public void calculateNumberOfPolicyInstalledApps(boolean async, NumberOfAppsCallback callback) {
        CurrentUserAndManagedProfilePolicyInstalledAppCounter counter = new CurrentUserAndManagedProfilePolicyInstalledAppCounter(this.mContext, this.mPm, callback);
        if (async) {
            counter.execute(new Void[0]);
        } else {
            counter.executeInForeground();
        }
    }

    public void listPolicyInstalledApps(ListOfAppsCallback callback) {
        new CurrentUserPolicyInstalledAppLister(this.mPm, this.mUm, callback).execute(new Void[0]);
    }

    public void calculateNumberOfAppsWithAdminGrantedPermissions(String[] permissions, boolean async, NumberOfAppsCallback callback) {
        CurrentUserAndManagedProfileAppWithAdminGrantedPermissionsCounter counter = new CurrentUserAndManagedProfileAppWithAdminGrantedPermissionsCounter(this.mContext, permissions, this.mPm, this.mPms, this.mDpm, callback);
        if (async) {
            counter.execute(new Void[0]);
        } else {
            counter.executeInForeground();
        }
    }

    public void listAppsWithAdminGrantedPermissions(String[] permissions, ListOfAppsCallback callback) {
        new CurrentUserAppWithAdminGrantedPermissionsLister(permissions, this.mPm, this.mPms, this.mDpm, this.mUm, callback).execute(new Void[0]);
    }

    public List<UserAppInfo> findPersistentPreferredActivities(int userId, Intent[] intents) {
        List<UserAppInfo> preferredActivities = new ArrayList();
        Set<UserAppInfo> uniqueApps = new ArraySet();
        UserInfo userInfo = this.mUm.getUserInfo(userId);
        for (Intent intent : intents) {
            try {
                ResolveInfo resolveInfo = this.mPms.findPersistentPreferredActivity(intent, userId);
                if (resolveInfo != null) {
                    ComponentInfo componentInfo = null;
                    if (resolveInfo.activityInfo != null) {
                        componentInfo = resolveInfo.activityInfo;
                    } else if (resolveInfo.serviceInfo != null) {
                        componentInfo = resolveInfo.serviceInfo;
                    } else if (resolveInfo.providerInfo != null) {
                        componentInfo = resolveInfo.providerInfo;
                    }
                    if (componentInfo != null) {
                        UserAppInfo info = new UserAppInfo(userInfo, componentInfo.applicationInfo);
                        if (uniqueApps.add(info)) {
                            preferredActivities.add(info);
                        }
                    }
                }
            } catch (RemoteException e) {
            }
        }
        return preferredActivities;
    }

    public Set<String> getKeepEnabledPackages() {
        Set<String> keepEnabledPackages = new ArraySet();
        String defaultDialer = DefaultDialerManager.getDefaultDialerApplication(this.mContext);
        if (!TextUtils.isEmpty(defaultDialer)) {
            keepEnabledPackages.add(defaultDialer);
        }
        ComponentName defaultSms = SmsApplication.getDefaultSmsApplication(this.mContext, true);
        if (defaultSms != null) {
            keepEnabledPackages.add(defaultSms.getPackageName());
        }
        keepEnabledPackages.add("com.oppo.market");
        return keepEnabledPackages;
    }
}
