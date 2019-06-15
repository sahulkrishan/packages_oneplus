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

public interface PackageManagerWrapper {
    void deletePackageAsUser(String str, IPackageDeleteObserver iPackageDeleteObserver, int i, int i2);

    ApplicationInfo getApplicationInfoAsUser(String str, int i, int i2) throws NameNotFoundException;

    String getDefaultBrowserPackageNameAsUser(int i);

    ComponentName getHomeActivities(List<ResolveInfo> list);

    int getInstallReason(String str, UserHandle userHandle);

    List<ApplicationInfo> getInstalledApplicationsAsUser(int i, int i2);

    PackageManager getPackageManager();

    VolumeInfo getPrimaryStorageCurrentVolume();

    boolean hasSystemFeature(String str);

    List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int i, int i2);

    List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int i, int i2);

    void replacePreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName);

    boolean setDefaultBrowserPackageNameAsUser(String str, int i);
}
