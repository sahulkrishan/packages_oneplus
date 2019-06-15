package com.android.settings.applications;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import java.util.List;

public class PackageManagerWrapperImpl implements PackageManagerWrapper {
    private final PackageManager mPm;

    public PackageManagerWrapperImpl(PackageManager pm) {
        this.mPm = pm;
    }

    public PackageManager getPackageManager() {
        return this.mPm;
    }

    public List<ApplicationInfo> getInstalledApplicationsAsUser(int flags, int userId) {
        return this.mPm.getInstalledApplicationsAsUser(flags, userId);
    }

    public boolean hasSystemFeature(String name) {
        return this.mPm.hasSystemFeature(name);
    }

    public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
        return this.mPm.queryIntentActivitiesAsUser(intent, flags, userId);
    }

    public int getInstallReason(String packageName, UserHandle user) {
        return this.mPm.getInstallReason(packageName, user);
    }

    public ApplicationInfo getApplicationInfoAsUser(String packageName, int i, int userId) throws NameNotFoundException {
        return this.mPm.getApplicationInfoAsUser(packageName, i, userId);
    }

    public boolean setDefaultBrowserPackageNameAsUser(String packageName, int userId) {
        return this.mPm.setDefaultBrowserPackageNameAsUser(packageName, userId);
    }

    public String getDefaultBrowserPackageNameAsUser(int userId) {
        return this.mPm.getDefaultBrowserPackageNameAsUser(userId);
    }

    public ComponentName getHomeActivities(List<ResolveInfo> homeActivities) {
        return this.mPm.getHomeActivities(homeActivities);
    }

    public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int i, int user) {
        return this.mPm.queryIntentServicesAsUser(intent, i, user);
    }

    public void replacePreferredActivity(IntentFilter homeFilter, int matchCategoryEmpty, ComponentName[] componentNames, ComponentName component) {
        this.mPm.replacePreferredActivity(homeFilter, matchCategoryEmpty, componentNames, component);
    }

    public void deletePackageAsUser(String packageName, IPackageDeleteObserver observer, int flags, int userId) {
        this.mPm.deletePackageAsUser(packageName, observer, flags, userId);
    }

    public VolumeInfo getPrimaryStorageCurrentVolume() {
        return this.mPm.getPrimaryStorageCurrentVolume();
    }
}
